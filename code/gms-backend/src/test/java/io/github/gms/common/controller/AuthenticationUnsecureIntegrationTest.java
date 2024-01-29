package io.github.gms.common.controller;

import io.github.gms.abstraction.AbstractIntegrationTest;
import io.github.gms.auth.dto.AuthenticateRequestDto;
import io.github.gms.util.DemoData;
import io.github.gms.util.TestUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import static io.github.gms.common.util.Constants.SET_COOKIE;
import static io.github.gms.common.util.Constants.SLASH;
import static io.github.gms.util.TestConstants.TAG_INTEGRATION_TEST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Szrnka
 * @since 1.0
 */
@Tag(TAG_INTEGRATION_TEST)
class AuthenticationUnsecureIntegrationTest extends AbstractIntegrationTest {

	@Test
	void shouldNotAuthenticate() {
		
		// act
		AuthenticateRequestDto dto = new AuthenticateRequestDto(DemoData.USERNAME1, "testFail");
		HttpEntity<AuthenticateRequestDto> requestEntity = new HttpEntity<>(dto);
		HttpClientErrorException.Unauthorized exception = 
				assertThrows(HttpClientErrorException.Unauthorized.class, () -> executeHttpPost(SLASH + LoginController.LOGIN_PATH, requestEntity, String.class));
		
		// assert
		assertEquals("401 : [no body]", exception.getMessage());
	}
	
	@Test
	void shouldAuthenticate() {
		
		// act
		AuthenticateRequestDto dto = new AuthenticateRequestDto(DemoData.USERNAME1, "test");
		HttpEntity<AuthenticateRequestDto> requestEntity = new HttpEntity<>(dto);
		ResponseEntity<String> response = executeHttpPost(SLASH + LoginController.LOGIN_PATH, requestEntity, String.class);
		
		// assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
	}

	@Test
	void shouldLogout() {
		
		// act
		HttpEntity<Void> requestEntity = new HttpEntity<>(TestUtils.getHttpHeaders(jwt));
		ResponseEntity<String> response = executeHttpPost(SLASH + LoginController.LOGOUT_PATH, requestEntity, String.class);
		
		// assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertTrue(response.getHeaders().keySet().stream().anyMatch(header -> header.equalsIgnoreCase(SET_COOKIE)));
	}
}