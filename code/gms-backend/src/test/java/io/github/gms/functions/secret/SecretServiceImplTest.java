package io.github.gms.functions.secret;

import ch.qos.logback.classic.Logger;
import com.google.common.collect.Sets;
import io.github.gms.abstraction.AbstractLoggingUnitTest;
import io.github.gms.common.dto.LongValueDto;
import io.github.gms.common.dto.PagingDto;
import io.github.gms.common.dto.SaveEntityResponseDto;
import io.github.gms.common.enums.EntityStatus;
import io.github.gms.common.enums.MdcParameter;
import io.github.gms.common.enums.SecretType;
import io.github.gms.common.service.CryptoService;
import io.github.gms.common.types.GmsException;
import io.github.gms.common.util.MdcUtils;
import io.github.gms.functions.iprestriction.IpRestrictionService;
import io.github.gms.functions.keystore.KeystoreAliasEntity;
import io.github.gms.functions.keystore.KeystoreAliasRepository;
import io.github.gms.functions.keystore.KeystoreEntity;
import io.github.gms.functions.keystore.KeystoreRepository;
import io.github.gms.util.DemoData;
import io.github.gms.util.TestUtils;
import lombok.SneakyThrows;
import org.assertj.core.util.Lists;
import org.jboss.logging.MDC;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Peter Szrnka
 * @since 1.0
 */
class SecretServiceImplTest extends AbstractLoggingUnitTest {

	private CryptoService cryptoService;
	private KeystoreRepository keystoreRepository;
	private KeystoreAliasRepository keystoreAliasRepository;
	private SecretRepository repository;
	private SecretConverter converter;
	private ApiKeyRestrictionRepository apiKeyRestrictionRepository;
	private IpRestrictionService ipRestrictionService;
	private SecretServiceImpl service;

	@Override
	@BeforeEach
	public void setup() {
		super.setup();
		cryptoService = mock(CryptoService.class);
		keystoreRepository = mock(KeystoreRepository.class);
		keystoreAliasRepository = mock(KeystoreAliasRepository.class);
		repository = mock(SecretRepository.class);
		converter = mock(SecretConverter.class);
		apiKeyRestrictionRepository = mock(ApiKeyRestrictionRepository.class);
		ipRestrictionService = mock(IpRestrictionService.class);
		service = new SecretServiceImpl(cryptoService, keystoreRepository, keystoreAliasRepository, repository,
				converter, apiKeyRestrictionRepository, ipRestrictionService);
		((Logger) LoggerFactory.getLogger(SecretServiceImpl.class)).addAppender(logAppender);

		MDC.put(MdcParameter.USER_ID.getDisplayName(), 1L);
	}

	@Test
	void shouldNotSaveNewEntityWhenKeystoreNotSent() {
		// arrange
		SaveSecretRequestDto dto = TestUtils.createSaveSecretRequestDto(2L);
		dto.setKeystoreAliasId(null);

		// act
		GmsException exception = assertThrows(GmsException.class, () -> service.save(dto));

		// assert
		assertEquals(SecretServiceImpl.WRONG_KEYSTORE_ALIAS, exception.getMessage());
		verify(keystoreRepository, never()).findByIdAndUserId(anyLong(), anyLong());
	}

	@Test
	void shouldNotSaveNewEntityWhenKeystoreAliasDoesNotExists() {
		// arrange
		when(keystoreAliasRepository.findById(anyLong())).thenReturn(Optional.empty());

		// act & assert
		SaveSecretRequestDto input = TestUtils.createSaveSecretRequestDto(2L);
		TestUtils.assertGmsException(() -> service.save(input), SecretServiceImpl.WRONG_KEYSTORE_ALIAS);
		verify(keystoreAliasRepository).findById(anyLong());
	}

	@Test
	void shouldNotSaveNewEntityWhenKeystoreDoesNotExists() {
		// arrange
		when(keystoreRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());
		when(keystoreAliasRepository.findById(anyLong()))
				.thenReturn(Optional.of(TestUtils.createKeystoreAliasEntity()));

		// act & assert
		SaveSecretRequestDto input = TestUtils.createSaveSecretRequestDto(2L);
		TestUtils.assertGmsException(() -> service.save(input), SecretServiceImpl.WRONG_ENTITY);
		verify(keystoreRepository).findByIdAndUserId(anyLong(), anyLong());
		verify(keystoreAliasRepository).findById(anyLong());
	}

