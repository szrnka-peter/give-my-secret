package io.github.gms.common.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class HealthcheckControllerTest {
    
    private HealthcheckController controller;

    @Test
    void shouldReturnSecret() {
        // arrange
        controller = new HealthcheckController();

        // act
        ResponseEntity<Void> response = controller.healthcheck();

        // arrange
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
    }
}