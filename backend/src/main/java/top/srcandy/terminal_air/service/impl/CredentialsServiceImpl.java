package top.srcandy.terminal_air.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.srcandy.terminal_air.pojo.vo.CredentialVo;
import top.srcandy.terminal_air.converter.CredentialConverter;
import top.srcandy.terminal_air.exception.ServiceException;
import top.srcandy.terminal_air.mapper.CredentialsMapper;
import top.srcandy.terminal_air.mapper.UserMapper;
import top.srcandy.terminal_air.pojo.model.Credential;
import top.srcandy.terminal_air.pojo.model.User;
import top.srcandy.terminal_air.request.CredentialConnectionRequest;
import top.srcandy.terminal_air.request.CredentialStatusRequest;
import top.srcandy.terminal_air.request.CredentialStatusShortTokenRequest;
import top.srcandy.terminal_air.service.CredentialsService;
import top.srcandy.terminal_air.service.RedisService;
import top.srcandy.terminal_air.utils.*;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class CredentialsServiceImpl implements CredentialsService {

    @Autowired
    private CredentialsMapper credentialsMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private CredentialConverter credentialConverter;

    @Autowired
    private UserMapper userMapper;

    @Override
    public CredentialVo generateKeyPair(String name, String tags) throws ServiceException, Exception {
        String username = SecuritySessionUtils.getUsername();
        Long userId = SecuritySessionUtils.getUserId();

        // 检查凭据名称是否已存在
        if (credentialsMapper.countCredentialsByUserIdAndName(userId, name) > 0) {
            throw new ServiceException("凭据名称已存在");
        }
        // 最大允许凭据数量
        int MAX_CREDENTIALS = 10;
        if (credentialsMapper.countCredentialsByUserId(userId) >= MAX_CREDENTIALS) {
            throw new ServiceException("凭据数量已达上限");
        }

        RSAKeyUtils.KeyPairString pair = RSAKeyUtils.generateSSHKeyPair(username);
        KeyUtils.calculateFingerprint(pair.getPublicKey());

        String fingerprint = KeyUtils.calculateFingerprint(pair.getPublicKey());

        Credential credential = Credential.builder()
                .userId(userId)
                .uuid(KeyUtils.generateUUID())
                .name(name)
                .publicKey(pair.getPublicKey())
                .status(0)
                .privateKey(pair.getPrivateKey())
                .createTime(new Timestamp(System.currentTimeMillis()))
                .updateTime(new Timestamp(System.currentTimeMillis()))
                .tags(tags)
                .fingerprint(fingerprint)
                .build();

        log.info("用户 {} 创建了凭据 {},凭据指纹:{}", username, credential.getUuid(),fingerprint);
        credentialsMapper.insertCredential(credential);
        return credentialConverter.credential2VO(credential);
    }

    @Override
    public List<CredentialVo> listCredentials() {
        log.info("用户 {} 获取凭据列表", SecuritySessionUtils.getUserId());
        Long userId = SecuritySessionUtils.getUserId();
        return credentialConverter.credentialList2VOList(credentialsMapper.selectCredentialsByUserId(userId));
    }

    @Override
    public int countCredentialsByUserId() {
        return credentialsMapper.countCredentialsByUserId(SecuritySessionUtils.getUserId());
    }

    @Override
    public Credential selectCredentialById(Long id) throws Exception {
        Long userId = SecuritySessionUtils.getUserId();
        return Optional.ofNullable(credentialsMapper.selectCredentialByUidAndId(userId, id))
                .orElseThrow(() -> new ServiceException("凭据不存在"));
    }

    @Override
    public Credential selectCredentialByUuid(String uuid) throws Exception {
        Long userId = SecuritySessionUtils.getUserId();
        return Optional.ofNullable(credentialsMapper.selectCredentialByUidAndUuid(userId, uuid))
                .orElseThrow(() -> new ServiceException("凭据不存在"));
    }

    @Override
    public Credential getCredentialByUuidSkipAuth(String uuid) throws Exception {
        return Optional.ofNullable(credentialsMapper.selectCredentialByUuid(uuid))
                .orElseThrow(() -> new ServiceException("凭据不存在"));
    }

    @Override
    public void updateCredentialStatus(CredentialStatusRequest request) throws Exception {
        Long userId = SecuritySessionUtils.getUserId();
        Optional.ofNullable(credentialsMapper.selectCredentialByUidAndUuid(userId, request.getUuid()))
                .orElseThrow(() -> new ServiceException("凭据不存在"));
        credentialsMapper.updateCredentialStatus(request);
    }

    @Override
    public void updateCredentialStatusByShortToken(CredentialStatusShortTokenRequest request) throws Exception {
        String token = request.getShort_token();
        String redisKey = "install_shell_token_" + token;
        String redisValue = redisService.get(redisKey);
        if (redisValue == null) {
            log.error("短令牌已失效或不存在");
            throw new ServiceException("短令牌已失效或不存在");
        }
        String[] redisParts = redisValue.split(":");
        User user = userMapper.selectByUserName(redisParts[0]);
        Long userId = user.getUid();
        log.info("用户 {} 更新凭据状态，UUID: {}", user.getUsername(), request.getUuid());
        if (request.getUuid().equals(redisParts[1])) {
            log.info("短令牌UUID与凭据UUID一致");
        } else {
            log.info("验证失败，短令牌UUID与凭据UUID不一致");
            throw new ServiceException("短令牌无权限操作该凭据");
        }
        Optional.ofNullable(credentialsMapper.selectCredentialByUidAndUuid(userId, request.getUuid()))
                .orElseThrow(() -> new ServiceException("凭据不存在"));
        credentialsMapper.updateCredentialStatusByParams(request.getUuid(),request.getStatus());
    }

    @Override
    public CredentialVo updateCredentialConnectId(CredentialConnectionRequest request) throws Exception {
        Long userId = SecuritySessionUtils.getUserId();
        Credential credential = credentialsMapper.selectCredentialByUidAndUuid(userId, request.getUuid());
        if (credential == null) {
            throw new ServiceException("凭据不存在");
        }
        // 如果凭据未与服务器绑定，则不允许更新连接ID
        if (credential.getStatus().equals(0)) {
            throw new ServiceException("凭据未绑定");
        }
        credentialsMapper.updateCredentialConnectId(request);

        return credentialConverter.credential2VO(credentialsMapper.selectCredentialByUidAndUuid(userId, request.getUuid()));
    }


    @Override
    public String getInstallShellUrl(String endpoint, String uuid) throws Exception {
        User user = SecuritySessionUtils.getUser();

        // 生成随机 token
        String randomToken = SaltUtils.generateSalt(10);
        String key = "install_shell_token_" + randomToken;
        String value = user.getUsername() + ":" + uuid;
        redisService.set(key, value, 10, TimeUnit.MINUTES);

        log.info("用户 {} 生成了安装脚本下载链接，UUID: {}, 预签名token: {}", user.getUsername(), uuid,randomToken);
        String extraMessage = String.format("%s;%s", endpoint, randomToken);
        String extra = Base64.getEncoder().encodeToString(extraMessage.getBytes(StandardCharsets.UTF_8));
        String link = String.format("%s/api/credentials/installation/%s?extra=%s", endpoint, uuid, extra);
        log.info("安装脚本下载链接: {}", link);
        return link;
    }

    @Override
    public String generateInstallShell(String uuid,String extra) throws Exception {
        // extra是Base64编码的字符串，解码后是 endpoint;token
        String extraInfo = new String(Base64.getDecoder().decode(extra), StandardCharsets.UTF_8);
        String[] extraParts = extraInfo.split(";");
        String token = extraParts[1];
        String endpoint = extraParts[0];
        log.info("预签名token:{}", token);
        String redisKey = "install_shell_token_" + token;
        String redisValue = redisService.get(redisKey);
        if (redisValue == null) {
            log.error("安装脚本下载链接已失效");
            throw new ServiceException("安装脚本下载链接已失效");
        }
        String[] redisParts = redisValue.split(":");
        User user = userMapper.selectByUserName(redisParts[0]);
        String publicKey = credentialsMapper.selectCredentialByUidAndUuid(user.getUid(), uuid).getPublicKey();
        String encodedPublicKey = Base64.getEncoder().encodeToString(publicKey.getBytes());

        // 加载脚本模板
        String scriptFilePath = "templates/install_template.sh";
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(scriptFilePath);
        if (inputStream == null) {
            log.error("{} 脚本模板未找到",scriptFilePath);
            throw new FileNotFoundException("Shell script template not found.");
        }

        String template = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        // 替换占位符
        return template
                .replace("{{UUID}}", uuid)
                .replace("{{TOKEN}}", token)
                .replace("{{ENCODED_PUBLIC_KEY}}", encodedPublicKey)
                .replace("{{ENDPOINT}}", endpoint);
    }


    @Override
    public List<CredentialVo> selectBoundCredentialsByConnectionId(String uuid) throws Exception {
        Long userId = SecuritySessionUtils.getUserId();
        List<Credential> credentials = credentialsMapper.selectBoundCredentialsByConnectionUuid(userId, uuid);
        return credentialConverter.credentialList2VOList(credentials);
    }


    @Override
    public void deleteCredential(String uuid) throws Exception {
        Long userId = SecuritySessionUtils.getUserId();
        log.info("用户ID {} 删除了 {} 的凭据", userId, uuid);
        Credential credential = credentialsMapper.selectCredentialsByUserId(userId).stream().filter(c -> c.getUuid().equals(uuid)).findFirst().orElse(null);
        if (credential == null) {
            throw new ServiceException("凭据不存在");
        }
        credentialsMapper.deleteCredentialByUuid(credential.getUuid());
    }
}