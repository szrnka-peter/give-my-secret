package io.github.gms.secure.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Peter Szrnka
 * @since 1.0
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CredentialPairApiResponseDto implements ApiResponseDto {

	private String username;
	private String password;
}
