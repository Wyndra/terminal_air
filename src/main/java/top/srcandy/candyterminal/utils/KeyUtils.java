package top.srcandy.candyterminal.utils;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Base64;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.util.UUID;

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
    public static String calculateFingerprint(PublicKey publicKey) throws Exception {
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