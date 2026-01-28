package io.orchestra.cloud.infra.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
@Slf4j
public class CryptoService {

    @org.springframework.beans.factory.annotation.Value("${orchestra.security.crypto-key}")
    private String SECRET_KEY;

    public String encrypted (String data){

        try {
            SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");

            cipher.init(Cipher.ENCRYPT_MODE, key);

            return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));

        } catch (InvalidKeyException
                 | NoSuchPaddingException
                 | NoSuchAlgorithmException
                 | IllegalBlockSizeException
                 | BadPaddingException e
        ) {
            throw new RuntimeException(e);
        }
    }

    public String descrypt(String data){

        if (data == null || data.isEmpty()) {
            return null;
        }

        try {
            SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);

            byte[] decodedBytes = Base64.getDecoder().decode(data);

            return new String(cipher.doFinal(decodedBytes));

        } catch (IllegalArgumentException e) {
            log.warn("WARN: Texto não é Base64 válido. Usando valor original.");
            return data;

        } catch (Exception e) {
            log.warn("WARN: Falha ao descriptografar. Usando valor original.");
            return data;
        }
    }
}
