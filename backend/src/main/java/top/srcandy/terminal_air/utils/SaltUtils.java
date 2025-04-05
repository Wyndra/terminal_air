package top.srcandy.terminal_air.utils;

public class SaltUtils {

    /**
     * 生成随机盐
     * @param n 盐的长度
     * @return 盐
     */
    public static String generateSalt(int n){
        char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            char aChar = chars[(int)(Math.random() * chars.length)];
            sb.append(aChar);
        }
        return sb.toString();
    }
}
