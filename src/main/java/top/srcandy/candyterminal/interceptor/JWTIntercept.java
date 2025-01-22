package top.srcandy.candyterminal.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import top.srcandy.candyterminal.exception.ServiceException;
import top.srcandy.candyterminal.utils.AuthAccess;
import top.srcandy.candyterminal.utils.JWTUtil;

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
        String authorization = request.getHeader("Authorization");
        // 如果不存在则抛出异常
        if (authorization == null) {
            throw new ServiceException("未登录,请先登录");
        }
        JWTUtil.validateToken(authorization.substring(PREFIX.length()));
        if (!authorization.startsWith(PREFIX)) {
            return false;
        }
        return true;
    }
}