	@Test
	void shouldNotSaveNewEntityWhenKeystoreDisabled() {
		// arrange
		KeystoreEntity mockKeystoreEntity = TestUtils.createKeystoreEntity();
		mockKeystoreEntity.setStatus(EntityStatus.DISABLED);
		when(keystoreRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(mockKeystoreEntity));
		when(keystoreAliasRepository.findById(anyLong()))
				.thenReturn(Optional.of(TestUtils.createKeystoreAliasEntity()));

		// act & assert
		SaveSecretRequestDto input = TestUtils.createSaveSecretRequestDto(2L);
		TestUtils.assertGmsException(() -> service.save(input), SecretServiceImpl.PLEASE_PROVIDE_ACTIVE_KEYSTORE);
		verify(keystoreRepository).findByIdAndUserId(anyLong(), anyLong());
		verify(keystoreAliasRepository).findById(anyLong());
	}

	@Test
	void shouldNotSaveNewEntityWhenKeystoreIsNotRelatedToAlias() {
		// arrange
		KeystoreEntity mockKeystoreEntity = TestUtils.createKeystoreEntity();
		when(keystoreRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(mockKeystoreEntity));
		when(keystoreAliasRepository.findById(anyLong()))
				.thenReturn(Optional.of(TestUtils.createKeystoreAliasEntity()));

		// act & assert
		SaveSecretRequestDto input = TestUtils.createSaveSecretRequestDto(2L);
		input.setKeystoreId(9L);
		TestUtils.assertGmsException(() -> service.save(input), "Invalid keystore defined in the request!");
		verify(keystoreRepository).findByIdAndUserId(anyLong(), anyLong());
		verify(keystoreAliasRepository).findById(anyLong());
	}

	@Test
	void shouldNotSaveNewEntityWhenSecretIdIsNotUnique() {
		// arrange
		MockedStatic<MdcUtils> mockedMdcUtils = mockStatic(MdcUtils.class);
		mockedMdcUtils.when(MdcUtils::getUserId).thenReturn(1L);
		SecretEntity mockEntity = TestUtils.createSecretEntity();
		when(keystoreRepository.findByIdAndUserId(anyLong(), anyLong()))
				.thenReturn(Optional.of(TestUtils.createKeystoreEntity()));
		when(keystoreAliasRepository.findById(anyLong()))
				.thenReturn(Optional.of(TestUtils.createKeystoreAliasEntity()));
		when(repository.countAllSecretsByUserIdAndSecretId(anyLong(), anyString())).thenReturn(1L);

		// act & assert
		SaveSecretRequestDto input = TestUtils.createNewSaveSecretRequestDto();
		TestUtils.assertGmsException(() -> service.save(input), "Secret ID name must be unique!");

		mockedMdcUtils.close();
		verify(keystoreRepository).findByIdAndUserId(anyLong(), anyLong());
		verify(converter, never()).toNewEntity(any(SaveSecretRequestDto.class));
		verify(cryptoService, never()).encrypt(mockEntity);
		verify(keystoreAliasRepository).findById(anyLong());
		verify(repository, never()).save(any(SecretEntity.class));
	}

