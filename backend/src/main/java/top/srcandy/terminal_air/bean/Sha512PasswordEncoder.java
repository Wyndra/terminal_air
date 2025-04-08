package top.srcandy.terminal_air.bean;

import org.springframework.context.annotation.Bean;
import org.springframework.security.core.token.Sha512DigestUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import top.srcandy.terminal_air.mapper.UserMapper;

public class Sha512PasswordEncoder implements PasswordEncoder {

    private final String salt;

    // 构造函数，其中注入盐值
    public Sha512PasswordEncoder(String salt) {
        this.salt = salt;
    }

    @Override
    public String encode(CharSequence rawPassword) {
        // 对rawPassword + salt进行SHA-512加密
        return Sha512DigestUtils.shaHex(rawPassword.toString() + salt);
    }
    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        // 对rawPassword + salt进行SHA-512加密，并与encodedPassword（存在数据库的密码）进行比较
        return Sha512DigestUtils.shaHex(rawPassword.toString() + salt).equals(encodedPassword);
    }
}

