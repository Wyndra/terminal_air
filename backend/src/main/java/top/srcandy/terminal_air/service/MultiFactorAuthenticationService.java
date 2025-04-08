package top.srcandy.terminal_air.service;

import top.srcandy.terminal_air.request.VerifyTwoFactorAuthCodeRequest;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

public interface MultiFactorAuthenticationService {
    String switchTwoFactorAuth();

    String getTwoFactorAuthSecretQRCode();

    boolean verifyTwoFactorAuthCode(String twoFactorAuthToken, VerifyTwoFactorAuthCodeRequest request) throws GeneralSecurityException, UnsupportedEncodingException;
}
