import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

public class StringEncryptor {

    // Minimum key length
    private static final int MIN_KEY_LENGTH = 16; // AES-128 requires a 16-byte key

    // Encrypt a string using AES encryption with a key
    public static String encrypt(String input, String key) {
        try {
            // Create a valid key by padding or truncating the input key to MIN_KEY_LENGTH
            byte[] keyBytes = key.getBytes();
            byte[] validKeyBytes = new byte[MIN_KEY_LENGTH];
            System.arraycopy(keyBytes, 0, validKeyBytes, 0, Math.min(keyBytes.length, MIN_KEY_LENGTH));
            Key secretKey = new SecretKeySpec(validKeyBytes, "AES");

            // Create an AES cipher
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            // Encrypt the input string
            byte[] encryptedBytes = cipher.doFinal(input.getBytes());

            // Encode the encrypted bytes as a base64 string
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            return input; // Return the input as is in case of an error
        }
    }

    // Decrypt an encrypted string with the same key
    public static String decrypt(String encrypted, String key) {
        try {
            // Create a valid key by padding or truncating the input key to MIN_KEY_LENGTH
            byte[] keyBytes = key.getBytes();
            byte[] validKeyBytes = new byte[MIN_KEY_LENGTH];
            System.arraycopy(keyBytes, 0, validKeyBytes, 0, Math.min(keyBytes.length, MIN_KEY_LENGTH));
            Key secretKey = new SecretKeySpec(validKeyBytes, "AES");

            // Create an AES cipher
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            // Decode the base64 string back to bytes
            byte[] encryptedBytes = Base64.getDecoder().decode(encrypted);

            // Decrypt the bytes
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            return new String(decryptedBytes);
        } catch (Exception e) {
            return encrypted; // Return the encrypted input as is in case of an error
        }
    }
}
