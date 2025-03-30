package top.srcandy.candyterminal.service.impl;

import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.srcandy.candyterminal.bean.vo.PageQueryResultVO;
import top.srcandy.candyterminal.dao.UserDao;
import top.srcandy.candyterminal.exception.ServiceException;
import top.srcandy.candyterminal.mapper.CredentialsMapper;
import top.srcandy.candyterminal.model.Credential;
import top.srcandy.candyterminal.model.User;
import top.srcandy.candyterminal.request.PageQueryRequest;
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
    public Credential generateKeyPair(String token, String name,String tags) throws ServiceException, Exception {
        User user = userDao.selectByUserName(JWTUtil.getTokenClaimMap(token).get("username").asString());
        Long userId = user.getUid();

        // 检查凭据名称是否已存在
        if (credentialsMapper.countCredentialsByUserIdAndName(userId, name) > 0) {
            throw new ServiceException("凭据名称已存在");
        }
        // 最大允许凭据数量
        int MAX_CREDENTIALS = 10;
        if (credentialsMapper.countCredentialsByUserId(userId) >= MAX_CREDENTIALS) {
            throw new ServiceException("凭据数量已达上限");
        }

        // 生成 3072 位 RSA 密钥对
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(3072, new SecureRandom());
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        RSAPrivateCrtKey privateKey = (RSAPrivateCrtKey) keyPair.getPrivate();

        // 使用工具类生成密钥
        String publicKeyPem = KeyUtils.encodePublicKeyToOpenSSH(privateKey, user.getUsername());
        String privateKeyPem = KeyUtils.encodePrivateKeyToPKCS1Pem(privateKey);

        Credential credential = new Credential();
        credential.setUserId(userId);
        credential.setUuid(KeyUtils.generateUUID());
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
    public List<Credential> listCredentials(String token) {
        User user = userDao.selectByUserName(JWTUtil.getTokenClaimMap(token).get("username").asString());
        Long userId = user.getUid();
        return credentialsMapper.selectCredentialsByUserId(userId);
    }

    @Override
    public int countCredentialsByUserId(String token) {
        User user = userDao.selectByUserName(JWTUtil.getTokenClaimMap(token).get("username").asString());
        return credentialsMapper.countCredentialsByUserId(user.getUid());
    }

    @Override
    public Credential selectCredentialById(String token, Long id) throws Exception {
        User user = userDao.selectByUserName(JWTUtil.getTokenClaimMap(token).get("username").asString());
        Long userId = user.getUid();
        Credential credential = credentialsMapper.selectCredentialByUidAndId(userId, id);
        if (credential == null) {
            throw new ServiceException("凭据不存在");
        }
        return credential;
    }

    @Override
    public void deleteCredential(String token, Long id) throws Exception {
        User user = userDao.selectByUserName(JWTUtil.getTokenClaimMap(token).get("username").asString());
        Long userId = user.getUid();
        Credential credential = credentialsMapper.selectCredentialsByUserId(userId).stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
        if (credential == null) {
            throw new ServiceException("凭据不存在");
        }
        credentialsMapper.deleteCredential(credential.getId());
    }
}