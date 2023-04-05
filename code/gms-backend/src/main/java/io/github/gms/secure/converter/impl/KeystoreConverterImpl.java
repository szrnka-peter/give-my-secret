package io.github.gms.secure.converter.impl;

import io.github.gms.secure.converter.KeystoreConverter;
import io.github.gms.secure.dto.KeystoreAliasDto;
import io.github.gms.secure.dto.KeystoreDto;
import io.github.gms.secure.dto.KeystoreListDto;
import io.github.gms.secure.dto.SaveKeystoreRequestDto;
import io.github.gms.secure.entity.KeystoreAliasEntity;
import io.github.gms.secure.entity.KeystoreEntity;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Peter Szrnka
 * @since 1.0
 */
@Component
public class KeystoreConverterImpl implements KeystoreConverter {

	private final Clock clock;

	public KeystoreConverterImpl(Clock clock) {
		this.clock = clock;
	}

	@Override
	public KeystoreEntity toNewEntity(SaveKeystoreRequestDto dto, MultipartFile file) {
		KeystoreEntity entity = new KeystoreEntity();

		entity.setName(dto.getName());
		entity.setUserId(dto.getUserId());
		entity.setDescription(dto.getDescription());
		entity.setCreationDate(ZonedDateTime.now(clock));
		entity.setCredential(dto.getCredential());
		entity.setName(dto.getName());
		if (file != null) {
			entity.setFileName(file.getOriginalFilename());
		}
		entity.setStatus(dto.getStatus());
		entity.setType(dto.getType());

		return entity;
	}

	@Override
	public KeystoreEntity toEntity(KeystoreEntity entity, SaveKeystoreRequestDto dto) {
		entity.setId(dto.getId());
		
		entity.setName(dto.getName());
		entity.setUserId(dto.getUserId());
		entity.setDescription(dto.getDescription());
		entity.setCredential(dto.getCredential());
		entity.setName(dto.getName());
		entity.setStatus(dto.getStatus());
		entity.setType(dto.getType());

		return entity;
	}

	@Override
	public KeystoreDto toDto(KeystoreEntity entity, List<KeystoreAliasEntity> aliasList) {
		KeystoreDto dto = new KeystoreDto();
		dto.setId(entity.getId());

		dto.setUserId(entity.getUserId());
		dto.setDescription(entity.getDescription());
		dto.setFileName(entity.getFileName());
		dto.setStatus(entity.getStatus());
		dto.setName(entity.getName());
		dto.setType(entity.getType());
		dto.setCreationDate(entity.getCreationDate());
		dto.setCredential(entity.getCredential());
		
		if (!CollectionUtils.isEmpty(aliasList)) {
			dto.setAliases(aliasList.stream().map(this::convertToAliasDto).collect(Collectors.toList()));
		}

		return dto;
	}

	@Override
	public KeystoreListDto toDtoList(Page<KeystoreEntity> resultList) {
		List<KeystoreDto> results = resultList.toList().stream().map(entity -> toDto(entity, null)).collect(Collectors.toList());
		return KeystoreListDto.builder().resultList(results).totalElements(resultList.getTotalElements()).build();
	}

	@Override
	public KeystoreAliasEntity toAliasEntity(Long keystoreId, KeystoreAliasDto dto) {
		KeystoreAliasEntity entity = new KeystoreAliasEntity();
		
		entity.setId(dto.getId());
		entity.setKeystoreId(keystoreId);
		entity.setAlias(dto.getAlias());
		entity.setAliasCredential(dto.getAliasCredential());
		entity.setAlgorithm(dto.getAlgorithm());

		return entity;
	}

	private KeystoreAliasDto convertToAliasDto(KeystoreAliasEntity entity) {
		KeystoreAliasDto dto = new KeystoreAliasDto();
		
		dto.setId(entity.getId());
		dto.setAlias(entity.getAlias());
		dto.setAliasCredential(entity.getAliasCredential());
		dto.setAlgorithm(entity.getAlgorithm());
		
		return dto;
	}
}
