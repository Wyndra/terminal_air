package top.srcandy.candyterminal.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.srcandy.candyterminal.dao.UserDao;
import top.srcandy.candyterminal.mapper.CredentialsMapper;
import top.srcandy.candyterminal.model.Credential;
import top.srcandy.candyterminal.model.User;
import top.srcandy.candyterminal.service.CredentialsService;
import top.srcandy.candyterminal.utils.JWTUtil;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Service
public class CredentialsServiceImpl implements CredentialsService {

    @Autowired
    private CredentialsMapper credentialsMapper;

    @Autowired
    private UserDao userDao;

    @Override
    public Credential generateKeyPair(String token, Long connectId, String name, String passphrase) throws Exception {
        // 获取用户信息
        User user = userDao.selectByUserName(JWTUtil.getTokenClaimMap(token).get("username").asString());
        Long userId = user.getUid();

        // 生成 RSA 密钥对
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048, new SecureRandom());
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        // 获取生成的公钥和私钥
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        // 将公钥和私钥转换为 PEM 格式
        String publicKeyPem = convertToPem(publicKey, "PUBLIC");

        // 使用 passphrase 加密私钥
        String privateKeyPem = convertToPem(privateKey, "PRIVATE");
        String encryptedPrivateKey = encryptPrivateKey(privateKey, passphrase);




        // 创建 Credential 对象
        Credential credential = new Credential();
        credential.setUserId(userId);
        credential.setConnectId(connectId);
        credential.setName(name);
        credential.setPublicKey(publicKeyPem);
        credential.setPrivateKey(encryptedPrivateKey); // 保存加密后的私钥

        // 保存到数据库
        credentialsMapper.insertCredential(credential);

        return credential;
    }

    // 将密钥转换为 PEM 格式
    private String convertToPem(Key key, String keyType) {
        String encodedKey = Base64.getEncoder().encodeToString(key.getEncoded());
        String header = "-----BEGIN " + keyType + " KEY-----";
        String footer = "-----END " + keyType + " KEY-----";

        // 添加换行符，每行64个字符
        StringBuilder sb = new StringBuilder();
        sb.append(header).append("\n");
        int lineLength = 64;
        for (int i = 0; i < encodedKey.length(); i += lineLength) {
            sb.append(encodedKey, i, Math.min(i + lineLength, encodedKey.length())).append("\n");
        }
        sb.append(footer);

        return sb.toString();
    }

    // 使用 passphrase 加密私钥
    private String encryptPrivateKey(PrivateKey privateKey, String passphrase) throws Exception {
        SecretKey secretKey = generateSecretKey(passphrase);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] encrypted = cipher.doFinal(privateKey.getEncoded());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    // 生成 AES 密钥
    private SecretKey generateSecretKey(String passphrase) throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256); // 使用 256 位 AES 加密
        return keyGenerator.generateKey();
    }

    // 解密私钥
    private PrivateKey decryptPrivateKey(String encryptedPrivateKey, String passphrase) throws Exception {
        SecretKey secretKey = generateSecretKey(passphrase);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedPrivateKey));

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decrypted);
        return keyFactory.generatePrivate(keySpec);
    }

    // 从数据库中读取并解密私钥
    public PrivateKey getPrivateKeyFromDb(String encryptedPrivateKey, String passphrase) throws Exception {
        return decryptPrivateKey(encryptedPrivateKey, passphrase);
    }
}
