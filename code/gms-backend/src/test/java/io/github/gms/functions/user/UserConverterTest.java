package io.github.gms.functions.user;

import com.google.common.collect.Lists;
import io.github.gms.abstraction.AbstractUnitTest;
import io.github.gms.auth.model.GmsUserDetails;
import io.github.gms.common.dto.UserInfoDto;
import io.github.gms.common.enums.EntityStatus;
import io.github.gms.common.enums.UserRole;
import io.github.gms.util.DemoData;
import io.github.gms.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static io.github.gms.common.enums.UserRole.ROLE_VIEWER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author Peter Szrnka
 * @since 1.0
 */
class UserConverterTest extends AbstractUnitTest {

	private Clock clock;
	private PasswordEncoder passwordEncoder;
	private UserConverter converter;

	@BeforeEach
	void beforeEach() {
		clock = mock(Clock.class);
		passwordEncoder = mock(PasswordEncoder.class);
		converter = new UserConverter(clock, passwordEncoder);
	}

	@Test
	void toEntity_whenRoleChangePresentedInDto_thenReturnEntity() {
		// arrange
		SaveUserRequestDto dto = TestUtils.createSaveUserRequestDto();
		when(passwordEncoder.encode(anyString())).thenReturn("encoded");

		// act
		UserEntity entity = converter.toEntity(TestUtils.createUser(), dto, false);

		// assert
		assertNotNull(entity);
		assertEquals("UserEntity(id=null, name=name, username=username, email=email@email.com, status=ACTIVE, credential=encoded, creationDate=null, role=ROLE_USER, mfaEnabled=false, mfaSecret=null, failedAttempts=0)", entity.toString());
		verify(passwordEncoder).encode(anyString());
	}

	@Test
	void toEntity_whenCredentialsMissing_thenReturnEntity() {
		// arrange
		when(clock.instant()).thenReturn(Instant.parse("2023-06-29T00:00:00Z"));
		when(clock.getZone()).thenReturn(ZoneOffset.UTC);
		SaveUserRequestDto dto = TestUtils.createSaveUserRequestDto();
		dto.setId(1L);
		dto.setCredential(null);

		UserEntity existingEntity = TestUtils.createUser();
		existingEntity.setCreationDate(ZonedDateTime.now(clock));

		// act
		UserEntity entity = converter.toEntity(existingEntity, dto, false);

		// assert
		assertNotNull(entity);
		assertEquals("UserEntity(id=1, name=name, username=username, email=email@email.com, status=ACTIVE, credential=OldCredential, creationDate=2023-06-29T00:00Z, role=ROLE_USER, mfaEnabled=false, mfaSecret=null, failedAttempts=0)", entity.toString());
	}

	@Test
	void toEntity_whenNewParametersProvided_thenReturnEntity() {
		// arrange
		when(clock.instant()).thenReturn(Instant.parse("2023-06-29T00:00:00Z"));
		when(clock.getZone()).thenReturn(ZoneOffset.UTC);
		SaveUserRequestDto dto = TestUtils.createSaveUserRequestDto();
		when(passwordEncoder.encode(anyString())).thenReturn("encoded");

		// act
		UserEntity mockEntity = TestUtils.createUser();
		mockEntity.setName("Test Test");
		mockEntity.setUsername("my-user-1");
		mockEntity.setCreationDate(ZonedDateTime.now(clock));
		mockEntity.setStatus(EntityStatus.DISABLED);
		mockEntity.setRole(ROLE_VIEWER);
		UserEntity entity = converter.toEntity(mockEntity, dto, true);

		// assert
		assertNotNull(entity);
		assertEquals("UserEntity(id=null, name=name, username=username, email=email@email.com, status=ACTIVE, credential=encoded, creationDate=2023-06-29T00:00Z, role=ROLE_USER, mfaEnabled=false, mfaSecret=null, failedAttempts=0)", entity.toString());
		verify(passwordEncoder).encode(anyString());
	}

