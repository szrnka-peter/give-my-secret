package io.github.gms.functions.secret;

import io.github.gms.common.abstraction.GmsConverter;
import io.github.gms.common.enums.EntityStatus;
import io.github.gms.functions.secret.dto.SaveSecretRequestDto;
import io.github.gms.functions.secret.dto.SecretDto;
import io.github.gms.functions.secret.dto.SecretListDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * @author Peter Szrnka
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
public class SecretConverter implements GmsConverter<SecretListDto, SecretEntity> {

	private final Clock clock;

	public SecretEntity toEntity(SecretEntity entity, SaveSecretRequestDto dto) {
		entity.setKeystoreAliasId(dto.getKeystoreAliasId());
		entity.setSecretId(dto.getSecretId());
		entity.setUserId(dto.getUserId());
		entity.setReturnDecrypted(dto.isReturnDecrypted());
		entity.setRotationEnabled(dto.isRotationEnabled());
		entity.setLastUpdated(ZonedDateTime.now(clock));
		entity.setType(dto.getType());

		if (StringUtils.hasText(dto.getValue())) {
			entity.setValue(dto.getValue());
		}

		if (dto.getRotationPeriod() != null) {
			entity.setRotationPeriod(dto.getRotationPeriod());
		}

		if (dto.getStatus() != null) {
			entity.setStatus(dto.getStatus());
		}

		return entity;
	}

	public SecretEntity toNewEntity(SaveSecretRequestDto dto) {
		SecretEntity entity = new SecretEntity();
		entity.setSecretId(dto.getSecretId());
		entity.setKeystoreAliasId(dto.getKeystoreAliasId());
		entity.setUserId(dto.getUserId());
		entity.setValue(dto.getValue());
		entity.setCreationDate(ZonedDateTime.now(clock));
		entity.setLastUpdated(ZonedDateTime.now(clock));
		entity.setLastRotated(ZonedDateTime.now(clock));
		entity.setRotationPeriod(dto.getRotationPeriod());
		entity.setReturnDecrypted(dto.isReturnDecrypted());
		entity.setRotationEnabled(dto.isRotationEnabled());
		entity.setStatus(EntityStatus.ACTIVE);
		entity.setType(dto.getType());
		return entity;
	}

	public SecretDto toDto(SecretEntity entity) {
		SecretDto dto = new SecretDto();
		dto.setId(entity.getId());
		dto.setSecretId(entity.getSecretId());
		dto.setKeystoreAliasId(entity.getKeystoreAliasId());
		dto.setUserId(entity.getUserId());
		dto.setCreationDate(entity.getCreationDate());
		dto.setLastUpdated(entity.getLastUpdated());
		dto.setLastRotated(entity.getLastRotated());
		dto.setRotationPeriod(entity.getRotationPeriod());
		dto.setReturnDecrypted(entity.isReturnDecrypted());
		dto.setRotationEnabled(entity.isRotationEnabled());
		dto.setStatus(entity.getStatus());
		dto.setType(entity.getType());
		return dto;
	}

	@Override
	public SecretListDto toDtoList(Page<SecretEntity> resultList) {
		List<SecretDto> results = resultList.toList().stream().map(this::toDto).toList();
		return SecretListDto.builder().resultList(results).totalElements(resultList.getTotalElements()).build();
	}
}
