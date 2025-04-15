package top.srcandy.terminal_air.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import top.srcandy.terminal_air.pojo.model.User;
import top.srcandy.terminal_air.pojo.LoginUser;

import java.util.Optional;

public class SecuritySessionUtils {

    /**
     * 获取当前登录用户的认证信息
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 获取当前登录用户对象（需要你登录时放的是 LoginUser）
     */
    public static LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional.ofNullable(authentication).orElseThrow(() -> new RuntimeException("未登录,请登录"));
        return (LoginUser) authentication.getPrincipal();
    }

    /**
     * 获取当前登录用户名
     */
    public static String getUsername() {
        LoginUser loginUser = getLoginUser();
        Optional.ofNullable(loginUser).orElseThrow(() -> new RuntimeException("未登录,请先登录"));
        return loginUser.getUsername();
    }

    /**
     * 获取当前登录用户ID
     */
    public static Long getUserId() {
        LoginUser loginUser = getLoginUser();
        Optional.ofNullable(loginUser).orElseThrow(() -> new RuntimeException("未登录,请先登录"));
        return loginUser.getUserId();
    }

    public static User getUser() {
        LoginUser loginUser = getLoginUser();
        Optional.ofNullable(loginUser).orElseThrow(() -> new RuntimeException("未登录,请先登录"));
        return loginUser.getUser();
    }
}
