package top.srcandy.terminal_air.service.impl;

import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Service;
import top.srcandy.terminal_air.exception.ServiceException;
import top.srcandy.terminal_air.service.TwoFactorAuthProvider;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Service
public class MicrosoftAuth implements TwoFactorAuthProvider {
    private MicrosoftAuth() {
    }

    // 在成员位置创建该类的对象
    private static MicrosoftAuth microsoftAuth;

    // 对外提供静态方法获取该对象
    public static synchronized MicrosoftAuth getMicrosoftAuth() {
        if (microsoftAuth == null) {
            microsoftAuth = new MicrosoftAuth();
        }
        return microsoftAuth;
    }

    /**
     * 时间前后偏移量
     * 用于防止客户端时间不精确导致生成的TOTP与服务器端的TOTP一直不一致
     * 如果为0,当前时间为 10:10:15
     * 则表明在 10:10:00-10:10:30 之间生成的TOTP 能校验通过
     * 如果为1,则表明在
     * 10:09:30-10:10:00
     * 10:10:00-10:10:30
     * 10:10:30-10:11:00 之间生成的TOTP 能校验通过
     * 以此类推
     */
    private static int WINDOW_SIZE = 1;

    /**
     * 加密方式，HmacSHA1、HmacSHA256、HmacSHA512
     */
    private static final String CRYPTO = "HmacSHA1";

    /**
     * description: 生成密钥，每个用户独享一份密钥
     *
     * @return java.lang.String
     * @author mints_candys
     * @date 2023/4/27
     */
    @Override
    public String getSecretKey() {
        String secretKey = null;
        try {
            SecureRandom random = new SecureRandom();
            byte[] bytes = new byte[20];
            random.nextBytes(bytes);
            Base32 base32 = new Base32();
            secretKey = base32.encodeToString(bytes).toUpperCase();
        } catch (Exception e) {
            throw new ServiceException("生成密钥发生异常：" + e.getMessage());
        }
        // make the secret key more human-readable by lower-casing and
        // inserting spaces between each group of 4 characters
        return secretKey;
    }

    /**
     * 检验 code 是否正确
     *
     * @param secret 密钥
     * @param code   code
     * @param time   时间戳
     * @return
     */
    @Override
    public boolean checkCode(String secret, long code, long time) {
        boolean flag = false;
        try {
            Base32 codec = new Base32();
            byte[] decodedKey = codec.decode(secret);
            // convert unix msec time into a 30 second "window"
            // this is per the TOTP spec (see the RFC for details)
            long t = (time / 1000L) / 30L;
            // Window is used to check codes generated in the near past.
            // You can use this value to tune how far you're willing to go.
            long hash;
            for (int i = -WINDOW_SIZE; i <= WINDOW_SIZE; ++i) {
                try {
                    hash = verifyCode(decodedKey, t + i);
                } catch (Exception e) {
                    // Yes, this is bad form - but
                    // the exceptions thrown would be rare and a static
                    // configuration problem
                    // e.printStackTrace();
                    throw new RuntimeException(e.getMessage());
                }
                if (hash == code) {
                    flag = true;
                    return flag;
                }
            }
        } catch (RuntimeException e) {
            throw new ServiceException("校验密钥发生异常：" + e.getMessage());
        }
        return flag;
    }

    /**
     * 根据时间偏移量计算
     *
     * @param key
     * @param t
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    private static long verifyCode(byte[] key, long t) throws NoSuchAlgorithmException, InvalidKeyException {
        try {
            byte[] data = new byte[8];
            long value = t;
            for (int i = 8; i-- > 0; value >>>= 8) {
                data[i] = (byte) value;
            }
            SecretKeySpec signKey = new SecretKeySpec(key, CRYPTO);
            Mac mac = Mac.getInstance(CRYPTO);
            mac.init(signKey);
            byte[] hash = mac.doFinal(data);
            int offset = hash[20 - 1] & 0xF;
            // We're using a long because Java hasn't got unsigned int.
            long truncatedHash = 0;
            for (int i = 0; i < 4; ++i) {
                truncatedHash <<= 8;
                // We are dealing with signed bytes:
                // we just keep the first byte.
                truncatedHash |= (hash[offset + i] & 0xFF);
            }
            truncatedHash &= 0x7FFFFFFF;
            truncatedHash %= 1000000;
            return truncatedHash;
        } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalStateException e) {
            throw new ServiceException("生成系统验证密钥出现异常：" + e.getMessage());
        }
    }
}
