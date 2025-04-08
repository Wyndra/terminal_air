package top.srcandy.terminal_air.security.provider;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import top.srcandy.terminal_air.pojo.LoginUser;
import top.srcandy.terminal_air.security.token.TwoFactorAuthenticationToken;
import top.srcandy.terminal_air.service.impl.MicrosoftAuth;
import top.srcandy.terminal_air.security.UserTwoFactorLoginDetailsServiceImpl;
import top.srcandy.terminal_air.utils.AESUtils;
import top.srcandy.terminal_air.utils.JWTUtil;

@Slf4j
@Component
public class TwoFactorAuthenticationProvider implements AuthenticationProvider {


    @Autowired
    private MicrosoftAuth microsoftAuth;

    @Autowired
    private UserTwoFactorLoginDetailsServiceImpl userTwoFactorLoginService;

    // 认证逻辑
    @SneakyThrows
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        TwoFactorAuthenticationToken token = (TwoFactorAuthenticationToken) authentication;
        String username = String.valueOf(token.getPrincipal());
        String tokenSecretHex = JWTUtil.getTokenClaimMap(token.getTwoFactorAuthToken()).get("twoFactorAuthSecret").asString();

        // 加载用户信息
        LoginUser loginUser = (LoginUser) userTwoFactorLoginService.loadUserByUsername(username);
        if (loginUser == null) {
            log.warn("用户名 {} 不存在", username);
            throw new UsernameNotFoundException("用户不存在");
        }

        // 解密密钥
        String decryptedSecret = AESUtils.decryptFromHex(tokenSecretHex, loginUser.getSalt());

        // 校验验证码
        long code = token.getCode();
        long time = token.getTime();
        log.info("用户 [{}] 正在进行二次认证，验证码：{}，时间戳：{}", username, code, time);

        boolean valid = microsoftAuth.checkCode(decryptedSecret, code, time);
        if (!valid) {
            log.warn("用户 [{}] 二次认证失败：验证码错误", username);
            throw new BadCredentialsException("二次认证失败，验证码错误");
        }

        // 构造已认证的 token
        TwoFactorAuthenticationToken authenticatedToken = new TwoFactorAuthenticationToken(loginUser, loginUser.getTwoFactorAuthSecret(),  // ⚠️ 建议避免再存密钥，可考虑改为 null
                code, time, loginUser.getAuthorities());
        log.info("用户 [{}] 二次认证成功，登录完成", username);
        return authenticatedToken;
    }


    @Override
    public boolean supports(Class<?> authentication) {
        return TwoFactorAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
