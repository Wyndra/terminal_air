package top.srcandy.candyterminal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.srcandy.candyterminal.interceptor.JWTIntercept;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public JWTIntercept jwtIntercept() {
        return new JWTIntercept();
    }
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 需要排除的路径列表
        String[] excludePaths = {
//                "/auth/login",
                "/auth/register",
                "/doc.html",
                "/webjars/**",
                "/swagger-ui",
                "/swagger-ui/**",
                "/v3/**",
                "/favicon.ico",
                "Mozilla/**",
                "/sms/sendVerificationCode",
                "/auth/loginBySmsCode",
                "/sms/verifyCode"
        };

        // 配置拦截器，除了上面指定的路径，其他所有请求都会经过拦截器
        registry.addInterceptor(jwtIntercept())
                .addPathPatterns("/**")  // 所有路径都需要拦截
                .excludePathPatterns(excludePaths)
                .order(1);
    // 排除特定的路径
    }



    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 设置允许跨域请求的域名
        config.addAllowedOrigin("*");
        // 是否允许证书 不再默认开启
        // config.setAllowCredentials(true);
        // 设置允许的方法
        config.addAllowedMethod("*");
        // 允许任何头
        config.addAllowedHeader("*");
        config.addExposedHeader("token");
        UrlBasedCorsConfigurationSource configSource = new UrlBasedCorsConfigurationSource();
        configSource.registerCorsConfiguration("/**", config);
        return new CorsFilter(configSource);
    }
}
