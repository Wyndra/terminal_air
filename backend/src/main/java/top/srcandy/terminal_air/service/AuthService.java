package top.srcandy.terminal_air.service;

import top.srcandy.terminal_air.pojo.vo.LoginResultVO;
import top.srcandy.terminal_air.pojo.vo.UserProfileVO;
import top.srcandy.terminal_air.constant.ResponseResult;
import top.srcandy.terminal_air.model.User;
import top.srcandy.terminal_air.request.*;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

public interface AuthService {
    ResponseResult<LoginResultVO> login(LoginRequest request);

    ResponseResult<LoginResultVO> loginAndChangePassword(LoginRequest request);

    ResponseResult<LoginResultVO> loginSecurity(LoginRequest request);

    void logout();

    ResponseResult<String> loginRequireTwoFactorAuth(String twoFactorAuthToken,VerifyTwoFactorAuthCodeRequest request) throws GeneralSecurityException, UnsupportedEncodingException;

    ResponseResult<String> loginSecurityRequireTwoFactorAuth(String twoFactorAuthToken,VerifyTwoFactorAuthCodeRequest request) throws GeneralSecurityException, UnsupportedEncodingException;

    ResponseResult<String> loginBySmsCode(LoginBySmsCodeRequest request);

    ResponseResult<String> loginSecurityBySmsCode(LoginBySmsCodeRequest request);

    ResponseResult<String> register(RegisterRequest request);

    ResponseResult<UserProfileVO> getUserProfile();

    ResponseResult<String> getUserAvatar();

    User getUserByUsername(String username);

    User getUserInfo(String username);

    String getSaltByUsername(String username);

    boolean verifyUserPassword(String password);

    ResponseResult<String> updatePassword(UpdatePasswordRequest request);

    ResponseResult<UserProfileVO> updateProfile(UpdateProfileRequest request);

}
