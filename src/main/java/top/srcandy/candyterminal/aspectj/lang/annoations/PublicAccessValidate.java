package top.srcandy.candyterminal.aspectj.lang.annoations;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PublicAccessValidate {
}
