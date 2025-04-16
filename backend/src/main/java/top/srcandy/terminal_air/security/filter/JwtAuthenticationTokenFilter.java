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
import top.srcandy.terminal_air.pojo.LoginUser;
import top.srcandy.terminal_air.service.RedisService;
import top.srcandy.terminal_air.utils.JWTUtil;

import javax.security.sasl.AuthenticationException;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Autowired
    private RedisService redisService;

    private final List<String> twoFactorAuthTokenWhiteList = List.of("/api/auth/getUserAvatar","/api/auth/loginRequireTwoFactorAuth", "/api/mfa/verifyTwoFactorAuthCode");

    private final List<String> shortTokenWhiteList = List.of("/api/credentials/update/status/shortToken","/api/credentials/get/status/shortToken/**");

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("Authorization");
        if (StringUtils.isBlank(token)){
            // 放行，因为还未登录
            filterChain.doFilter(request, response);
            return;
        }
        log.info(("访问了接口: " + request.getRequestURI()));
        // 如果是两步验证的请求，验证并放行。
        if (twoFactorAuthTokenWhiteList.contains(request.getRequestURI())) {
            JWTUtil.validateTwoFactorAuthSecretToken(token.substring(7));
            filterChain.doFilter(request, response);
            return;
        }

        String requestUri = request.getRequestURI();
        boolean shortTokenWhiteListMatch = shortTokenWhiteList.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestUri));
        if (shortTokenWhiteListMatch) {
            // 放行，短令牌请求
            String user = redisService.get("install_shell_token_" + token.substring(7));
            if (Objects.isNull(user)) {
                log.error("短令牌已失效或不存在");
                throw new AuthenticationException("短令牌已失效或不存在");
            }
            filterChain.doFilter(request, response);
            return;
        }

        JWTUtil.validateToken(token.substring(7));
        String username = JWTUtil.getTokenClaimMap(token.substring(7)).get("username").asString();
        LoginUser loginUser = redisService.getObject("security:" + username,LoginUser.class);
        if (Objects.isNull(loginUser)) {
            log.error("用户未登录");
            throw new AuthenticationException("用户未登录");
        }

        // 存入SecurityContextHolder
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginUser, null, null);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        // 放行
        filterChain.doFilter(request, response);

    }
}
