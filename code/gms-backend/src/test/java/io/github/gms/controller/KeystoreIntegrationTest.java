package io.github.gms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;
import io.github.gms.abstraction.AbstractClientControllerIntegrationTest;
import io.github.gms.common.enums.EntityStatus;
import io.github.gms.common.enums.KeyStoreValueType;
import io.github.gms.common.filter.SecureHeaderInitializerFilter;
import io.github.gms.common.util.Constants;
import io.github.gms.secure.controller.KeystoreController;
import io.github.gms.secure.dto.*;
import io.github.gms.secure.entity.KeystoreEntity;
import io.github.gms.util.DemoData;
import io.github.gms.util.TestConstants;
import io.github.gms.util.TestUtils;
import io.github.gms.util.TestUtils.ValueHolder;
import lombok.SneakyThrows;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Szrnka
 * @since 1.0
 */
@Tag(TestConstants.TAG_INTEGRATION_TEST)
class KeystoreIntegrationTest extends AbstractClientControllerIntegrationTest {

	@Autowired
	private SecureHeaderInitializerFilter secureHeaderInitializerFilter;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private WebApplicationContext webApplicationContext;

	KeystoreIntegrationTest() {
		super("/secure/keystore");
	}

	@Test
	@SneakyThrows
	void testSave() {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream jksFileStream = classloader.getResourceAsStream("test.jks");

		MockMultipartFile sampleFile = new MockMultipartFile(KeystoreController.MULTIPART_FILE,
				"test-" + UUID.randomUUID().toString() + ".jks", MediaType.APPLICATION_OCTET_STREAM_VALUE,
				jksFileStream.readAllBytes());

		String saveRequestJson = objectMapper.writeValueAsString(TestUtils.createSaveKeystoreRequestDto());

		MockMultipartHttpServletRequestBuilder multipartRequest = MockMvcRequestBuilders.multipart("/secure/keystore")
				.file(sampleFile);

		multipartRequest.flashAttr("model", saveRequestJson);

		multipartRequest.headers(TestUtils.getHttpHeaders(null));
		multipartRequest.cookie(TestUtils.getCookie(jwt));

		MockMvc mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
				.addFilter(secureHeaderInitializerFilter, "/secure/keystore").build();

		// act
		mvc.perform(multipartRequest).andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	void testGetById() {
		// act
		HttpEntity<Void> requestEntity = new HttpEntity<>(TestUtils.getHttpHeaders(jwt));
		ResponseEntity<KeystoreDto> response = executeHttpGet("/" + DemoData.KEYSTORE_ID, requestEntity,
				KeystoreDto.class);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		KeystoreDto responseBody = response.getBody();
		assertEquals(DemoData.KEYSTORE_ID, responseBody.getId());
		assertEquals(EntityStatus.ACTIVE, responseBody.getStatus());
	}

	@Test
	void testList() {
		// act
		PagingDto request = PagingDto.builder().page(0).size(50).direction("ASC").property("id").build();

		HttpEntity<PagingDto> requestEntity = new HttpEntity<>(request, TestUtils.getHttpHeaders(jwt));
		ResponseEntity<KeystoreListDto> response = executeHttpPost("/list", requestEntity, KeystoreListDto.class);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		KeystoreListDto responseList = response.getBody();
		assertEquals(2, responseList.getResultList().size());
	}

	@ParameterizedTest
	@MethodSource("valueData")
	void testGetSecretValue(ValueHolder input) {
		// act
		GetSecureValueDto dto = new GetSecureValueDto(DemoData.KEYSTORE_ID, input.getAliasId(), input.getValueType());
		HttpEntity<GetSecureValueDto> requestEntity = new HttpEntity<>(dto, TestUtils.getHttpHeaders(jwt));
		ResponseEntity<String> response = executeHttpPost("/value", requestEntity, String.class);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		String responseBody = response.getBody();
		assertEquals(input.getExpectedValue(), responseBody);
	}

	@Test
	@SneakyThrows
	void testDelete() {
		// arrange
		KeystoreEntity newEntity = keystoreRepository.save(TestUtils.createNewKeystoreEntity(3L));

		// act
		HttpEntity<Void> requestEntity = new HttpEntity<>(TestUtils.getHttpHeaders(jwt));
		ResponseEntity<String> response = executeHttpDelete("/" + newEntity.getId(), requestEntity, String.class);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNull(response.getBody());

		Files.copy(new File("src/test/resources/test.jks"), new File("src/test/resources/1/test.jks"));
	}

	@Transactional
	@ParameterizedTest
	@ValueSource(booleans = { false, true })
	void testToggleStatus(boolean enabled) {
		// act
		HttpEntity<Void> requestEntity = new HttpEntity<>(TestUtils.getHttpHeaders(jwt));
		ResponseEntity<String> response = executeHttpPost("/" + DemoData.KEYSTORE_ID + "?enabled=" + enabled,
				requestEntity, String.class);

		// assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNull(response.getBody());

		KeystoreEntity entity = keystoreRepository.getReferenceById(DemoData.KEYSTORE_ID);
		assertNotNull(entity);
		assertEquals(enabled ? EntityStatus.ACTIVE : EntityStatus.DISABLED, entity.getStatus());

		executeHttpPost("/" + DemoData.KEYSTORE_ID + "?enabled=" + true, requestEntity, String.class);
	}

	@Test
	void testCount() {
		// arrange
		HttpEntity<GetSecureValueDto> requestEntity = new HttpEntity<>(TestUtils.getHttpHeaders(jwt));

		// act
		ResponseEntity<LongValueDto> response = executeHttpGet("/count", requestEntity, LongValueDto.class);

		// assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(2L, response.getBody().getValue());
	}

	@Test
	void testGetAllKeystoreNames() {
		// arrange
		HttpEntity<GetSecureValueDto> requestEntity = new HttpEntity<>(TestUtils.getHttpHeaders(jwt));

		// act
		ResponseEntity<IdNamePairListDto> response = executeHttpGet("/list_names", requestEntity,
				IdNamePairListDto.class);

		// assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertFalse(response.getBody().getResultList().isEmpty());
		assertEquals(2, response.getBody().getResultList().size());
	}

	@Test
	void testGetAllKeystoreAliasNames() {
		// arrange
		HttpEntity<Void> requestEntity = new HttpEntity<>(TestUtils.getHttpHeaders(jwt));

		// act
		ResponseEntity<IdNamePairListDto> response = executeHttpGet("/list_aliases/" + DemoData.KEYSTORE_ID,
				requestEntity, IdNamePairListDto.class);

		// assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertFalse(response.getBody().getResultList().isEmpty());
		assertEquals(2, response.getBody().getResultList().size());
	}

	@Test
	@SneakyThrows
	void testDownloadKeystoreFile() {
		// arrange
		File keystoreFile = new File("./keystores/1/test.jks");
		boolean keystoreFileCreated = !keystoreFile.exists();
		
		if (!keystoreFile.exists()) {
			Files.createParentDirs(keystoreFile);
			keystoreFile.createNewFile();
			Files.write("test".getBytes(), keystoreFile);
		}
		
		HttpHeaders headers = new HttpHeaders();	
		headers.add("Cookie", Constants.ACCESS_JWT_TOKEN + "=" + jwt + ";Max-Age=3600;HttpOnly");
		
		HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

		// act
		ResponseEntity<Resource> response = executeHttpGet("/download/" + DemoData.KEYSTORE_ID, requestEntity,
				Resource.class);

		// assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		
		if (keystoreFileCreated) {
			keystoreFile.delete();
		}
	}

	@Test
	void testGenerateKeystore() {
		// arrange
		SaveKeystoreRequestDto request = new SaveKeystoreRequestDto();
		HttpEntity<SaveKeystoreRequestDto> requestEntity = new HttpEntity<>(request, TestUtils.getHttpHeaders(jwt));

		// act
		ResponseEntity<String> response = executeHttpPost("/generate", requestEntity, String.class);

		// assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("generated.jks", response.getBody());
	}

	public static List<ValueHolder> valueData() {
		return Lists.newArrayList(new ValueHolder(KeyStoreValueType.KEYSTORE_ALIAS, DemoData.KEYSTORE_ALIAS_ID, "test"),
				new ValueHolder(KeyStoreValueType.KEYSTORE_ALIAS_CREDENTIAL, DemoData.KEYSTORE_ALIAS_ID, "test"),
				new ValueHolder(KeyStoreValueType.KEYSTORE_CREDENTIAL, null, "test"));
	}
}