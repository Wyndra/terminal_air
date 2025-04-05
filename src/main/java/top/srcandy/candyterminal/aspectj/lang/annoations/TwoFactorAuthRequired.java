package top.srcandy.candyterminal.aspectj.lang.annoations;

import java.lang.annotation.*;


/**
 * 标记需要使用两步验证的方法
 * 主要是在拦截器中处理不同的verifyToken逻辑
 * 避免在其他应用场景下使用两步验证进行请求认证
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TwoFactorAuthRequired {

}
