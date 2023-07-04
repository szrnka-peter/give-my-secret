package io.github.gms.secure.converter.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Lists;

import io.github.gms.abstraction.AbstractUnitTest;
import io.github.gms.common.enums.AliasOperation;
import io.github.gms.common.enums.EntityStatus;
import io.github.gms.common.enums.KeystoreType;
import io.github.gms.secure.dto.KeystoreAliasDto;
import io.github.gms.secure.dto.KeystoreDto;
import io.github.gms.secure.dto.KeystoreListDto;
import io.github.gms.secure.dto.SaveKeystoreRequestDto;
import io.github.gms.secure.entity.KeystoreAliasEntity;
import io.github.gms.secure.entity.KeystoreEntity;
import io.github.gms.secure.model.EnabledAlgorithm;
import io.github.gms.util.TestUtils;

/**
 * @author Peter Szrnka
 * @since 1.0
 */
class KeystoreConverterImplTest extends AbstractUnitTest {

	private static final String FILE_NAME = "test.jks";
	private Clock clock;
	private KeystoreConverterImpl converter;

	@BeforeEach
	void beforeEach() {
		clock = mock(Clock.class);
		converter = new KeystoreConverterImpl(clock);
	}

	@Test
	void checkToEntityWithoutFile() {
		// act
		KeystoreEntity entity = converter.toEntity(TestUtils.createKeystoreEntity(),
				TestUtils.createSaveKeystoreRequestDto());

		// assert
		assertNotNull(entity);
		assertEquals("keystore", entity.getName());
		assertEquals(1L, entity.getUserId());
		assertEquals("description", entity.getDescription());
		assertEquals("test", entity.getCredential());
		assertEquals(EntityStatus.ACTIVE, entity.getStatus());
		assertEquals(KeystoreType.JKS, entity.getType());
		assertEquals(FILE_NAME, entity.getFileName());
		assertEquals("test.jks", entity.getFileName());
	}

	@Test
	void checkToEntityWithParameters() {
		// arrange
		when(clock.instant()).thenReturn(Instant.parse("2023-06-29T00:00:00Z"));
		when(clock.getZone()).thenReturn(ZoneOffset.UTC);
		KeystoreEntity existingEntity = TestUtils.createKeystoreEntity();
		existingEntity.setCreationDate(ZonedDateTime.now(clock));

		// act
		KeystoreEntity entity = converter.toEntity(existingEntity, TestUtils.createSaveKeystoreRequestDto());

		// assert
		assertNotNull(entity);
		assertEquals("KeystoreEntity(id=1, userId=1, status=ACTIVE, name=keystore, fileName=test.jks, type=JKS, description=description, credential=test, creationDate=2023-06-29T00:00Z)", entity.toString());
	}

	@Test
	void checkToNewEntityWithoutFile() {
		// arrange
		setupClock(clock);

		// arrange
		SaveKeystoreRequestDto dto = TestUtils.createSaveKeystoreRequestDto();

		// act
		KeystoreEntity entity = converter.toNewEntity(dto, null);

		// assert
		assertNotNull(entity);
		assertEquals("keystore", entity.getName());
		assertEquals(1L, entity.getUserId());
		assertEquals("description", entity.getDescription());
		assertEquals("test", entity.getCredential());
		assertEquals(EntityStatus.ACTIVE, entity.getStatus());
		assertEquals(KeystoreType.JKS, entity.getType());
		assertNull(entity.getFileName());
	}

	@Test
	void checkToNewEntity() {
		// arrange
		when(clock.instant()).thenReturn(Instant.parse("2023-06-29T00:00:00Z"));
		when(clock.getZone()).thenReturn(ZoneOffset.UTC);

		// arrange
		SaveKeystoreRequestDto dto = TestUtils.createSaveKeystoreRequestDto();
		MultipartFile multipartFile = mock(MultipartFile.class);
		when(multipartFile.getOriginalFilename()).thenReturn(FILE_NAME);

		// act
		KeystoreEntity entity = converter.toNewEntity(dto, multipartFile);

		// assert
		assertNotNull(entity);
		assertEquals("keystore", entity.getName());
		assertEquals(1L, entity.getUserId());
		assertEquals("description", entity.getDescription());
		assertEquals("test", entity.getCredential());
		assertEquals(EntityStatus.ACTIVE, entity.getStatus());
		assertEquals(KeystoreType.JKS, entity.getType());
		assertEquals(FILE_NAME, entity.getFileName());
		assertEquals("2023-06-29T00:00Z", entity.getCreationDate().toString());
		verify(multipartFile).getOriginalFilename();
	}

	@Test
	void checkToList() {
		// arrange
		Page<KeystoreEntity> entityList = new PageImpl<>(Lists.newArrayList(TestUtils.createKeystoreEntity()));

		// act
		KeystoreListDto resultList = converter.toDtoList(entityList);

		// assert
		assertNotNull(resultList);
		assertEquals(1, resultList.getResultList().size());
		assertEquals(1L, resultList.getTotalElements());
		assertNotNull(resultList.getResultList().get(0));
		assertEquals("KeystoreDto(id=1, userId=1, status=ACTIVE, name=keystore, fileName=test.jks, type=JKS, description=description, credential=test, creationDate=null, aliases=[])", resultList.getResultList().get(0).toString());
	}

	@Test
	void checkToDto() {
		// arrange
		when(clock.instant()).thenReturn(Instant.parse("2023-06-29T00:00:00Z"));
		when(clock.getZone()).thenReturn(ZoneOffset.UTC);
		KeystoreEntity entity = TestUtils.createKeystoreEntity();
		entity.setCreationDate(ZonedDateTime.now(clock));

		// act
		KeystoreDto response = converter.toDto(entity, List.of(TestUtils.createKeystoreAliasEntity()));

		// assert
		assertNotNull(response);
		assertEquals("KeystoreDto(id=1, userId=1, status=ACTIVE, name=keystore, fileName=test.jks, type=JKS, description=description, credential=test, creationDate=2023-06-29T00:00Z, aliases=[KeystoreAliasDto(id=1, alias=test, aliasCredential=test, operation=null, algorithm=algorithm)])", response.toString());
	}

	@Test
	void checkToAliasEntity() {
		// arrange
		KeystoreAliasDto aliasDto = new KeystoreAliasDto(1L, "alias", "test1234", AliasOperation.SAVE,
				EnabledAlgorithm.SHA256WITHRSA.getDisplayName());

		// act
		KeystoreAliasEntity response = converter.toAliasEntity(1L, aliasDto);

		// assert
		assertNotNull(response);
		assertEquals(1L, response.getId());
		assertEquals(1, response.getKeystoreId());
		assertEquals("alias", response.getAlias());
		assertEquals("test1234", response.getAliasCredential());
		assertEquals(EnabledAlgorithm.SHA256WITHRSA.getDisplayName(), response.getAlgorithm());
	}
}
