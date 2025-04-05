package top.srcandy.terminal_air.utils;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Base64;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.interfaces.RSAPrivateCrtKey;
import java.util.UUID;
import java.io.ByteArrayInputStream;

@Slf4j
public class KeyUtils {

    /**
     * 将 RSA 公钥编码为 OpenSSH 格式（ssh-rsa ...）
     */
    public static String encodePublicKeyToOpenSSH(RSAPrivateCrtKey privateKey, String username) throws Exception {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(byteOut);

        byte[] sshRsa = "ssh-rsa".getBytes(StandardCharsets.UTF_8);
        dataOut.writeInt(sshRsa.length);
        dataOut.write(sshRsa);

        byte[] eBytes = privateKey.getPublicExponent().toByteArray();
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

        byte[] nBytes = privateKey.getModulus().toByteArray();
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
        String base64Key = Base64.toBase64String(pubKeyBytes);
        log.info("公钥 Base64: {}", base64Key);
        return "ssh-rsa " + base64Key + " " + username;
    }

    /**
     * 将 RSA 私钥编码为未加密的 PKCS#1 PEM 格式
     */
    public static String encodePrivateKeyToPKCS1Pem(RSAPrivateCrtKey privateKey) throws Exception {
        ByteArrayOutputStream derOut = new ByteArrayOutputStream();
        DataOutputStream derDataOut = new DataOutputStream(derOut);

        derDataOut.write(0x30);
        ByteArrayOutputStream seqOut = new ByteArrayOutputStream();
        DataOutputStream seqDataOut = new DataOutputStream(seqOut);

        writeDERInteger(seqDataOut, BigInteger.ZERO);
        writeDERInteger(seqDataOut, privateKey.getModulus());
        writeDERInteger(seqDataOut, privateKey.getPublicExponent());
        writeDERInteger(seqDataOut, privateKey.getPrivateExponent());
        writeDERInteger(seqDataOut, privateKey.getPrimeP());
        writeDERInteger(seqDataOut, privateKey.getPrimeQ());
        writeDERInteger(seqDataOut, privateKey.getPrimeExponentP());
        writeDERInteger(seqDataOut, privateKey.getPrimeExponentQ());
        writeDERInteger(seqDataOut, privateKey.getCrtCoefficient());

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

    /**
     * 计算公钥的 SHA-256 指纹
     */
    public static String calculateFingerprint(String publicKeyString) throws Exception {
        // 解析公钥字符串，提取 Base64 部分
        String base64Part = extractBase64FromPublicKey(publicKeyString);
        byte[] decoded = Base64.decode(base64Part);

        // 解析二进制数据，获取 e 和 n 的字节数组
        byte[] eBytes = parseE(decoded);
        byte[] nBytes = parseN(decoded);

        // 处理 e 和 n 的字节数组（与原代码逻辑相同）
        eBytes = adjustBytes(eBytes);
        nBytes = adjustBytes(nBytes);

        // 构建数据流并计算指纹
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(byteOut);

        byte[] sshRsa = "ssh-rsa".getBytes(StandardCharsets.UTF_8);
        dataOut.writeInt(sshRsa.length);
        dataOut.write(sshRsa);

        dataOut.writeInt(eBytes.length);
        dataOut.write(eBytes);

        dataOut.writeInt(nBytes.length);
        dataOut.write(nBytes);

        byte[] pubKeyBytes = byteOut.toByteArray();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] fingerprint = digest.digest(pubKeyBytes);

        return "SHA256:" + Base64.toBase64String(fingerprint);
    }

    // 辅助方法：从公钥字符串中提取 Base64 部分
    private static String extractBase64FromPublicKey(String publicKeyString) {
        String[] parts = publicKeyString.split(" ");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid public key format");
        }
        return parts[1];
    }

    // 辅助方法：从二进制数据中解析 e 的字节数组
    private static byte[] parseE(byte[] decoded) throws IOException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(decoded);
             DataInputStream dis = new DataInputStream(bis)) {
            // 读取算法名称（如 "ssh-rsa"）
            String algorithm = readString(dis);
            if (!algorithm.equals("ssh-rsa")) {
                throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
            }
            // 读取 e 的 MPINT（SSH 整数格式）
            return readMPInt(dis);
        }
    }

    // 辅助方法：从二进制数据中解析 n 的字节数组
    private static byte[] parseN(byte[] decoded) throws IOException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(decoded);
             DataInputStream dis = new DataInputStream(bis)) {
            String algorithm = readString(dis);
            if (!algorithm.equals("ssh-rsa")) {
                throw new IllegalArgumentException("Unsupported algorithm");
            }
            // 跳过 e 的 MPINT
            skipMPInt(dis);
            // 读取 n 的 MPINT
            return readMPInt(dis);
        }
    }

    // 辅助方法：调整字节数组（去除前导零，补前导零）
    private static byte[] adjustBytes(byte[] bytes) {
        // 去除前导零
        if (bytes[0] == 0 && bytes.length > 1) {
            byte[] trimmed = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, trimmed, 0, trimmed.length);
            bytes = trimmed;
        }
        // 补前导零（如果最高位为负数）
        if ((bytes[0] & 0x80) != 0) {
            byte[] padded = new byte[bytes.length + 1];
            System.arraycopy(bytes, 0, padded, 1, bytes.length);
            bytes = padded;
        }
        return bytes;
    }

    // 辅助方法：读取 SSH 字符串（前 4 字节为长度）
    private static String readString(DataInputStream dis) throws IOException {
        int length = dis.readInt();
        byte[] bytes = new byte[length];
        dis.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    // 辅助方法：读取 SSH 整数（MPINT 格式）
    private static byte[] readMPInt(DataInputStream dis) throws IOException {
        int length = dis.readInt();
        byte[] bytes = new byte[length];
        dis.readFully(bytes);
        return bytes;
    }

    // 辅助方法：跳过 SSH 整数（MPINT 格式）
    private static void skipMPInt(DataInputStream dis) throws IOException {
        int length = dis.readInt();
        dis.skip(length);
    }

    private static void writeDERInteger(DataOutputStream out, BigInteger value) throws Exception {
        byte[] bytes = value.toByteArray();
        if (bytes[0] == 0 && bytes.length > 1) {
            byte[] trimmed = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, trimmed, 0, trimmed.length);
            bytes = trimmed;
        }
        out.write(0x02);
        writeDERLength(out, bytes.length);
        out.write(bytes);
    }

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

    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}