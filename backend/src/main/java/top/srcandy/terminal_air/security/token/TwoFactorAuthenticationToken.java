package top.srcandy.terminal_air.security.token;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class TwoFactorAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;
    private final long code;
    private final String twoFactorAuthToken;
    private final long time;
    private final Object credentials;

    // 验证前的构造函数
    public TwoFactorAuthenticationToken(String username, String twoFactorAuthToken,long code,long time) {
        super(null);
        this.principal = username;
        this.twoFactorAuthToken = twoFactorAuthToken;
        this.time = time;
        this.code = code;
        this.credentials = code;
        setAuthenticated(false);
    }

    // 验证成功后的构造函数
    public TwoFactorAuthenticationToken(Object principal, String twoFactorAuthToken,long code, long time, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.twoFactorAuthToken = twoFactorAuthToken;
        this.code = code;
        this.time = time;
        this.credentials = code;
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

    public long getCode() {
        return code;
    }

    public long getTime() {
        return time;
    }

    public String getTwoFactorAuthToken() {
        return twoFactorAuthToken;
    }
}