	@ParameterizedTest
	@ValueSource(strings = { "a", "a;", "a;b", "a:x;b:" })
	void shouldNotSaveNewEntityWhenUsernamePasswordPairisInvalid(String inputValue) {
		// arrange
		MockedStatic<MdcUtils> mockedMdcUtils = mockStatic(MdcUtils.class);
		mockedMdcUtils.when(MdcUtils::getUserId).thenReturn(1L);
		SecretEntity mockEntity = TestUtils.createSecretEntity();
		when(keystoreRepository.findByIdAndUserId(anyLong(), anyLong()))
				.thenReturn(Optional.of(TestUtils.createKeystoreEntity()));
		when(keystoreAliasRepository.findById(anyLong()))
				.thenReturn(Optional.of(TestUtils.createKeystoreAliasEntity()));
		when(repository.countAllSecretsByUserIdAndSecretId(anyLong(), anyString())).thenReturn(0L);

		// act & assert
		SaveSecretRequestDto input = TestUtils.createNewSaveSecretRequestDto();
		input.setType(SecretType.MULTIPLE_CREDENTIAL);
		input.setValue(inputValue);
		TestUtils.assertGmsException(() -> service.save(input), "Username password pair is invalid!");

		verify(keystoreRepository).findByIdAndUserId(anyLong(), anyLong());
		verify(converter, never()).toNewEntity(any(SaveSecretRequestDto.class));
		verify(cryptoService, never()).encrypt(mockEntity);
		verify(keystoreAliasRepository).findById(anyLong());
		verify(repository, never()).save(any(SecretEntity.class));

		mockedMdcUtils.close();
	}

	@Test
	@SneakyThrows
	void shouldSaveNewEntityWhenValueIsNotProvided() {
		// arrange
		Clock clock = mock(Clock.class);
		when(clock.instant()).thenReturn(Instant.parse("2023-06-29T00:00:00Z"));
		when(clock.getZone()).thenReturn(ZoneOffset.UTC);
		MockedStatic<MdcUtils> mockedMdcUtils = mockStatic(MdcUtils.class);
		mockedMdcUtils.when(MdcUtils::getUserId).thenReturn(1L);
		SecretEntity mockEntity = TestUtils.createSecretEntity();
		mockEntity.setCreationDate(ZonedDateTime.now(clock));
		mockEntity.setLastRotated(ZonedDateTime.now(clock));
		when(keystoreRepository.findByIdAndUserId(anyLong(), anyLong()))
				.thenReturn(Optional.of(TestUtils.createKeystoreEntity()));
		when(keystoreAliasRepository.findById(anyLong()))
				.thenReturn(Optional.of(TestUtils.createKeystoreAliasEntity()));
		when(repository.countAllSecretsByUserIdAndSecretId(anyLong(), anyString())).thenReturn(0L);
		when(repository.save(any(SecretEntity.class))).thenReturn(mockEntity);
		when(converter.toNewEntity(any(SaveSecretRequestDto.class))).thenReturn(mockEntity);

		// act & assert
		SaveSecretRequestDto input = TestUtils.createNewSaveSecretRequestDto();
		input.setType(SecretType.MULTIPLE_CREDENTIAL);
		input.setValue(null);
		service.save(input);

		mockedMdcUtils.close();
		verify(keystoreRepository).findByIdAndUserId(anyLong(), anyLong());
		verify(converter).toNewEntity(any(SaveSecretRequestDto.class));
		verify(cryptoService).encrypt(mockEntity);
		verify(keystoreAliasRepository).findById(anyLong());

		ArgumentCaptor<SecretEntity> argumentCaptor = ArgumentCaptor.forClass(SecretEntity.class);
		verify(repository).save(argumentCaptor.capture());

		SecretEntity capturedEntity = argumentCaptor.getValue();
		assertEquals("SecretEntity(id=1, userId=1, keystoreAliasId=1, secretId=secret, value=test, status=ACTIVE, type=SIMPLE_CREDENTIAL, creationDate=2023-06-29T00:00Z, lastUpdated=null, lastRotated=2023-06-29T00:00Z, rotationPeriod=YEARLY, returnDecrypted=false, rotationEnabled=false)", capturedEntity.toString());
	}

