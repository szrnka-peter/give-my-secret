package io.github.gms.common.service.impl;

import io.github.gms.common.dto.ResetPasswordRequestDto;
import io.github.gms.common.exception.GmsException;
import io.github.gms.secure.dto.MessageDto;
import io.github.gms.secure.repository.UserRepository;
import io.github.gms.secure.service.MessageService;
import io.github.gms.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Peter Szrnka
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class ResetPasswordServiceImplTest {

    private MessageService messageService;
    private UserRepository userRepository;

    private ResetPasswordServiceImpl service;

    @BeforeEach
    void setup() {
        messageService = mock(MessageService.class);
        userRepository = mock(UserRepository.class);
        service = new ResetPasswordServiceImpl(messageService, userRepository);
    }

    @Test
    void shouldThrowError() {
        // arrange
        ResetPasswordRequestDto dto = new ResetPasswordRequestDto("user");
        when(userRepository.findByUsername(eq("user"))).thenReturn(Optional.empty());

        // act
        GmsException exception = assertThrows(GmsException.class, () -> service.resetPassword(dto));

        // assert
        assertNotNull(exception);
        assertEquals("User not found!", exception.getMessage());
        verify(userRepository).findByUsername(eq("user"));
    }

    @Test
    void shouldSendMessages() {
        // arrange
        ResetPasswordRequestDto dto = new ResetPasswordRequestDto("user");
        when(userRepository.findByUsername(eq("user"))).thenReturn(Optional.of(TestUtils.createUser()));
        when(userRepository.getAllAdmins()).thenReturn(List.of(TestUtils.createUser()));

        // act
        assertDoesNotThrow(() -> service.resetPassword(dto));

        // assert
        verify(userRepository).findByUsername(eq("user"));
        verify(userRepository).getAllAdmins();

        ArgumentCaptor<MessageDto> messageDtoArgumentCaptor = ArgumentCaptor.forClass(MessageDto.class);
        verify(messageService).save(messageDtoArgumentCaptor.capture());

        MessageDto captured = messageDtoArgumentCaptor.getValue();
        assertEquals("Password reset requested by user 'user'", captured.getMessage());
        assertEquals("/user/1", captured.getActionPath());
    }
}
