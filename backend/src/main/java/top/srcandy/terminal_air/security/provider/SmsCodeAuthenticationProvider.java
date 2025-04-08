package top.srcandy.terminal_air.security.provider;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import top.srcandy.terminal_air.pojo.LoginUser;
import top.srcandy.terminal_air.security.token.SmsCodeAuthenticationToken;
import top.srcandy.terminal_air.service.RedisService;
import top.srcandy.terminal_air.security.UserPhoneLoginDetailsServiceImpl;

@Slf4j
@Component
public class SmsCodeAuthenticationProvider implements AuthenticationProvider {

    @Resource
    private RedisService redisService;

    @Resource
    private UserPhoneLoginDetailsServiceImpl userPhoneLoginService;

    @Override
    public SmsCodeAuthenticationToken authenticate(Authentication authentication) {
        SmsCodeAuthenticationToken smsToken = (SmsCodeAuthenticationToken) authentication;

        String phone = smsToken.getPrincipal().toString();
        String serial = smsToken.getSerial();
        String code = smsToken.getCredentials().toString();

        log.info("手机号 {} 正在尝试登录，验证码：{}", phone, code);

        // 校验验证码
        String redisCode = redisService.get("verify_code:" + phone);
        String expectCode = serial + ":" + code;
        if (redisCode == null || !redisCode.equals(expectCode)) {
            throw new BadCredentialsException("验证码错误或已过期");
        }

        // 使用验证码后，删除 Redis 中的验证码
        redisService.delete("verify_code:" + phone);

        // 从数据库载入用户信息
        LoginUser loginUser = (LoginUser) userPhoneLoginService.loadUserByUserPhone(phone);
        if (loginUser == null) {
            throw new UsernameNotFoundException("该手机号未注册");
        }

        // 返回认证成功的 Token：✅ 现在 principal 是 loginUser，而不是手机号字符串
        return new SmsCodeAuthenticationToken(loginUser, serial, code, loginUser.getAuthorities());
    }


    @Override
    public boolean supports(Class<?> authentication) {
        return SmsCodeAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