	@Test
	@SneakyThrows
	void shouldSaveNewEntityWhenUsernamePasswordPairIsValid() {
		// arrange
		MockedStatic<MdcUtils> mockedMdcUtils = mockStatic(MdcUtils.class);
		mockedMdcUtils.when(MdcUtils::getUserId).thenReturn(5L);
		SecretEntity mockEntity = TestUtils.createSecretEntity();
		when(keystoreRepository.findByIdAndUserId(anyLong(), anyLong()))
				.thenReturn(Optional.of(TestUtils.createKeystoreEntity()));
		when(keystoreAliasRepository.findById(anyLong()))
				.thenReturn(Optional.of(TestUtils.createKeystoreAliasEntity()));
		when(converter.toNewEntity(any(SaveSecretRequestDto.class))).thenReturn(mockEntity);
		when(repository.countAllSecretsByUserIdAndSecretId(anyLong(), anyString())).thenReturn(0L);
		when(apiKeyRestrictionRepository.findAllByUserIdAndSecretId(anyLong(), anyLong())).thenReturn(List.of());
		when(repository.save(any(SecretEntity.class))).thenReturn(mockEntity);

		// act
		SaveSecretRequestDto input = TestUtils.createNewSaveSecretRequestDto();
		input.setType(SecretType.MULTIPLE_CREDENTIAL);
		input.setValue("username:test;password:y");

		SaveEntityResponseDto response = service.save(input);

		// assert
		assertNotNull(response);
		assertEquals(1L, response.getEntityId());
		verify(keystoreRepository).findByIdAndUserId(anyLong(), eq(5L));
		verify(converter).toNewEntity(any(SaveSecretRequestDto.class));
		verify(cryptoService).encrypt(mockEntity);
		verify(keystoreAliasRepository).findById(anyLong());
		verify(repository).save(any(SecretEntity.class));

		mockedMdcUtils.close();
	}

	@Test
	void shouldSaveNewEntity() {
		// arrange
		SecretEntity mockEntity = TestUtils.createSecretEntity();
		when(keystoreRepository.findByIdAndUserId(anyLong(), anyLong()))
				.thenReturn(Optional.of(TestUtils.createKeystoreEntity()));
		when(converter.toNewEntity(any(SaveSecretRequestDto.class))).thenReturn(mockEntity);
		when(repository.save(any(SecretEntity.class))).thenReturn(mockEntity);
		when(keystoreAliasRepository.findById(anyLong()))
				.thenReturn(Optional.of(TestUtils.createKeystoreAliasEntity()));

		// act
		SaveEntityResponseDto response = service.save(TestUtils.createNewSaveSecretRequestDto());

		// assert
		assertNotNull(response);
		assertEquals(1L, response.getEntityId());
		verify(keystoreRepository).findByIdAndUserId(anyLong(), anyLong());
		verify(converter).toNewEntity(any(SaveSecretRequestDto.class));
		verify(cryptoService).encrypt(mockEntity);
		verify(keystoreAliasRepository).findById(anyLong());
	}

	@Test
	void shouldSaveNewEntityWithApiKeyRestrictions() {
		// arrange
		SecretEntity mockEntity = TestUtils.createSecretEntity();
		when(keystoreRepository.findByIdAndUserId(anyLong(), anyLong()))
				.thenReturn(Optional.of(TestUtils.createKeystoreEntity()));
		when(converter.toNewEntity(any(SaveSecretRequestDto.class))).thenReturn(mockEntity);
		when(repository.save(any(SecretEntity.class))).thenReturn(mockEntity);
		when(keystoreAliasRepository.findById(anyLong()))
				.thenReturn(Optional.of(TestUtils.createKeystoreAliasEntity()));
		List<ApiKeyRestrictionEntity> mockApiRestrictionList = List.of(
			TestUtils.createApiKeyRestrictionEntity(1L), TestUtils.createApiKeyRestrictionEntity(2L)
		);
		when(apiKeyRestrictionRepository.findAllByUserIdAndSecretId(1L, 1L)).thenReturn(mockApiRestrictionList);

		// act
		Set<Long> apiKeys = Sets.newHashSet(3L);
		SaveEntityResponseDto response = service.save(TestUtils.createSaveSecretRequestDto(null, apiKeys));

		// assert
		assertNotNull(response);
		assertEquals(1L, response.getEntityId());
		verify(keystoreRepository).findByIdAndUserId(anyLong(), anyLong());
		verify(converter).toNewEntity(any(SaveSecretRequestDto.class));
		verify(cryptoService).encrypt(mockEntity);

		ArgumentCaptor<ApiKeyRestrictionEntity> captor = ArgumentCaptor.forClass(ApiKeyRestrictionEntity.class);
		verify(apiKeyRestrictionRepository, times(1)).save(captor.capture());
		assertEquals(1L, captor.getValue().getSecretId());
		assertEquals(1L, captor.getValue().getUserId());
		assertEquals(3L, captor.getValue().getApiKeyId());
		verify(keystoreAliasRepository).findById(anyLong());
	}

