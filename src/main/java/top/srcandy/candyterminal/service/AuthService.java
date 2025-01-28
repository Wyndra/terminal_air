package top.srcandy.candyterminal.service;

import top.srcandy.candyterminal.bean.vo.LoginResultVO;
import top.srcandy.candyterminal.bean.vo.UserProfileVO;
import top.srcandy.candyterminal.constant.ResponseResult;
import top.srcandy.candyterminal.model.User;
import top.srcandy.candyterminal.request.*;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

public interface AuthService {
    ResponseResult<LoginResultVO> login(LoginRequest request);

    ResponseResult<String> loginRequireTwoFactorAuth(String twoFactorAuthToken,VerifyTwoFactorAuthCodeRequest request) throws GeneralSecurityException, UnsupportedEncodingException;

    ResponseResult<String> loginBySmsCode(LoginBySmsCodeRequest request);

    ResponseResult<String> register(RegisterRequest request);

    ResponseResult<UserProfileVO> getUserProfile(String no_bearer_token);

    ResponseResult<String> getUserAvatar(String no_bearer_token);

    User getUserByUsername(String username);

    User getUserInfo(String username);

    String getSaltByUsername(String username);

    boolean verifyUserPassword(String token, String password);

    boolean updatePassword(String username, String oldPassword, String newPassword);

    ResponseResult<UserProfileVO> updateProfile(String token, UpdateProfileRequest request);

    String switchTwoFactorAuth(String token);

    String getTwoFactorAuthSecretQRCode(String token);

    boolean verifyTwoFactorAuthCode(String twoFactorAuthToken, VerifyTwoFactorAuthCodeRequest request) throws GeneralSecurityException, UnsupportedEncodingException;

}
