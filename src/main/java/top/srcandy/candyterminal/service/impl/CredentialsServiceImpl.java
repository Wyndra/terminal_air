package top.srcandy.candyterminal.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.srcandy.candyterminal.dao.UserDao;
import top.srcandy.candyterminal.exception.ServiceException;
import top.srcandy.candyterminal.mapper.CredentialsMapper;
import top.srcandy.candyterminal.model.Credential;
import top.srcandy.candyterminal.model.User;
import top.srcandy.candyterminal.request.CredentialConnectionRequest;
import top.srcandy.candyterminal.request.CredentialStatusRequest;
import top.srcandy.candyterminal.service.CredentialsService;
import top.srcandy.candyterminal.utils.JWTUtil;
import top.srcandy.candyterminal.utils.KeyUtils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateCrtKey;
import java.sql.Timestamp;
import java.util.Base64;
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
        credential.setStatus(0);
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
    public Credential selectCredentialByUuid(String token, String uuid) throws Exception {
        User user = userDao.selectByUserName(JWTUtil.getTokenClaimMap(token).get("username").asString());
        Long userId = user.getUid();
        Credential credential = credentialsMapper.selectCredentialByUidAndUuid(userId, uuid);
        if (credential == null) {
            throw new ServiceException("凭据不存在");
        }
        return credential;
    }

    @Override
    public void updateCredentialStatus(String token, CredentialStatusRequest request) throws Exception {
        User user = userDao.selectByUserName(JWTUtil.getTokenClaimMap(token).get("username").asString());
        Long userId = user.getUid();
        Credential credential = credentialsMapper.selectCredentialByUidAndUuid(userId, request.getUuid());
        if (credential == null) {
            throw new ServiceException("凭据不存在");
        }
        credentialsMapper.updateCredentialStatus(request);
    }

    @Override
    public Credential updateCredentialConnectId(String token, CredentialConnectionRequest request) throws Exception {
        User user = userDao.selectByUserName(JWTUtil.getTokenClaimMap(token).get("username").asString());
        Long userId = user.getUid();
        Credential credential = credentialsMapper.selectCredentialByUidAndUuid(userId, request.getUuid());
        if (credential == null) {
            throw new ServiceException("凭据不存在");
        }
        // 如果凭据未与服务器绑定，则不允许更新连接ID
        if (credential.getStatus().equals(0)){
            throw new ServiceException("凭据未绑定");
        }
        credentialsMapper.updateCredentialConnectId(request);

        return credentialsMapper.selectCredentialByUidAndUuid(userId, request.getUuid());
    }


    @Override
    public String generateInstallShell(String token, String uuid, String endpoint) throws Exception {
        User user = userDao.selectByUserName(JWTUtil.getTokenClaimMap(token).get("username").asString());
        String publicKey = credentialsMapper.selectCredentialByUidAndUuid(user.getUid(), uuid).getPublicKey();

        // Base64 编码公钥，避免 Shell 解析问题
        String encodedPublicKey = Base64.getEncoder().encodeToString(publicKey.getBytes());

        return String.format("#!/bin/bash\n" +
                        "set -e\n" +
                        "\n" +
                        "RED='\\033[0;31m'\n" +
                        "GREEN='\\033[0;32m'\n" +
                        "BLUE='\\033[0;34m'\n" +
                        "NC='\\033[0m'\n" +
                        "\n" +
                        "UUID=\"%s\"\n" +
                        "TOKEN=\"%s\"\n" +
                        "ENCODED_PUBLIC_KEY=\"%s\"\n" +
                        "ENDPOINT=\"%s\"\n" +
                        "\n" +
                        "STATUS_URL=\"$ENDPOINT/api/credentials/get/status/%s\"\n" +
                        "AUTH_HEADER=\"Authorization: Bearer $TOKEN\"\n" +
                        "\n" +
                        "echo -e \"${BLUE}Checking credential status...${NC}\"\n" +
                        "RESPONSE=$(curl -s -X POST \"$STATUS_URL\" -H \"Content-Type: application/json\" -H \"$AUTH_HEADER\")\n" +
                        "\n" +
                        "# Extract 'data' field from JSON response\n" +
                        "if command -v jq >/dev/null 2>&1; then\n" +
                        "    DATA=$(echo \"$RESPONSE\" | jq -r '.data // empty')\n" +
                        "else\n" +
                        "    DATA=$(echo \"$RESPONSE\" | grep -o '\"data\":[0-9]*' | awk -F':' '{print $2}')\n" +
                        "fi\n" +
                        "\n" +
                        "# Validate DATA\n" +
                        "if [[ -z \"$DATA\" ]]; then\n" +
                        "    echo -e \"${RED}Error: Failed to retrieve credential status. Please check the API server or token validity.${NC}\"\n" +
                        "    echo -e \"  - Possible causes: API is down, incorrect URL, or expired token.\"\n" +
                        "    exit 1\n" +
                        "fi\n" +
                        "\n" +
                        "if ! [[ \"$DATA\" =~ ^[0-9]+$ ]]; then\n" +
                        "    echo -e \"${RED}Error: Invalid response format: 'data=$DATA'. Please check the API response structure.${NC}\"\n" +
                        "    exit 1\n" +
                        "fi\n" +
                        "\n" +
                        "if [[ \"$DATA\" -ne 0 ]]; then\n" +
                        "    echo -e \"${RED}Error: Credential status indicates it is already bound (data=$DATA). Aborting installation.${NC}\"\n" +
                        "    exit 1\n" +
                        "fi\n" +
                        "\n" +
                        "echo -e \"${GREEN}Credential status is valid. Proceeding with installation...${NC}\"\n" +
                        "\n" +
                        "OS_TYPE=$(uname)\n" +
                        "\n" +
                        "echo -e \"${BLUE}Decoding SSH public key...${NC}\"\n" +
                        "for i in {1..5}; do\n" +
                        "    echo -n \".\"\n" +
                        "    sleep 0.3\n" +
                        "done\n" +
                        "echo \"\"\n" +
                        "\n" +
                        "if [ \"$OS_TYPE\" == \"Darwin\" ]; then\n" +
                        "    PUBLIC_KEY=$(echo \"$ENCODED_PUBLIC_KEY\" | base64 -D)\n" +
                        "else\n" +
                        "    PUBLIC_KEY=$(echo \"$ENCODED_PUBLIC_KEY\" | base64 -d)\n" +
                        "fi\n" +
                        "\n" +
                        "if [ \"$EUID\" -eq 0 ]; then\n" +
                        "    TARGET_HOME=\"/root\"\n" +
                        "else\n" +
                        "    TARGET_HOME=\"$HOME\"\n" +
                        "fi\n" +
                        "\n" +
                        "SSH_DIR=\"$TARGET_HOME/.ssh\"\n" +
                        "AUTHORIZED_KEYS=\"$SSH_DIR/authorized_keys\"\n" +
                        "\n" +
                        "if [ ! -d \"$SSH_DIR\" ]; then\n" +
                        "    echo -e \"${RED}Error: SSH directory ($SSH_DIR) does not exist.${NC}\"\n" +
                        "    exit 1\n" +
                        "fi\n" +
                        "\n" +
                        "if [ ! -f \"$AUTHORIZED_KEYS\" ]; then\n" +
                        "    echo -e \"${RED}Error: SSH authorized_keys file not found at $AUTHORIZED_KEYS.${NC}\"\n" +
                        "    exit 1\n" +
                        "fi\n" +
                        "\n" +
                        "echo \"$PUBLIC_KEY\" >> \"$AUTHORIZED_KEYS\"\n" +
                        "\n" +
                        "echo -e \"${GREEN}SSH key installed successfully.${NC}\"\n" +
                        "echo -e \"${GREEN}Public key added to: ${AUTHORIZED_KEYS}${NC}\"\n" +
                        "echo -e \"${GREEN}You can now SSH into the system using the private key.${NC}\"\n" +
                        "\n" +
                        "# Update API server credential status\n" +
                        "UPDATE_URL=\"$ENDPOINT/api/credentials/update/status\"\n" +
                        "JSON_DATA=\"{\\\"uuid\\\":\\\"$UUID\\\",\\\"status\\\":1}\"\n" +
                        "AUTH_HEADER=\"Authorization: Bearer $TOKEN\"\n" +
                        "\n" +
                        "echo -e \"${BLUE}Updating credentials status...${NC}\"\n" +
                        "RESPONSE=$(curl -s -o /dev/null -w \"%%{http_code}\" -X POST \"$UPDATE_URL\" \\\n" +
                        "    -H \"Content-Type: application/json\" \\\n" +
                        "    -H \"$AUTH_HEADER\" \\\n" +
                        "    -d \"$JSON_DATA\")\n" +
                        "\n" +
                        "if [ \"$RESPONSE\" -eq 200 ]; then\n" +
                        "    echo -e \"${GREEN}Credentials status updated successfully.${NC}\"\n" +
                        "else\n" +
                        "    echo -e \"${RED}Failed to update credentials status. HTTP Code: $RESPONSE. Please check the API server.${NC}\"\n" +
                        "fi\n",
                uuid, token, encodedPublicKey, endpoint, uuid);
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