	@Test
	void shouldNotSaveExistingEntity() {
		// arrange
		SecretEntity mockEntity = TestUtils.createSecretEntity();
		when(keystoreRepository.findByIdAndUserId(anyLong(), anyLong()))
				.thenReturn(Optional.of(TestUtils.createKeystoreEntity()));
		when(keystoreAliasRepository.findById(anyLong()))
				.thenReturn(Optional.of(TestUtils.createKeystoreAliasEntity()));

		// act & assert
		TestUtils.assertGmsException(() -> service.save(TestUtils.createSaveSecretRequestDto(2L)), "Secret not found!");

		verify(keystoreRepository).findByIdAndUserId(anyLong(), anyLong());
		verify(converter, never()).toEntity(any(SecretEntity.class), any(SaveSecretRequestDto.class));
		verify(cryptoService, never()).encrypt(mockEntity);
		verify(keystoreAliasRepository).findById(anyLong());

		MDC.clear();
	}

	@Test
	void shouldSaveExistingEntity() {
		// arrange
		SecretEntity mockEntity = TestUtils.createSecretEntity();
		when(keystoreRepository.findByIdAndUserId(anyLong(), anyLong()))
				.thenReturn(Optional.of(TestUtils.createKeystoreEntity()));
		when(repository.findById(1L)).thenReturn(Optional.of(mockEntity));
		when(converter.toEntity(any(SecretEntity.class), any(SaveSecretRequestDto.class))).thenReturn(mockEntity);
		when(repository.save(any(SecretEntity.class))).thenReturn(mockEntity);
		when(keystoreAliasRepository.findById(anyLong()))
				.thenReturn(Optional.of(TestUtils.createKeystoreAliasEntity()));

		// act
		SaveEntityResponseDto response = service.save(TestUtils.createSaveSecretRequestDto(1L));

		// assert
		assertNotNull(response);
		assertEquals(1L, response.getEntityId());
		verify(keystoreRepository).findByIdAndUserId(anyLong(), anyLong());
		verify(repository).findById(1L);
		verify(converter).toEntity(any(SecretEntity.class), any(SaveSecretRequestDto.class));
		verify(cryptoService).encrypt(mockEntity);
		verify(keystoreAliasRepository).findById(anyLong());
	}

	@Test
	void shouldSaveExistingEntityAndRemoveApiKeys() {
		// arrange
		SecretEntity mockEntity = TestUtils.createSecretEntity();
		when(keystoreRepository.findByIdAndUserId(anyLong(), anyLong()))
				.thenReturn(Optional.of(TestUtils.createKeystoreEntity()));
		when(repository.findById(1L)).thenReturn(Optional.of(mockEntity));
		when(converter.toEntity(any(SecretEntity.class), any(SaveSecretRequestDto.class))).thenReturn(mockEntity);
		when(repository.save(any(SecretEntity.class))).thenReturn(mockEntity);
		when(apiKeyRestrictionRepository.findAllByUserIdAndSecretId(anyLong(), anyLong())).thenReturn(
				List.of(TestUtils.createApiKeyRestrictionEntity(1L), TestUtils.createApiKeyRestrictionEntity(2L)));
		when(keystoreAliasRepository.findById(anyLong()))
				.thenReturn(Optional.of(TestUtils.createKeystoreAliasEntity()));

		// act
		SaveEntityResponseDto response = service.save(TestUtils.createSaveSecretRequestDto(1L, Set.of(1L)));

		// assert
		assertNotNull(response);
		assertEquals(1L, response.getEntityId());
		verify(keystoreRepository).findByIdAndUserId(anyLong(), anyLong());
		verify(repository).findById(1L);
		verify(converter).toEntity(any(SecretEntity.class), any(SaveSecretRequestDto.class));
		verify(cryptoService).encrypt(mockEntity);
		verify(keystoreAliasRepository).findById(anyLong());
	}

