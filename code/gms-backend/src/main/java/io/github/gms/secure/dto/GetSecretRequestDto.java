package io.github.gms.secure.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Peter Szrnka
 * @since 1.0
 */
@Data
@AllArgsConstructor
public class GetSecretRequestDto implements Serializable {

	@Serial
	private static final long serialVersionUID = 8124711164571263907L;
	private String apiKey;
	private String secretId;
}
