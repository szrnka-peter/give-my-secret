package io.github.gms.api.controller;

import io.github.gms.abstraction.AbstractIntegrationTest;
import io.github.gms.secure.repository.KeystoreAliasRepository;
import io.github.gms.util.DemoData;
import io.github.gms.util.TestUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static io.github.gms.util.TestConstants.TAG_INTEGRATION_TEST;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Peter Szrnka
 * @since 1.0
 */
@Tag(TAG_INTEGRATION_TEST)
class ApiIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private KeystoreAliasRepository keystoreAliasRepository;

	@Test
	void testGetSecret() {

		// arrange
		apiKeyRepository.save(TestUtils.createApiKey(DemoData.API_KEY_3_ID, DemoData.API_KEY_CREDENTIAL3));
		keystoreAliasRepository.save(TestUtils.createKeystoreAliasEntity(DemoData.KEYSTORE_ALIAS3_ID, DemoData.KEYSTORE_ID));
		//keystoreRepository.save(TestUtils.createKeystoreEntity(DemoData.KEYSTORE3_ID, "test"));
		secretRepository.save(
				TestUtils.createSecretEntity(DemoData.SECRET_ENTITY3_ID, DemoData.KEYSTORE_ALIAS3_ID, DemoData.SECRET_ID3));
		
		// act
		HttpEntity<Void> requestEntity = new HttpEntity<>(TestUtils.getApiHttpHeaders(DemoData.API_KEY_CREDENTIAL3));
		ResponseEntity<Map> response = executeHttpGet("/api/secret/" + DemoData.SECRET_ID3, requestEntity, Map.class);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(DemoData.ENCRYPTED_VALUE, response.getBody().get("value"));
		
		secretRepository.deleteById(DemoData.SECRET_ENTITY3_ID);
		apiKeyRepository.deleteById(DemoData.API_KEY_3_ID);
		keystoreAliasRepository.deleteById(DemoData.KEYSTORE_ALIAS3_ID);
	}
}