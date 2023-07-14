package io.github.gms.secure.service.impl;

import static io.github.gms.common.util.Constants.ENTITY_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import ch.qos.logback.classic.Logger;
import io.github.gms.abstraction.AbstractLoggingUnitTest;
import io.github.gms.common.enums.EntityStatus;
import io.github.gms.common.enums.MdcParameter;
import io.github.gms.common.exception.GmsException;
import io.github.gms.secure.converter.ApiKeyConverter;
import io.github.gms.secure.dto.ApiKeyDto;
import io.github.gms.secure.dto.ApiKeyListDto;
import io.github.gms.secure.dto.IdNamePairDto;
import io.github.gms.secure.dto.IdNamePairListDto;
import io.github.gms.secure.dto.LongValueDto;
import io.github.gms.secure.dto.PagingDto;
import io.github.gms.secure.dto.SaveApiKeyRequestDto;
import io.github.gms.secure.dto.SaveEntityResponseDto;
import io.github.gms.secure.entity.ApiKeyEntity;
import io.github.gms.secure.repository.ApiKeyRepository;
import io.github.gms.util.TestUtils;

/**
 * @author Peter Szrnka
 * @since 1.0
 */
class ApiKeyServiceImplTest extends AbstractLoggingUnitTest {

	private ApiKeyServiceImpl service;
	private ApiKeyRepository repository;
	private ApiKeyConverter converter;

	@Override
	@BeforeEach
	public void setup() {
		super.setup();
		repository = mock(ApiKeyRepository.class);
		converter = mock(ApiKeyConverter.class);
		service = new ApiKeyServiceImpl(repository, converter);
		((Logger) LoggerFactory.getLogger(ApiKeyServiceImpl.class)).addAppender(logAppender);
	}

	@Test
	void shouldSaveNewEntity() {
		// arrange
		MDC.put(MdcParameter.USER_ID.getDisplayName(), "2");
		ApiKeyEntity mockEntity = new ApiKeyEntity();
		mockEntity.setId(1L);
		when(converter.toNewEntity(any(SaveApiKeyRequestDto.class))).thenReturn(mockEntity);
		when(repository.save(any(ApiKeyEntity.class))).thenReturn(mockEntity);
		when(repository.countAllApiKeysByName(anyLong(), anyString())).thenReturn(0L);
		when(repository.countAllApiKeysByValue(anyLong(), anyString())).thenReturn(0L);

		// act
		SaveEntityResponseDto response = service.save(TestUtils.createNewSaveApiKeyRequestDto());

		assertNotNull(response);

		ArgumentCaptor<SaveApiKeyRequestDto> requestCaptor = ArgumentCaptor.forClass(SaveApiKeyRequestDto.class);
		verify(converter).toNewEntity(requestCaptor.capture());

		assertEquals(2L, requestCaptor.getValue().getUserId());

		verify(repository).countAllApiKeysByName(anyLong(), anyString());
		verify(repository).countAllApiKeysByValue(anyLong(), anyString());
		verify(repository).save(any(ApiKeyEntity.class));

		MDC.remove(MdcParameter.USER_ID.getDisplayName());
	}

	@Test
	void shouldSaveExistingEntity() {
		// arrange
		MDC.put(MdcParameter.USER_ID.getDisplayName(), "2");
		ApiKeyEntity mockEntity = new ApiKeyEntity();
		mockEntity.setId(1L);
		when(repository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(TestUtils.createApiKey()));
		when(converter.toEntity(any(ApiKeyEntity.class), any(SaveApiKeyRequestDto.class))).thenReturn(mockEntity);
		when(repository.save(any(ApiKeyEntity.class))).thenReturn(mockEntity);
		when(repository.countAllApiKeysByName(anyLong(), anyString())).thenReturn(0L);
		when(repository.countAllApiKeysByValue(anyLong(), anyString())).thenReturn(0L);

		// act
		SaveEntityResponseDto response = service.save(TestUtils.createSaveApiKeyRequestDto());

		assertNotNull(response);
		verify(converter).toEntity(any(ApiKeyEntity.class), any(SaveApiKeyRequestDto.class));
		verify(repository).findByIdAndUserId(anyLong(), anyLong());
		verify(repository).save(any(ApiKeyEntity.class));
	}
	
	@Test
	void shouldNotSaveNewEntityWhenApiKeyNameIsNotUnique() {
		// arrange
		ApiKeyEntity mockEntity = new ApiKeyEntity();
		mockEntity.setId(1L);
		when(repository.countAllApiKeysByName(anyLong(), anyString())).thenReturn(1L);

		// act
		SaveApiKeyRequestDto input = TestUtils.createNewSaveApiKeyRequestDto();
		GmsException exception = assertThrows(GmsException.class, () -> service.save(input));

		// assert
		assertEquals("API key name must be unique!", exception.getMessage());
		verify(converter, never()).toNewEntity(any(SaveApiKeyRequestDto.class));
		verify(repository, never()).save(any(ApiKeyEntity.class));
		verify(repository).countAllApiKeysByName(anyLong(), anyString());
	}
	
