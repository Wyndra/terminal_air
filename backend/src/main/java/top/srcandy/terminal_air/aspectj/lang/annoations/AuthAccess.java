package top.srcandy.terminal_air.aspectj.lang.annoations;

import java.lang.annotation.*;


/**
 * 认证通过注解，有该注解的方法将无需进行认证。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuthAccess {
}
