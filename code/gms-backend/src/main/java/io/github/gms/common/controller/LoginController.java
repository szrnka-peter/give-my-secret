package io.github.gms.common.controller;

import io.github.gms.auth.AuthenticationService;
import io.github.gms.auth.dto.AuthenticateRequestDto;
import io.github.gms.auth.dto.AuthenticateResponseDto;
import io.github.gms.auth.model.AuthenticationResponse;
import io.github.gms.auth.types.AuthResponsePhase;
import io.github.gms.common.abstraction.AbstractLoginController;
import io.github.gms.common.util.CookieUtils;
import io.github.gms.functions.systemproperty.SystemPropertyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static io.github.gms.common.util.Constants.ACCESS_JWT_TOKEN;
import static io.github.gms.common.util.Constants.REFRESH_JWT_TOKEN;
import static io.github.gms.common.util.Constants.SET_COOKIE;

/**
 * @author Peter Szrnka
 * @since 1.0
 */
@RestController
@RequestMapping("/")
public class LoginController extends AbstractLoginController {
	
	public static final String LOGIN_PATH = "authenticate";
	public static final String LOGOUT_PATH = "logoutUser";

	private final AuthenticationService authenticationService;

	public LoginController(AuthenticationService authenticationService,
						   SystemPropertyService systemPropertyService,
						   @Value("${config.cookie.secure}") boolean secure) {
		super(systemPropertyService, secure);
		this.authenticationService = authenticationService;
	}

	@PostMapping(LOGIN_PATH)
	public ResponseEntity<AuthenticateResponseDto> loginAuthentication(@RequestBody AuthenticateRequestDto dto) {
		AuthenticationResponse authenticateResult = authenticationService.authenticate(dto.getUsername(), dto.getCredential());

		if (AuthResponsePhase.FAILED == authenticateResult.getPhase()) {
			HttpHeaders headers = new HttpHeaders();
			headers.add(SET_COOKIE, CookieUtils.createCookie(ACCESS_JWT_TOKEN, null, 0, secure).toString());
			headers.add(SET_COOKIE, CookieUtils.createCookie(REFRESH_JWT_TOKEN, null, 0, secure).toString());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).body(null);
		}

		return ResponseEntity.ok().headers(addHeaders(authenticateResult))
			.body(AuthenticateResponseDto.builder()
				.currentUser(authenticateResult.getCurrentUser())
				.phase(authenticateResult.getPhase())
				.build());
	}
	
	@PostMapping(LOGOUT_PATH)
	public ResponseEntity<Void> logout() {
		HttpHeaders headers = new HttpHeaders();

		authenticationService.logout();
		
		headers.add(SET_COOKIE, CookieUtils.createCookie(ACCESS_JWT_TOKEN, null, 0, secure).toString());
		headers.add(SET_COOKIE, CookieUtils.createCookie(REFRESH_JWT_TOKEN, null, 0, secure).toString());

		return ResponseEntity.ok().headers(headers).build();
	}
}