	@Test
	void shouldNotSaveNewEntityWhenApiKeyValueIsNotUnique() {
		// arrange
		ApiKeyEntity mockEntity = new ApiKeyEntity();
		mockEntity.setId(1L);
		when(repository.countAllApiKeysByValue(anyLong(), anyString())).thenReturn(1L);

		// act
		SaveApiKeyRequestDto input = TestUtils.createNewSaveApiKeyRequestDto();
		GmsException exception = assertThrows(GmsException.class, () -> service.save(input));

		// assert
		assertEquals("API key value must be unique!", exception.getMessage());
		verify(converter, never()).toNewEntity(any(SaveApiKeyRequestDto.class));
		verify(repository, never()).save(any(ApiKeyEntity.class));
		verify(repository).countAllApiKeysByValue(anyLong(), anyString());
	}

	@Test
	void shouldNotFindById() {
		// arrange
		when(repository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());

		// act
		GmsException exception = assertThrows(GmsException.class, () -> service.getById(1L));

		// assert
		assertEquals(ENTITY_NOT_FOUND, exception.getMessage());
		verify(repository).findByIdAndUserId(anyLong(), anyLong());
		verify(converter, never()).toDto(any());
	}

	@Test
	void shouldFindById() {
		// arrange
		when(repository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(TestUtils.createApiKey()));
		when(converter.toDto(any())).thenReturn(new ApiKeyDto());

		// act
		ApiKeyDto response = service.getById(1L);

		// assert
		assertNotNull(response);
		verify(repository).findByIdAndUserId(anyLong(), anyLong());
		verify(converter).toDto(any());
	}
	
	@Test
	void shouldReturnEmptyList() {
		// arrange
		when(repository.findAllByUserId(anyLong(), any(Pageable.class))).thenThrow(new RuntimeException("Unexpected error!"));

		// act
		ApiKeyListDto response = service.list(new PagingDto("ASC", "id", 0, 10));

		// assert
		assertNotNull(response);
		assertEquals(0, response.getResultList().size());
		verify(repository).findAllByUserId(anyLong(), any(Pageable.class));
		verify(converter, never()).toDtoList(any());
	}

	@Test
	void shouldReturnList() {
		// arrange
		Page<ApiKeyEntity> mockList = new PageImpl<>(Lists.newArrayList(new ApiKeyEntity()));
		when(repository.findAllByUserId(anyLong(), any(Pageable.class))).thenReturn(mockList);
		when(converter.toDtoList(any())).thenReturn(ApiKeyListDto.builder().resultList(Lists.newArrayList(new ApiKeyDto())).build());

		// act
		ApiKeyListDto response = service.list(new PagingDto("ASC", "id", 0, 10));

		// assert
		assertNotNull(response);
		assertEquals(1, response.getResultList().size());
		verify(repository).findAllByUserId(anyLong(), any(Pageable.class));
		verify(converter).toDtoList(any());
	}
	
	@Test
	void shouldDelete() {
		// act
		service.delete(1L);

		// assert
		verify(repository).deleteById(1L);
	}
	
	@Test
	void shouldCount() {
		// arrange
		when(repository.countByUserId(anyLong())).thenReturn(3L);

		// act
		LongValueDto response = service.count();
		
		// assert
		assertNotNull(response);
		assertEquals(3L, response.getValue());
	}
	
	@ParameterizedTest
	@ValueSource(booleans = { true, false })
	void shouldToggleStatus(boolean enabled) {
		// arrange
		when(repository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(TestUtils.createApiKey()));

		// act
		service.toggleStatus(1L, enabled);

		// assert
		verify(repository).save(any());
		verify(repository).findByIdAndUserId(anyLong(), anyLong());

		ArgumentCaptor<ApiKeyEntity> argumentCaptor = ArgumentCaptor.forClass(ApiKeyEntity.class);
		verify(repository).save(argumentCaptor.capture());

		assertEquals(enabled, argumentCaptor.getValue().getStatus() == EntityStatus.ACTIVE);
	}
	
	@Test	
	void shouldReturnDecryptedValue() {
		// arrange
		when(repository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(TestUtils.createApiKey()));

		// act
		String response = service.getDecryptedValue(1L);

		// assert
		assertEquals("apikey", response);
		verify(repository).findByIdAndUserId(anyLong(), anyLong());
	}
	
	@Test
	void shouldGetAllApiKeyNames() {
		when(repository.getAllApiKeyNames(anyLong())).thenReturn(Lists.newArrayList(
				new IdNamePairDto(1L, "apikey1"),
				new IdNamePairDto(1L, "apikey2")
		));

		// act
		IdNamePairListDto response = service.getAllApiKeyNames();
		
		// assert
		assertNotNull(response);
		assertEquals(2, response.getResultList().size());
		assertEquals("apikey1", response.getResultList().get(0).getName());
		verify(repository).getAllApiKeyNames(anyLong());
	}
}
