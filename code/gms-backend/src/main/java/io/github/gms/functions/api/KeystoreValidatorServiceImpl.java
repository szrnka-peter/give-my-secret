package io.github.gms.functions.api;

import io.github.gms.functions.api.KeystoreValidatorService;
import io.github.gms.common.enums.EntityStatus;
import io.github.gms.common.types.GmsException;
import io.github.gms.functions.keystore.KeystoreAliasEntity;
import io.github.gms.functions.secret.SecretEntity;
import io.github.gms.functions.keystore.KeystoreAliasRepository;
import io.github.gms.functions.keystore.KeystoreRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author Peter Szrnka
 * @since 1.0
 */
@Slf4j
@Service
public class KeystoreValidatorServiceImpl implements KeystoreValidatorService {

    private final KeystoreRepository keystoreRepository;
    private final KeystoreAliasRepository keystoreAliasRepository;

    public KeystoreValidatorServiceImpl(
            KeystoreRepository keystoreRepository,
            KeystoreAliasRepository keystoreAliasRepository
    ) {
        this.keystoreRepository = keystoreRepository;
        this.keystoreAliasRepository = keystoreAliasRepository;
    }

    @Override
    public void validateSecretKeystore(SecretEntity secretEntity) {
        KeystoreAliasEntity aliasEntity = keystoreAliasRepository.findById(secretEntity.getKeystoreAliasId()).orElseThrow(() -> {
            log.warn("Keystore alias not found");
            return new GmsException("Keystore alias is not available!");
        });

        if (keystoreRepository
                .findByIdAndUserIdAndStatus(aliasEntity.getKeystoreId(), secretEntity.getUserId(), EntityStatus.ACTIVE)
                .isEmpty()) {
            log.warn("Keystore is not active");
            throw new GmsException("Invalid keystore!");
        }
    }
}