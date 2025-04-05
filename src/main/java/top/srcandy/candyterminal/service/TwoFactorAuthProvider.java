package top.srcandy.candyterminal.service;

public interface TwoFactorAuthProvider {
    String getSecretKey();

    boolean checkCode(String secret, long code, long time);
}
