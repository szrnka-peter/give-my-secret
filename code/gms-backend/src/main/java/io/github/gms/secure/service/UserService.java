package io.github.gms.secure.service;

import dev.samstevens.totp.exceptions.QrGenerationException;
import io.github.gms.common.abstraction.AbstractCrudService;
import io.github.gms.secure.dto.ChangePasswordRequestDto;
import io.github.gms.secure.dto.SaveEntityResponseDto;
import io.github.gms.secure.dto.SaveUserRequestDto;
import io.github.gms.secure.dto.UserDto;
import io.github.gms.secure.dto.UserInfoDto;
import io.github.gms.secure.dto.UserListDto;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Peter Szrnka
 * @since 1.0
 */
public interface UserService extends AbstractCrudService<SaveUserRequestDto, SaveEntityResponseDto, UserDto, UserListDto>, CountService {
	
	SaveEntityResponseDto saveAdminUser(SaveUserRequestDto dto);

	SaveEntityResponseDto save(SaveUserRequestDto dto);
	
	String getUsernameById(Long id);

	void changePassword(ChangePasswordRequestDto dto);

    byte[] getMfaQrCode() throws QrGenerationException;

	void toggleMfa(boolean enabled);

    boolean isMfaActive();

	UserInfoDto getUserInfo(HttpServletRequest request);
}