	@Test
	void shouldSaveExistingEntityAndUpdateApiKeys() {
		// arrange
		SecretEntity mockEntity = TestUtils.createSecretEntity();
		when(keystoreRepository.findByIdAndUserId(anyLong(), anyLong()))
				.thenReturn(Optional.of(TestUtils.createKeystoreEntity()));
		when(repository.findById(1L)).thenReturn(Optional.of(mockEntity));
		when(converter.toEntity(any(SecretEntity.class), any(SaveSecretRequestDto.class))).thenReturn(mockEntity);
		when(repository.save(any(SecretEntity.class))).thenReturn(mockEntity);
		when(apiKeyRestrictionRepository.findAllByUserIdAndSecretId(anyLong(), anyLong())).thenReturn(
				List.of(TestUtils.createApiKeyRestrictionEntity(1L), TestUtils.createApiKeyRestrictionEntity(2L)));
		when(keystoreAliasRepository.findById(anyLong()))
				.thenReturn(Optional.of(TestUtils.createKeystoreAliasEntity()));

		// act
		SaveEntityResponseDto response = service
				.save(TestUtils.createSaveSecretRequestDto(1L, Sets.newHashSet(2L, 3L)));

		// assert
		assertNotNull(response);
		assertEquals(1L, response.getEntityId());
		verify(keystoreRepository).findByIdAndUserId(anyLong(), anyLong());
		verify(repository).findById(1L);
		verify(converter).toEntity(any(SecretEntity.class), any(SaveSecretRequestDto.class));
		verify(cryptoService).encrypt(mockEntity);
		verify(apiKeyRestrictionRepository, never()).delete(any(ApiKeyRestrictionEntity.class));
		verify(keystoreAliasRepository).findById(anyLong());
	}

	@Test
	void shouldSaveExistingEntityWithoutNewValue() {
		// arrange
		SecretEntity mockEntity = TestUtils.createSecretEntity();
		when(keystoreRepository.findByIdAndUserId(anyLong(), anyLong()))
				.thenReturn(Optional.of(TestUtils.createKeystoreEntity()));
		when(repository.findById(1L)).thenReturn(Optional.of(mockEntity));
		when(converter.toEntity(any(SecretEntity.class), any(SaveSecretRequestDto.class))).thenReturn(mockEntity);
		when(repository.save(any(SecretEntity.class))).thenReturn(mockEntity);
		when(keystoreAliasRepository.findById(anyLong()))
				.thenReturn(Optional.of(TestUtils.createKeystoreAliasEntity()));

		// act
		SaveSecretRequestDto dto = TestUtils.createSaveSecretRequestDto(1L);
		dto.setValue(null);
		SaveEntityResponseDto response = service.save(dto);

		// assert
		assertNotNull(response);
		assertEquals(1L, response.getEntityId());
		verify(keystoreRepository).findByIdAndUserId(anyLong(), anyLong());
		verify(repository).findById(1L);
		verify(converter).toEntity(any(SecretEntity.class), any(SaveSecretRequestDto.class));
		verify(cryptoService, never()).encrypt(mockEntity);
		verify(keystoreAliasRepository).findById(anyLong());
	}

	@Test
	void shouldNotFindById() {
		// arrange
		when(repository.findById(1L)).thenReturn(Optional.empty());

		// act
		GmsException exception = assertThrows(GmsException.class, () -> service.getById(1L));

		// assert
		assertEquals(SecretServiceImpl.WRONG_ENTITY, exception.getMessage());
		verify(repository).findById(1L);
		verify(converter, never()).toDto(any());
	}

