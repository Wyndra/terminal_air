package top.srcandy.candyterminal.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.srcandy.candyterminal.dao.UserDao;
import top.srcandy.candyterminal.mapper.CredentialsMapper;
import top.srcandy.candyterminal.model.Credential;
import top.srcandy.candyterminal.model.User;
import top.srcandy.candyterminal.service.CredentialsService;
import top.srcandy.candyterminal.utils.JWTUtil;
import top.srcandy.candyterminal.utils.KeyUtils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateCrtKey;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;

@Service
@Slf4j
public class CredentialsServiceImpl implements CredentialsService {

    @Autowired
    private CredentialsMapper credentialsMapper;

    @Autowired
    private UserDao userDao;

    @Override
    public Credential generateKeyPair(String token, String name,String tags) throws Exception {
        User user = userDao.selectByUserName(JWTUtil.getTokenClaimMap(token).get("username").asString());
        Long userId = user.getUid();

        // 生成 3072 位 RSA 密钥对
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(3072, new SecureRandom());
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        RSAPrivateCrtKey privateKey = (RSAPrivateCrtKey) keyPair.getPrivate();

        // 使用工具类生成密钥
        String publicKeyPem = KeyUtils.encodePublicKeyToOpenSSH(privateKey, user.getUsername());
        String privateKeyPem = KeyUtils.encodePrivateKeyToPKCS1Pem(privateKey);

        // 调试信息
        log.info("Public Exponent (e): {}", privateKey.getPublicExponent().toString(16));
        log.info("Modulus (n): {}", privateKey.getModulus().toString(16).substring(0, 20) + "...");
        log.info("公钥已生成：{}", publicKeyPem);
        log.info("私钥已生成，长度：{}", privateKeyPem.length());

        Credential credential = new Credential();
        credential.setUserId(userId);
        // 计算uuid 得到唯一值
        credential.setUuid(KeyUtils.generateUUID());
//        credential.setConnectId(connectId);
        credential.setName(name);
        credential.setFingerprint(KeyUtils.calculateFingerprint(keyPair.getPublic()));
        credential.setPublicKey(publicKeyPem);
        credential.setPrivateKey(privateKeyPem);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        credential.setCreateTime(timestamp);
        credential.setUpdateTime(timestamp);
        credential.setTags(tags);

        // 计算指纹
        String fingerprint = KeyUtils.calculateFingerprint(keyPair.getPublic());
        log.info("密钥指纹：{}", fingerprint);
        // 临时存储私钥以便返回
        credential.setPrivateKey(privateKeyPem);

        credentialsMapper.insertCredential(credential);
        return credential;
    }

    @Override
    public List<Credential> listCredentials(String token) throws Exception {
        User user = userDao.selectByUserName(JWTUtil.getTokenClaimMap(token).get("username").asString());
        Long userId = user.getUid();
        return credentialsMapper.selectCredentialsByUserId(userId);
    }

    @Override
    public void deleteCredential(String token, Long id) throws Exception {
        User user = userDao.selectByUserName(JWTUtil.getTokenClaimMap(token).get("username").asString());
        Long userId = user.getUid();
        Credential credential = credentialsMapper.selectCredentialsByUserId(userId).stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
        assert credential != null;
        credentialsMapper.deleteCredential(credential.getId());
    }
}