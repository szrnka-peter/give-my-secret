package io.github.gms.common.db.converter;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.AttributeConverter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Peter Szrnka
 * @since 1.0
 */
@Component
public class EncryptedFieldConverter implements AttributeConverter<String, String> {

	private static final String AES_GCM_NO_PADDING = "AES/GCM/NoPadding";
	private static final String ALGORYTHM = "AES";

	private final String secret;
	private Key key;
	private final String encryptionIv;
	private final Cipher cipher;

	public EncryptedFieldConverter(
			@Value("${config.crypto.secret}") String secret, 
			@Value("${config.encryption.iv}") String encryptionIv) 
					throws NoSuchAlgorithmException, NoSuchPaddingException {
        this.secret = secret;
        this.encryptionIv = encryptionIv;
        cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
    }

	@Override
	public String convertToDatabaseColumn(String attribute) {
		try {
			key = new SecretKeySpec(Base64.getDecoder().decode(secret.getBytes()), ALGORYTHM);
			cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, encryptionIv.getBytes()));
			return Base64.getEncoder().encodeToString(cipher.doFinal(attribute.getBytes()));
		} catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String convertToEntityAttribute(String dbData) {
		try {
			key = new SecretKeySpec(Base64.getDecoder().decode(secret.getBytes()), ALGORYTHM);
			cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, encryptionIv.getBytes()));
			return new String(cipher.doFinal(Base64.getDecoder().decode(dbData)));
		} catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
			throw new IllegalStateException(e);
		}
	}
}
