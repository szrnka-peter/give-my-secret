package io.github.gms.auth.ldap;

import static io.github.gms.common.util.Constants.CONFIG_AUTH_TYPE_LDAP;
import static io.github.gms.common.util.Constants.LDAP_CRYPT_PREFIX;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import io.github.gms.auth.model.GmsUserDetails;
import io.github.gms.common.enums.EntityStatus;
import io.github.gms.secure.entity.UserEntity;
import io.github.gms.secure.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Peter Szrnka
 * @since 1.0
 */
@Slf4j
@Service
@Profile(value = { CONFIG_AUTH_TYPE_LDAP })
public class LdapUserPersistenceServiceImpl implements LdapUserPersistenceService {

    private final Clock clock;
    private final UserRepository repository;
    private final boolean storeLdapCredential;

    public LdapUserPersistenceServiceImpl(Clock clock, UserRepository repository, @Value("${config.store.ldap.credential:false}") boolean storeLdapCredential) {
        this.clock = clock;
        this.repository = repository;
        this.storeLdapCredential = storeLdapCredential;
    }

    @Override
    public GmsUserDetails saveUserIfRequired(String username, GmsUserDetails foundUser) {
        repository.findByUsername(username).ifPresentOrElse(userEntity -> saveExistingUser(foundUser, userEntity),
				() -> saveNewUser(foundUser));

        return foundUser;
    }

    private void saveExistingUser(GmsUserDetails foundUser, UserEntity userEntity) {
		foundUser.setUserId(userEntity.getId());

		if (storeLdapCredential && !userEntity.getCredential().equals(foundUser.getCredential())) {
			userEntity.setCredential(getCredential(foundUser));
			repository.save(userEntity);
			log.info("Credential has been updated for user={}", foundUser.getUsername());
		}
	}

    private void saveNewUser(GmsUserDetails foundUser) {
		UserEntity userEntity = new UserEntity();
		userEntity.setStatus(EntityStatus.ACTIVE);
		userEntity.setName(foundUser.getName());
		userEntity.setUsername(foundUser.getUsername());
		userEntity.setCredential(getCredential(foundUser));
		userEntity.setCreationDate(ZonedDateTime.now(clock));
		userEntity.setEmail(foundUser.getEmail());
		userEntity.setRoles(foundUser.getAuthorities().stream().map(GrantedAuthority::getAuthority)
				.collect(Collectors.joining(",")));
		userEntity.setMfaEnabled(foundUser.isMfaEnabled());
		userEntity.setMfaSecret(foundUser.getMfaSecret());
		userEntity = repository.save(userEntity);

		foundUser.setUserId(userEntity.getId());
		log.info("User data has been saved into DB for user={}", foundUser.getUsername());
	}

    private String getCredential(GmsUserDetails foundUser) {
		return storeLdapCredential ? foundUser.getCredential().replace(LDAP_CRYPT_PREFIX, "")
				: "*PROVIDED_BY_LDAP*";
	}
}