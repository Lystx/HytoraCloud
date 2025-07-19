package cloud.hytora.modules.dashboard.intern.base;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

public class PasswordManager {

    private SecretKeyFactory factory;

    {
        try {
            factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public byte[] generateHash(String password, byte[] salt) throws InvalidKeySpecException {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
        return factory.generateSecret(spec).getEncoded();
    }

    public boolean verify(String password, byte[] salt, byte[] hash) throws InvalidKeySpecException {
        return Arrays.equals(generateHash(password, salt), hash);
    }

}
