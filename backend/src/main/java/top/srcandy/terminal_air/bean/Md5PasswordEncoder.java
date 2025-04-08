package top.srcandy.terminal_air.bean;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.DigestUtils;

public class Md5PasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence rawPassword) {
        return DigestUtils.md5DigestAsHex(rawPassword.toString().getBytes());
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return DigestUtils.md5DigestAsHex(rawPassword.toString().getBytes()).equals(encodedPassword);
    }
}
