package top.srcandy.terminal_air.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import top.srcandy.terminal_air.bean.Md5PasswordEncoder;
import top.srcandy.terminal_air.bean.Sha512PasswordEncoder;
import top.srcandy.terminal_air.constant.ResponseResult;
import top.srcandy.terminal_air.security.filter.JwtAuthenticationTokenFilter;
import top.srcandy.terminal_air.pojo.LoginUser;
import top.srcandy.terminal_air.security.provider.SmsCodeAuthenticationProvider;
import top.srcandy.terminal_air.security.provider.TwoFactorAuthenticationProvider;

import java.util.Objects;

@Slf4j
@Configuration
// 解决错误Could not autowire. No beans of 'HttpSecurity' type found
@EnableWebSecurity
// 开启权限注解功能
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private SmsCodeAuthenticationProvider smsCodeAuthenticationProvider;

    @Autowired
    private TwoFactorAuthenticationProvider twoFactorAuthenticationProvider;

    @Autowired
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;


    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
        return new AuthenticationProvider() {
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                String username = authentication.getName();
                String password = authentication.getCredentials().toString();
                // 逐步向下兼容，支持原有的登录方式，支持原先password_hash的登录方式
                LoginUser loginUser = (LoginUser) userDetailsService.loadUserByUsername(username);
                if (Objects.isNull(loginUser)) {
                    log.error("用户不存在");
                    throw new BadCredentialsException("用户不存在");
                }

                // 获取用户的盐
                String salt = loginUser.getSalt();
                PasswordEncoder md5PasswordEncoder = new Md5PasswordEncoder();
                PasswordEncoder sha512PasswordEncoder = new Sha512PasswordEncoder(salt);

                String password_hash = loginUser.getPassword_hash();
                if (Objects.isNull(password_hash)) {
                    // 如果password_hash为空，说明是已经更新密码加密方式的用户
                    if (!sha512PasswordEncoder.matches(password, loginUser.getPassword())) {
                        log.error("密码错误");
                        throw new BadCredentialsException("用户名或密码错误");
                    }
                }else {
                    // 如果password_hash不为空，说明是原先的用户
                    if (!md5PasswordEncoder.matches(password, password_hash)) {
                        log.error("密码错误");
                        throw new BadCredentialsException("用户名或密码错误");
                    }
                }
                return new UsernamePasswordAuthenticationToken(loginUser, password, loginUser.getAuthorities());
            }

            @Override
            public boolean supports(Class<?> authentication) {
                return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
            }
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder
                .authenticationProvider(authenticationProvider(userDetailsService))
                .authenticationProvider(smsCodeAuthenticationProvider)
                .authenticationProvider(twoFactorAuthenticationProvider);
        return builder.build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 不使用 Session
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .formLogin(AbstractHttpConfigurer::disable) // 禁用 Spring Security 默认表单
                .httpBasic(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            if (authException instanceof BadCredentialsException) {
                                response.setCharacterEncoding("UTF-8");
                                response.setContentType("application/json;charset=UTF-8");
                                response.setStatus(401);
                                log.warn("登录失败：{}", request.getRequestURI());
                                ObjectMapper mapper = new ObjectMapper();
                                ResponseResult<?> result = ResponseResult.fail("用户名或密码错误");
                                response.getWriter().write(mapper.writeValueAsString(result));
                            }else {
                                response.setCharacterEncoding("UTF-8");
                                response.setContentType("application/json;charset=UTF-8");
                                response.setStatus(401);
                                log.warn("未认证请求：{}", request.getRequestURI());
                                ObjectMapper mapper = new ObjectMapper();
                                ResponseResult<?> result = ResponseResult.unauthorized("未登录，请先登录");
                                response.getWriter().write(mapper.writeValueAsString(result));
                            }

                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setCharacterEncoding("UTF-8");
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(403);
                            log.warn("权限不足请求：{}", request.getRequestURI());
                            ObjectMapper mapper = new ObjectMapper();
                            ResponseResult<?> result = ResponseResult.forbidden("暂无权限");
                            response.getWriter().write(mapper.writeValueAsString(result));
                        })
                )

                // 关闭 CSRF
                .csrf(AbstractHttpConfigurer::disable)
                // 配置 CORS（等价于你提供的 corsFilter 方法）
                .cors(cors -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.addAllowedOrigin("*"); // 允许所有来源
                    config.addAllowedMethod("*"); // 允许所有请求方法
                    config.addAllowedHeader("*"); // 允许所有请求头
                    config.addExposedHeader("token"); // 暴露 token 响应头
                    config.addExposedHeader("Authorization"); // 暴露 Authorization 响应头
                    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                    source.registerCorsConfiguration("/**", config);
                    cors.configurationSource(source);
                })
                // 配置请求权限
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS).permitAll()
                        .requestMatchers("/webssh/**").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/register").permitAll()
                        .requestMatchers("/api/auth/loginBySmsCode").permitAll()
                        .requestMatchers("/api/turnstile/verify").permitAll()
                        .requestMatchers("/api/sms/sendVerificationCode").permitAll()
                        .requestMatchers("/api/sms/verifyCode").permitAll()
                        .requestMatchers("/api/auth/getUserAvatar").permitAll()
                        .requestMatchers("/api/auth/loginRequireTwoFactorAuth").permitAll()
                        .requestMatchers("/api/mfa/verifyTwoFactorAuthCode").permitAll()
                        .requestMatchers("/api/credentials/installation/**").permitAll()
                        .anyRequest().authenticated()
                )
                // 添加 JWT 认证过滤器
                .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }



    /**
     * 对 knife4j 的资源进行放行
     * @return
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                "/css/**",
                "/js/**",
                "/index.html",
                "/favicon.ico",
                "/doc.html",
                "/swagger-resources/**",
                "/webjars/**",
                "/v3/api-docs/**",
                "/v2/api-docs/**",
                "/swagger-ui.html",
                "/swagger-ui/**"
        );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 只是防止 Spring 报错，你可以不使用它
        return new Md5PasswordEncoder(); // 或 new Sha512PasswordEncoder()
    }



}
