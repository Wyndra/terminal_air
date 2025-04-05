package top.srcandy.candyterminal.service;

import top.srcandy.candyterminal.bean.vo.LoginResultVO;
import top.srcandy.candyterminal.bean.vo.UserProfileVO;
import top.srcandy.candyterminal.constant.ResponseResult;
import top.srcandy.candyterminal.model.User;
import top.srcandy.candyterminal.request.*;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.Objects;

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
