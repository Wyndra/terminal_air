package top.srcandy.terminal_air.service;

import top.srcandy.terminal_air.bean.vo.LoginResultVO;
import top.srcandy.terminal_air.bean.vo.UserProfileVO;
import top.srcandy.terminal_air.constant.ResponseResult;
import top.srcandy.terminal_air.model.User;
import top.srcandy.terminal_air.request.*;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

public interface AuthService {
    ResponseResult<LoginResultVO> login(LoginRequest request);

    ResponseResult<LoginResultVO> loginAndChangePassword(LoginRequest request);

    ResponseResult<String> loginRequireTwoFactorAuth(String twoFactorAuthToken,VerifyTwoFactorAuthCodeRequest request) throws GeneralSecurityException, UnsupportedEncodingException;

    ResponseResult<String> loginBySmsCode(LoginBySmsCodeRequest request);

    ResponseResult<String> register(RegisterRequest request);

    ResponseResult<UserProfileVO> getUserProfile(String no_bearer_token);

    ResponseResult<String> getUserAvatar(String no_bearer_token);

    User getUserByUsername(String username);

    User getUserInfo(String username);

    String getSaltByUsername(String username);

    boolean verifyUserPassword(String token, String password);

    ResponseResult<String> updatePassword(String username, UpdatePasswordRequest request);

    ResponseResult<UserProfileVO> updateProfile(String token, UpdateProfileRequest request);

}
