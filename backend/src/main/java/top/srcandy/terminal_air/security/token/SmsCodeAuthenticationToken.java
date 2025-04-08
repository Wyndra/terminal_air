package top.srcandy.terminal_air.security.token;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class SmsCodeAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;
    private final String serial;
    private final Object credentials;

    // 验证前的构造函数
    public SmsCodeAuthenticationToken(String phone, String serial, String code) {
        super(null);
        this.principal = phone;
        this.serial = serial;
        this.credentials = code;
        setAuthenticated(false);
    }

    // 验证成功后的构造函数
    public SmsCodeAuthenticationToken(Object principal, String serial, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.serial = serial;
        this.credentials = credentials;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    public String getSerial() {
        return serial;
    }
}