	@ParameterizedTest
	@MethodSource("findByIdData")
	void shouldFindById(Optional<KeystoreAliasEntity> aliasEntity) {
		// arrange
		MockedStatic<MdcUtils> mockedMdcUtils = mockStatic(MdcUtils.class);
		mockedMdcUtils.when(MdcUtils::getUserId).thenReturn(4L);
		when(repository.findById(1L)).thenReturn(Optional.of(TestUtils.createSecretEntity()));
		when(converter.toDto(any())).thenReturn(new SecretDto());
		when(keystoreAliasRepository.findById(anyLong())).thenReturn(aliasEntity);

		List<ApiKeyRestrictionEntity> mockRestrictionEntities = List.of(TestUtils.createApiKeyRestrictionEntity(1L));
		when(apiKeyRestrictionRepository.findAllByUserIdAndSecretId(4L, 1L)).thenReturn(mockRestrictionEntities);

		// act
		SecretDto response = service.getById(1L);

		// assert
		assertNotNull(response);
		if (aliasEntity.isPresent()) {
			assertEquals(DemoData.KEYSTORE_ID, response.getKeystoreId());
		}
		verify(repository).findById(1L);
		verify(converter).toDto(any());
		verify(apiKeyRestrictionRepository).findAllByUserIdAndSecretId(4L, 1L);
		mockedMdcUtils.close();
	}

	@Test
	void shouldReturnList() {
		// arrange
		Page<SecretEntity> mockList = new PageImpl<>(Lists.newArrayList(TestUtils.createSecretEntity()));
		when(repository.findAllByUserId(anyLong(), any(Pageable.class))).thenReturn(mockList);
		when(converter.toDtoList(any())).thenReturn(SecretListDto.builder()
				.resultList(Lists.newArrayList(TestUtils.createSecretDto()))
				.totalElements(1).build());

		// act
		SecretListDto response = service.list(new PagingDto("ASC", "id", 0, 10));

		// assert
		assertNotNull(response);
		assertEquals(1, response.getResultList().size());
		verify(converter).toDtoList(any());
	}

	@Test
	void shouldDelete() {
		// act
		service.delete(1L);

		// assert
		verify(repository).deleteById(1L);
	}

	@ParameterizedTest
	@ValueSource(booleans = { true, false })
	void shouldToggleStatus(boolean enabled) {
		// arrange
		when(repository.findByIdAndUserId(anyLong(), anyLong()))
				.thenReturn(Optional.of(TestUtils.createSecretEntity()));

		// act
		service.toggleStatus(1L, enabled);

		// assert
		ArgumentCaptor<SecretEntity> entityCaptor = ArgumentCaptor.forClass(SecretEntity.class);
		verify(repository).save(entityCaptor.capture());
		SecretEntity capturedEntity = entityCaptor.getValue();
		assertEquals(enabled ? EntityStatus.ACTIVE : EntityStatus.DISABLED, capturedEntity.getStatus());
		verify(repository).findByIdAndUserId(eq(1L), anyLong());
	}

	@Test
	void shouldNotToggleStatus() {

		// act
		GmsException exception = assertThrows(GmsException.class, () -> service.toggleStatus(3L, true));

		// assert
		assertEquals("Wrong entity!", exception.getMessage());
		verify(repository, never()).save(any());

		MDC.clear();
	}

	@Test
	void shouldNotGetSecretValue() {
		// arrange
		when(repository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());

		// act
		GmsException exception = assertThrows(GmsException.class, () -> service.getSecretValue(1L));

		// assert
		assertEquals("Wrong entity!", exception.getMessage());
		verify(cryptoService, never()).decrypt(any());
	}

	@Test
	void shouldGetSecretValue() {
		// arrange
		when(repository.findByIdAndUserId(anyLong(), anyLong()))
				.thenReturn(Optional.of(TestUtils.createSecretEntity()));
		when(cryptoService.decrypt(any())).thenReturn("result");

		// act
		String response = service.getSecretValue(1L);

		// assert
		assertEquals("result", response);
		verify(cryptoService).decrypt(any());
	}

	@Test
	void shouldReturnCount() {
		// arrange
		when(repository.countByUserId(anyLong())).thenReturn(4L);

		// act
		LongValueDto response = service.count();

		// assert
		assertNotNull(response);
		assertEquals(4L, response.getValue());

		MDC.clear();
	}

	private static Object[] findByIdData() {
		return new Object[] {
			Optional.of(TestUtils.createKeystoreAliasEntity()),
			Optional.empty()
		};
	}
}
