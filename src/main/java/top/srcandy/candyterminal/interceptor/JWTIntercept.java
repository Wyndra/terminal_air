package top.srcandy.candyterminal.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import top.srcandy.candyterminal.aspectj.lang.annoations.PublicAccessValidate;
import top.srcandy.candyterminal.aspectj.lang.annoations.TwoFactorAuthRequired;
import top.srcandy.candyterminal.exception.ServiceException;
import top.srcandy.candyterminal.aspectj.lang.annoations.AuthAccess;
import top.srcandy.candyterminal.utils.JWTUtil;

import java.util.Optional;

@Component
@Slf4j
public class JWTIntercept implements HandlerInterceptor {

    private final String PREFIX = "Bearer ";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws ServiceException {
        if (handler instanceof HandlerMethod) {
            AuthAccess authAccess = ((HandlerMethod) handler).getMethodAnnotation(AuthAccess.class);
            if (authAccess != null) {
                return true;
            }
        }
        // 获取请求头的 如果没有则抛出异常
        String authorization = Optional.ofNullable(request.getHeader("Authorization"))
                .orElseThrow(() -> new ServiceException("未登录,请先登录"));

        if (handler instanceof HandlerMethod) {
            PublicAccessValidate publicAccessValidate = ((HandlerMethod) handler).getMethodAnnotation(PublicAccessValidate.class);
            if (publicAccessValidate != null) {
                JWTUtil.validateBothToken(authorization.substring(PREFIX.length()));
                return true;
            }
            TwoFactorAuthRequired usingTwoFactorAuth = ((HandlerMethod) handler).getMethodAnnotation(TwoFactorAuthRequired.class);
            // 如果该接口为双重认证接口，则验证双重认证token

            if (usingTwoFactorAuth != null) {
                JWTUtil.validateTwoFactorAuthSecretToken(authorization.substring(PREFIX.length()));
            } else {
                JWTUtil.validateToken(authorization.substring(PREFIX.length()));
            }
        }
        if (!authorization.startsWith(PREFIX)) {
            return false;
        }
        return true;
    }
}
