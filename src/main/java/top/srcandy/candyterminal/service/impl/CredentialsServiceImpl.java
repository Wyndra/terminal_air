package top.srcandy.candyterminal.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.srcandy.candyterminal.dao.UserDao;
import top.srcandy.candyterminal.mapper.CredentialsMapper;
import top.srcandy.candyterminal.model.Credential;
import top.srcandy.candyterminal.model.User;
import top.srcandy.candyterminal.service.CredentialsService;
import top.srcandy.candyterminal.utils.JWTUtil;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateCrtKey;

@Service
@Slf4j
public class CredentialsServiceImpl implements CredentialsService {

    @Autowired
    private CredentialsMapper credentialsMapper;

    @Autowired
    private UserDao userDao;

    @Override
    public Credential generateKeyPair(String token, Long connectId, String name, String passphrase) throws Exception {
        User user = userDao.selectByUserName(JWTUtil.getTokenClaimMap(token).get("username").asString());
        Long userId = user.getUid();

        // 生成 3072 位 RSA 密钥对
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(3072, new SecureRandom());
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        PublicKey publicKey = keyPair.getPublic();
        RSAPrivateCrtKey privateKey = (RSAPrivateCrtKey) keyPair.getPrivate();

        // 生成 OpenSSH 格式公钥（系统保管）
        String publicKeyPem = encodePublicKey(privateKey, user.getUsername());
        // 生成未加密的 PKCS#1 格式私钥（返回给用户）
        String privateKeyPem = encodePrivateKeyToPKCS1Pem(privateKey);

        // 调试：打印关键参数
        log.info("Public Exponent (e): {}", privateKey.getPublicExponent().toString(16));
        log.info("Modulus (n): {}", privateKey.getModulus().toString(16).substring(0, 20) + "...");
        log.info("公钥已生成：{}", publicKeyPem);
        log.info("私钥已生成，长度：{}", privateKeyPem.length());

        Credential credential = new Credential();
        credential.setUserId(userId);
        credential.setConnectId(connectId);
        credential.setName(name);
        credential.setPublicKey(publicKeyPem);
        credential.setPrivateKey(null); // 不存储私钥

        // 可选：计算指纹
        String fingerprint = calculateFingerprint(publicKey);
        log.info("密钥指纹：{}", fingerprint);

        // 临时存储私钥以便返回
        credential.setPrivateKey(privateKeyPem);
        return credential;
    }

    // 将公钥编码为 OpenSSH 格式
    private static String encodePublicKey(RSAPrivateCrtKey privateKey, String username) throws Exception {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(byteOut);

        // 写入 "ssh-rsa"
        byte[] sshRsa = "ssh-rsa".getBytes(StandardCharsets.UTF_8);
        dataOut.writeInt(sshRsa.length);
        dataOut.write(sshRsa);

        // 写入公钥指数 e (mpint)
        byte[] eBytes = privateKey.getPublicExponent().toByteArray();
        if (eBytes[0] == 0 && eBytes.length > 1) { // 移除多余的 0
            byte[] trimmed = new byte[eBytes.length - 1];
            System.arraycopy(eBytes, 1, trimmed, 0, trimmed.length);
            eBytes = trimmed;
        }
        if ((eBytes[0] & 0x80) != 0) { // 如果最高位为 1，添加前导 0
            byte[] padded = new byte[eBytes.length + 1];
            System.arraycopy(eBytes, 0, padded, 1, eBytes.length);
            eBytes = padded;
        }
        dataOut.writeInt(eBytes.length);
        dataOut.write(eBytes);

        // 写入模数 n (mpint)
        byte[] nBytes = privateKey.getModulus().toByteArray();
        if (nBytes[0] == 0 && nBytes.length > 1) { // 移除多余的 0
            byte[] trimmed = new byte[nBytes.length - 1];
            System.arraycopy(nBytes, 1, trimmed, 0, trimmed.length);
            nBytes = trimmed;
        }
        if ((nBytes[0] & 0x80) != 0) { // 如果最高位为 1，添加前导 0
            byte[] padded = new byte[nBytes.length + 1];
            System.arraycopy(nBytes, 0, padded, 1, nBytes.length);
            nBytes = padded;
        }
        dataOut.writeInt(nBytes.length);
        dataOut.write(nBytes);

        byte[] pubKeyBytes = byteOut.toByteArray();
        String base64Key = Base64.toBase64String(pubKeyBytes);
        log.info("公钥 Base64: {}", base64Key); // 调试输出
        return "ssh-rsa " + base64Key + " " + username;
    }

