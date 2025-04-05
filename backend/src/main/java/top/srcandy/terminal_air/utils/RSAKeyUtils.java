package top.srcandy.terminal_air.utils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

import java.io.*;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

public class RSAKeyUtils {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static class KeyPairString {
        private final String privateKey; // PEM格式（RSA PRIVATE KEY）
        private final String publicKey;  // OpenSSH格式

        public KeyPairString(String privateKey, String publicKey) {
            this.privateKey = privateKey;
            this.publicKey = publicKey;
        }

        public String getPrivateKey() {
            return privateKey;
        }

        public String getPublicKey() {
            return publicKey;
        }
    }

    public static KeyPairString generateSSHKeyPair(String comment) throws Exception {
        System.out.println("➡️ 开始生成 RSA 密钥对...");

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048); // 推荐2048或更高
        KeyPair keyPair = keyGen.generateKeyPair();

        // 私钥（PEM格式）
        PrivateKey privateKey = keyPair.getPrivate();
        String pemPrivateKey = convertPrivateKeyToPEM(privateKey);

        // 公钥（OpenSSH格式）
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        String sshPublicKey = convertPublicKeyToOpenSSH(publicKey, comment);

        System.out.println("✅ 密钥对生成成功");
        return new KeyPairString(pemPrivateKey, sshPublicKey);
    }

    // 私钥转为PEM格式（兼容JSch）
    private static String convertPrivateKeyToPEM(PrivateKey privateKey) throws Exception {
        StringWriter writer = new StringWriter();
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(writer)) {
            pemWriter.writeObject(privateKey);
        }
        return writer.toString();
    }

    // 公钥转为OpenSSH格式（兼容SSH服务器）
    private static String convertPublicKeyToOpenSSH(RSAPublicKey publicKey, String comment) {
        // 构造OpenSSH公钥格式：ssh-rsa BASE64(e + n) comment
        byte[] sshHeader = "ssh-rsa".getBytes();
        byte[] eBytes = publicKey.getPublicExponent().toByteArray();
        byte[] nBytes = publicKey.getModulus().toByteArray();

        // 拼接SSH公钥二进制数据（需按SSH协议格式）
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            writeSSHString(bos, sshHeader); // 写入算法标识（ssh-rsa）
            writeSSHBytes(bos, eBytes);     // 写入公钥指数e
            writeSSHBytes(bos, nBytes);     // 写入模数n
        } catch (IOException e) {
            throw new RuntimeException("公钥编码失败", e);
        }

        // 转换为Base64并拼接最终格式
        String base64 = Base64.getEncoder().encodeToString(bos.toByteArray());
        return String.format("ssh-rsa %s %s", base64, comment);
    }

    // 辅助方法：写入SSH格式的字符串长度和内容
    private static void writeSSHString(OutputStream out, byte[] data) throws IOException {
        writeSSHInt(out, data.length);
        out.write(data);
    }

    // 辅助方法：写入SSH格式的字节数组长度和内容
    private static void writeSSHBytes(OutputStream out, byte[] data) throws IOException {
        writeSSHInt(out, data.length);
        out.write(data);
    }

    // 辅助方法：写入SSH格式的整数（4字节大端序）
    private static void writeSSHInt(OutputStream out, int value) throws IOException {
        out.write((value >> 24) & 0xFF);
        out.write((value >> 16) & 0xFF);
        out.write((value >> 8) & 0xFF);
        out.write(value & 0xFF);
    }
}