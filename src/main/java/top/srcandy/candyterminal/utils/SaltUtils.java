package top.srcandy.candyterminal.utils;

public class SaltUtils {
    public static String getSalt(int n){
        char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            char aChar = chars[(int)(Math.random() * chars.length)];
            sb.append(aChar);
        }
        return sb.toString();
    }
}
