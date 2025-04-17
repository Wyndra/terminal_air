package top.srcandy.terminal_air.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import top.srcandy.terminal_air.exception.ServiceException;
import top.srcandy.terminal_air.pojo.LoginUser;
import top.srcandy.terminal_air.service.RedisService;
import top.srcandy.terminal_air.utils.JWTUtil;

import javax.security.sasl.AuthenticationException;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Autowired
    private RedisService redisService;

    private final List<String> twoFactorAuthTokenWhiteList = List.of("/api/auth/getUserAvatar","/api/auth/loginRequireTwoFactorAuth", "/api/mfa/verifyTwoFactorAuthCode");

    private final List<String> shortTokenWhiteList = List.of("/api/credentials/update/status/shortToken","/api/credentials/get/status/shortToken/**");

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException, ServiceException {
        String token = request.getHeader("Authorization");

        if (StringUtils.isBlank(token)){
            filterChain.doFilter(request, response);
            return;
        }
        log.info("{} 访问了接口: {}", Optional.ofNullable(JWTUtil.getTokenClaimMap(token.substring(7)).get("username").asString()).orElse("匿名用户"), request.getRequestURI());
        if (twoFactorAuthTokenWhiteList.contains(request.getRequestURI())) {
            JWTUtil.validateTwoFactorAuthSecretToken(token.substring(7));
            filterChain.doFilter(request, response);
            return;
        }
        String requestUri = request.getRequestURI();
        boolean shortTokenWhiteListMatch = shortTokenWhiteList.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestUri));
        if (shortTokenWhiteListMatch) {
            String user = redisService.get("install_shell_token_" + token.substring(7));
            if (Objects.isNull(user)) {
                throw new AuthenticationException("短令牌已失效或不存在");
            }
            filterChain.doFilter(request, response);
            return;
        }

        JWTUtil.validateToken(token.substring(7));
        String username = JWTUtil.getTokenClaimMap(token.substring(7)).get("username").asString();
        LoginUser loginUser = redisService.getObject("security:" + username,LoginUser.class);
        // 如果用户不存在，说明用户已经退出登录
        if (Objects.isNull(loginUser)) {
            throw new AuthenticationException("用户未登录");
        }

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginUser, null, null);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        filterChain.doFilter(request, response);

    }
}
