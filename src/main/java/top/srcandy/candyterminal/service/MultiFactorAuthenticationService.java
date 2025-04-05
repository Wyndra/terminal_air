package top.srcandy.candyterminal.service;

import top.srcandy.candyterminal.request.VerifyTwoFactorAuthCodeRequest;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

public interface MultiFactorAuthenticationService {
    String switchTwoFactorAuth(String token);

    String getTwoFactorAuthSecretQRCode(String token);

    boolean verifyTwoFactorAuthCode(String twoFactorAuthToken, VerifyTwoFactorAuthCodeRequest request) throws GeneralSecurityException, UnsupportedEncodingException;
}
