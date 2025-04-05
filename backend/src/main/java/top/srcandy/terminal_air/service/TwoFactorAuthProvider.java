package top.srcandy.terminal_air.service;

public interface TwoFactorAuthProvider {
    String getSecretKey();

    boolean checkCode(String secret, long code, long time);
}
