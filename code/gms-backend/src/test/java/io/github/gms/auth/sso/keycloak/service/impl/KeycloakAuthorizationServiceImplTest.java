package io.github.gms.auth.sso.keycloak.service.impl;

import io.github.gms.auth.model.AuthorizationResponse;
import io.github.gms.auth.sso.keycloak.Input;
import io.github.gms.auth.sso.keycloak.converter.KeycloakConverter;
import io.github.gms.auth.sso.keycloak.model.IntrospectResponse;
import io.github.gms.auth.sso.keycloak.service.KeycloakIntrospectService;
import io.github.gms.util.TestUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;

import static io.github.gms.common.util.Constants.ACCESS_JWT_TOKEN;
import static io.github.gms.common.util.Constants.REFRESH_JWT_TOKEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Peter Szrnka
 * @since 1.0
 */
class KeycloakAuthorizationServiceImplTest {

    private KeycloakConverter converter;
    private KeycloakIntrospectService keycloakIntrospectService;
    private KeycloakAuthorizationServiceImpl service;

    @BeforeEach
    public void setup() {
        converter = mock(KeycloakConverter.class);
        keycloakIntrospectService = mock(KeycloakIntrospectService.class);
        service = new KeycloakAuthorizationServiceImpl(converter, keycloakIntrospectService);
    }

    @ParameterizedTest
    @MethodSource("emptyInputData")
    void shouldReturnEmptyInfo(Input input) {
        // arrange
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getCookies()).thenReturn(input.getCookies());

        // act
        AuthorizationResponse response = service.authorize(httpServletRequest);

        // assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getResponseStatus());
        assertEquals("Access denied!", response.getErrorMessage());
    }

    @Test
    void shouldReturnInfo() {
        // arrange
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getCookies()).thenReturn(new Cookie[]{
                new Cookie(ACCESS_JWT_TOKEN, "access"),
                new Cookie(REFRESH_JWT_TOKEN, "refresh")
        });
        IntrospectResponse mockIntrospectResponse = IntrospectResponse.builder().build();
        when(keycloakIntrospectService.getUserDetails("access", "refresh"))
                .thenReturn(mockIntrospectResponse);
        when(converter.toUserDetails(eq(mockIntrospectResponse))).thenReturn(TestUtils.createGmsAdminUser());

        // act
        AuthorizationResponse response = service.authorize(httpServletRequest);

        // assert
        assertNotNull(response);
        assertNotNull(response.getAuthentication());
        assertEquals(HttpStatus.OK, response.getResponseStatus());
    }

    private static Object[] emptyInputData() {
        return new Object[]{
                new Input(new Cookie[]{}),
                new Input(new Cookie[]{ new Cookie(ACCESS_JWT_TOKEN, "access") }),
                new Input(new Cookie[]{ new Cookie(REFRESH_JWT_TOKEN, "refresh") })
        };
    }
}
