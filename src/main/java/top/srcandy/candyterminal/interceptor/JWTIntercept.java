package top.srcandy.candyterminal.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import top.srcandy.candyterminal.exception.ServiceException;
import top.srcandy.candyterminal.utils.AuthAccess;

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
        if (authorization == null) {
            throw new ServiceException("未登录,请先登录");
        }
        if (!authorization.startsWith(PREFIX)) {
            return false;
        }
        return true;
    }
}