	@Test
	void toNewEntity_whenInputProvided_thenReturnOk() {
		// arrange
		when(clock.instant()).thenReturn(Instant.parse("2023-06-29T00:00:00Z"));
		when(clock.getZone()).thenReturn(ZoneOffset.UTC);
		when(passwordEncoder.encode(anyString())).thenReturn("encoded");

		// arrange
		SaveUserRequestDto dto = TestUtils.createSaveUserRequestDto();

		// act
		UserEntity entity = converter.toNewEntity(dto, false);

		// assert
		assertNotNull(entity);
		assertEquals("UserEntity(id=null, name=name, username=username, email=email@email.com, status=ACTIVE, credential=encoded, creationDate=2023-06-29T00:00Z, role=null, mfaEnabled=false, mfaSecret=null, failedAttempts=0)", entity.toString());
		verify(passwordEncoder).encode(anyString());
	}

	@Test
	void toNewEntity_whenInputContainsRoleChange_thenReturnOk() {
		// arrange
		when(clock.instant()).thenReturn(Instant.parse("2023-06-29T00:00:00Z"));
		when(clock.getZone()).thenReturn(ZoneOffset.UTC);
		when(passwordEncoder.encode(anyString())).thenReturn("encoded");

		// arrange
		SaveUserRequestDto dto = TestUtils.createSaveUserRequestDto();

		// act
		UserEntity entity = converter.toNewEntity(dto, true);

		// assert
		assertNotNull(entity);
		assertEquals("UserEntity(id=null, name=name, username=username, email=email@email.com, status=ACTIVE, credential=encoded, creationDate=2023-06-29T00:00Z, role=ROLE_USER, mfaEnabled=false, mfaSecret=null, failedAttempts=0)", entity.toString());
		verify(passwordEncoder).encode(anyString());
	}

	@Test
	void toDtoList_whenInputProvided_thenReturnOk() {
		// arrange
		when(clock.instant()).thenReturn(Instant.parse("2023-06-29T00:00:00Z"));
		when(clock.getZone()).thenReturn(ZoneOffset.UTC);
		UserEntity userEntity = TestUtils.createUser();
		userEntity.setCreationDate(ZonedDateTime.now(clock));
		Page<UserEntity> entityList = new PageImpl<>(Lists.newArrayList(userEntity));

		// act
		UserListDto resultList = converter.toDtoList(entityList);

		// assert
		assertNotNull(resultList);
		assertEquals(1, resultList.getResultList().size());
		assertEquals(1L, resultList.getTotalElements());

		UserDto dto = resultList.getResultList().getFirst();
		assertEquals(1L, dto.getId());
		assertEquals("name", dto.getName());
		assertEquals(TestUtils.USERNAME, dto.getUsername());

		assertEquals("a@b.com", dto.getEmail());
		assertEquals(EntityStatus.ACTIVE, dto.getStatus());
		assertEquals(UserRole.ROLE_USER, dto.getRole());
		assertEquals("2023-06-29T00:00Z", dto.getCreationDate().toString());
	}

	@Test
	void toUserInfoDto_whenInputProvidedWithoutMfa_thenReturnOk() {
		// act
		UserInfoDto dto = converter.toUserInfoDto(TestUtils.createGmsUser(), false);

		// assert
		assertNotNull(dto);
		assertEquals(DemoData.USER_1_ID, dto.getId());
		assertEquals(DemoData.USERNAME1, dto.getName());
		assertEquals(DemoData.USERNAME1, dto.getUsername());
		assertEquals("a@b.com", dto.getEmail());
		assertEquals(UserRole.ROLE_USER, dto.getRole());
	}

	@Test
	void toUserInfoDto_whenInputProvidedWithMfa_thenReturnOk() {
		// arrange
		GmsUserDetails testUser = TestUtils.createGmsUser();
		testUser.setMfaEnabled(true);
		testUser.setMfaSecret("secret");

		// act
		UserInfoDto dto = converter.toUserInfoDto(testUser, true);

		// assert
		assertNotNull(dto);
		assertEquals("UserInfoDto(id=null, name=null, username=username1, email=null, role=null, status=null, failedAttempts=null)", dto.toString());
	}

	@Test
	void addIdToUserDetails_whenInputProvided_thenReturnOk() {
		// arrange
		GmsUserDetails testUser = TestUtils.createGmsUser();
		testUser.setUserId(null);

		// act
		GmsUserDetails response = converter.addIdToUserDetails(testUser, 1L);

		// assert
		assertNotNull(response);
		assertEquals(1L, response.getUserId());
	}
}
