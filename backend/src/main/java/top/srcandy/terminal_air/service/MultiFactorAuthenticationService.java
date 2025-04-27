package top.srcandy.terminal_air.service;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import top.srcandy.terminal_air.pojo.vo.EnableTwoFactorAuthVo;
import top.srcandy.terminal_air.request.EnableTwoFactorAuthCodeRequest;
import top.srcandy.terminal_air.request.VerifyOneTimeBackupCodeRequest;
import top.srcandy.terminal_air.request.VerifyTwoFactorAuthCodeRequest;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

public interface MultiFactorAuthenticationService {
    Boolean switchTwoFactorAuth();

    EnableTwoFactorAuthVo enableTwoFactorAuth(EnableTwoFactorAuthCodeRequest request);

    void disableTwoFactorAuth();

    String initTwoFactorAuth();

    String getTwoFactorAuthSecretQRCode();

    Boolean getCurrentTwoFactorAuthStatus();

    String getTwoFactorAuthTitle();

    ResponseEntity<Resource> downloadOneTimeCodeBackup();

    boolean verifyTwoFactorAuthCode(VerifyTwoFactorAuthCodeRequest request) throws GeneralSecurityException, UnsupportedEncodingException;

    boolean verifyOneTimeBackupCode(VerifyOneTimeBackupCodeRequest request) throws GeneralSecurityException, UnsupportedEncodingException;

    ArrayList<String> generateOneTimeCodeBackupList();

    ArrayList<String> refreshOneTimeCodeBackupList() throws GeneralSecurityException, UnsupportedEncodingException;
}
