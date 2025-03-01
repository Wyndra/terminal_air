package top.srcandy.candyterminal.aspectj.lang.annoations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(ElementType.LOCAL_VARIABLE)
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface AESDecrypt {
}