    // 将私钥编码为未加密的 PKCS#1 PEM 格式
    private static String encodePrivateKeyToPKCS1Pem(RSAPrivateCrtKey privateKey) throws Exception {
        ByteArrayOutputStream derOut = new ByteArrayOutputStream();
        DataOutputStream derDataOut = new DataOutputStream(derOut);

        derDataOut.write(0x30); // SEQUENCE 标签
        ByteArrayOutputStream seqOut = new ByteArrayOutputStream();
        DataOutputStream seqDataOut = new DataOutputStream(seqOut);

        writeDERInteger(seqDataOut, BigInteger.ZERO); // version
        writeDERInteger(seqDataOut, privateKey.getModulus()); // n
        writeDERInteger(seqDataOut, privateKey.getPublicExponent()); // e
        writeDERInteger(seqDataOut, privateKey.getPrivateExponent()); // d
        writeDERInteger(seqDataOut, privateKey.getPrimeP()); // p
        writeDERInteger(seqDataOut, privateKey.getPrimeQ()); // q
        writeDERInteger(seqDataOut, privateKey.getPrimeExponentP()); // d mod (p-1)
        writeDERInteger(seqDataOut, privateKey.getPrimeExponentQ()); // d mod (q-1)
        writeDERInteger(seqDataOut, privateKey.getCrtCoefficient()); // iqmp

        byte[] seqBytes = seqOut.toByteArray();
        writeDERLength(derDataOut, seqBytes.length);
        derDataOut.write(seqBytes);

        String base64Key = Base64.toBase64String(derOut.toByteArray());
        StringBuilder pem = new StringBuilder();
        pem.append("-----BEGIN RSA PRIVATE KEY-----\n");
        for (int i = 0; i < base64Key.length(); i += 64) {
            pem.append(base64Key, i, Math.min(i + 64, base64Key.length())).append("\n");
        }
        pem.append("-----END RSA PRIVATE KEY-----\n");
        return pem.toString();
    }

    // 写入 DER 编码的 INTEGER
    private static void writeDERInteger(DataOutputStream out, BigInteger value) throws Exception {
        byte[] bytes = value.toByteArray();
        if (bytes[0] == 0 && bytes.length > 1) {
            byte[] trimmed = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, trimmed, 0, trimmed.length);
            bytes = trimmed;
        }
        out.write(0x02); // INTEGER 标签
        writeDERLength(out, bytes.length);
        out.write(bytes);
    }

    // 写入 DER 长度字段
    private static void writeDERLength(DataOutputStream out, int length) throws Exception {
        if (length < 128) {
            out.writeByte(length);
        } else {
            ByteArrayOutputStream lenOut = new ByteArrayOutputStream();
            while (length > 0) {
                lenOut.write(length & 0xFF);
                length >>= 8;
            }
            byte[] lenBytes = lenOut.toByteArray();
            out.writeByte(0x80 | lenBytes.length);
            for (int i = lenBytes.length - 1; i >= 0; i--) {
                out.writeByte(lenBytes[i]);
            }
        }
    }

    // 计算 SHA256 指纹
    private static String calculateFingerprint(PublicKey publicKey) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(byteOut);
        byte[] sshRsa = "ssh-rsa".getBytes(StandardCharsets.UTF_8);
        dataOut.writeInt(sshRsa.length);
        dataOut.write(sshRsa);
        byte[] eBytes = ((java.security.interfaces.RSAPublicKey) publicKey).getPublicExponent().toByteArray();
        if (eBytes[0] == 0 && eBytes.length > 1) {
            byte[] trimmed = new byte[eBytes.length - 1];
            System.arraycopy(eBytes, 1, trimmed, 0, trimmed.length);
            eBytes = trimmed;
        }
        if ((eBytes[0] & 0x80) != 0) {
            byte[] padded = new byte[eBytes.length + 1];
            System.arraycopy(eBytes, 0, padded, 1, eBytes.length);
            eBytes = padded;
        }
        dataOut.writeInt(eBytes.length);
        dataOut.write(eBytes);
        byte[] nBytes = ((java.security.interfaces.RSAPublicKey) publicKey).getModulus().toByteArray();
        if (nBytes[0] == 0 && nBytes.length > 1) {
            byte[] trimmed = new byte[nBytes.length - 1];
            System.arraycopy(nBytes, 1, trimmed, 0, trimmed.length);
            nBytes = trimmed;
        }
        if ((nBytes[0] & 0x80) != 0) {
            byte[] padded = new byte[nBytes.length + 1];
            System.arraycopy(nBytes, 0, padded, 1, nBytes.length);
            nBytes = padded;
        }
        dataOut.writeInt(nBytes.length);
        dataOut.write(nBytes);
        byte[] pubKeyBytes = byteOut.toByteArray();
        byte[] fingerprint = digest.digest(pubKeyBytes);
        return "SHA256:" + Base64.toBase64String(fingerprint).replaceAll("=+$", "");
    }

    // 获取私钥
    public String getPrivateKey(Credential credential) {
        return credential.getPrivateKey();
    }
}