package top.srcandy.candyterminal.utils;

import java.io.UnsupportedEncodingException;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AESUtils {
    // 保留默认密钥，支持调用时不指定密钥
    private static final String DEFAULT_AESKEY = "c2h0ZC4yMDE3LmFlc2tleQ==";

    public AESUtils() {}

    public static byte[] encrypt(String content, String password, int keySize) throws GeneralSecurityException, UnsupportedEncodingException {
        SecretKeySpec key = new SecretKeySpec(zeroPad(password.getBytes(), keySize), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec iv = new IvParameterSpec(zeroPad(password.getBytes(), 16));
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        return cipher.doFinal(content.getBytes("UTF-8"));
    }

    public static byte[] decrypt(byte[] content, String password, int keySize) throws GeneralSecurityException, UnsupportedEncodingException {
        SecretKeySpec key = new SecretKeySpec(zeroPad(password.getBytes(), keySize), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec iv = new IvParameterSpec(zeroPad(password.getBytes(), 16));
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        return cipher.doFinal(content);
    }

    public static String byteToHex(byte[] buf) {
        StringBuilder sb = new StringBuilder();
        for (byte b : buf) {
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    public static byte[] hexToByte(String hexStr) {
        int len = hexStr.length();
        byte[] result = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            result[i / 2] = (byte) ((Character.digit(hexStr.charAt(i), 16) << 4) + Character.digit(hexStr.charAt(i+1), 16));
        }
        return result;
    }

    public static byte[] zeroPad(byte[] in, int blockSize) {
        byte[] out = new byte[blockSize];
        System.arraycopy(in, 0, out, 0, Math.min(in.length, blockSize));
        return out;
    }

    // 支持传入自定义密钥的加密方法
    public static String encryptToHex(String content, String base64Key) throws GeneralSecurityException, UnsupportedEncodingException {
        return byteToHex(encrypt(content, new String(Base64.getDecoder().decode(base64Key)), 16));
    }

    // 支持传入自定义密钥的解密方法
    public static String decryptFromHex(String content, String base64Key) throws GeneralSecurityException, UnsupportedEncodingException {
        return new String(decrypt(hexToByte(content), new String(Base64.getDecoder().decode(base64Key)), 16), "UTF-8");
    }

    // 使用默认密钥进行加密
    public static String encryptWithDefaultKey(String content) throws GeneralSecurityException, UnsupportedEncodingException {
        return encryptToHex(content, DEFAULT_AESKEY);
    }

    // 使用默认密钥进行解密
    public static String decryptWithDefaultKey(String content) throws GeneralSecurityException, UnsupportedEncodingException {
        return decryptFromHex(content, DEFAULT_AESKEY);
    }

    public static void main(String[] args) throws GeneralSecurityException, UnsupportedEncodingException {
        // 示例：使用自定义密钥加解密
        String customKey = "1Htlnr9x5Jatcfgt";
//        String customEncrypted = encryptToHex("BA50AEDDA3E563BB366B030181C9197A", customKey);
//        System.out.println("Encrypted with custom key: " + customEncrypted);

        String customDecrypted = decryptFromHex("BA50AEDDA3E563BB366B030181C9197A", customKey);
        System.out.println("Decrypted with custom key: " + customDecrypted);
    }
}
