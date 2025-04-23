package top.srcandy.terminal_air.service;

import top.srcandy.terminal_air.pojo.vo.LoginResultVo;
import top.srcandy.terminal_air.pojo.vo.UserProfileVo;
import top.srcandy.terminal_air.constant.ResponseResult;
import top.srcandy.terminal_air.pojo.model.User;
import top.srcandy.terminal_air.request.*;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

public interface AuthService {
    ResponseResult<LoginResultVo> login(LoginRequest request);

    ResponseResult<LoginResultVo> loginAndChangePassword(LoginRequest request);

    ResponseResult<LoginResultVo> loginSecurity(LoginRequest request);

    void logout();

    ResponseResult<String> loginRequireTwoFactorAuth(String twoFactorAuthToken,VerifyTwoFactorAuthCodeRequest request) throws GeneralSecurityException, UnsupportedEncodingException;

    ResponseResult<String> loginSecurityRequireTwoFactorAuth(String twoFactorAuthToken,VerifyTwoFactorAuthCodeRequest request) throws GeneralSecurityException, UnsupportedEncodingException;

    ResponseResult<String> loginBySmsCode(LoginBySmsCodeRequest request);

    ResponseResult<String> loginSecurityBySmsCode(LoginBySmsCodeRequest request);

    ResponseResult<String> register(RegisterRequest request);

    ResponseResult<UserProfileVo> getUserProfile();

    ResponseResult<String> getUserAvatar(String token);

    User getUserByUsername(String username);

    User getUserInfo(String username);

    String getSaltByUsername(String username);

    boolean verifyUserPassword(String password);

    ResponseResult<String> updatePassword(UpdatePasswordRequest request);

    ResponseResult<UserProfileVo> updateProfile(UpdateProfileRequest request);

}
