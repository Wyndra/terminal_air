package top.srcandy.candyterminal.utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AESUtil {

    // 默认的 AES 密钥长度
    private static final int KEY_SIZE = 128;

    // 使用 AES 算法进行加密
    public static String encrypt(String data, String key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        SecretKeySpec secretKeySpec = new SecretKeySpec(getKey(key), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    // 使用 AES 算法进行解密
    public static String decrypt(String encryptedData, String key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        SecretKeySpec secretKeySpec = new SecretKeySpec(getKey(key), "AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        byte[] decryptedData = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedData, StandardCharsets.UTF_8);
    }

    // 生成 AES 密钥
    public static String generateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(KEY_SIZE);
        SecretKey secretKey = keyGen.generateKey();
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    // 将密钥转换为 byte 数组
    private static byte[] getKey(String key) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] keyBytesPadded = new byte[16]; // AES-128 使用 16 字节密钥
        System.arraycopy(keyBytes, 0, keyBytesPadded, 0, Math.min(keyBytes.length, keyBytesPadded.length));
        return keyBytesPadded;
    }

    public static void main(String[] args) throws Exception {
        String originalData = "Hello, World!";
        String secretKey = AESUtil.generateKey();

        System.out.println("Original Data: " + originalData);
        System.out.println("Secret Key: " + secretKey);

        String encryptedData = AESUtil.encrypt(originalData, secretKey);
        System.out.println("Encrypted Data: " + encryptedData);

        String decryptedData = AESUtil.decrypt(encryptedData, secretKey);
        System.out.println("Decrypted Data: " + decryptedData);
    }
}
