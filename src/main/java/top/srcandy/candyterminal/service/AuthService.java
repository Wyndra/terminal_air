package top.srcandy.candyterminal.service;

import top.srcandy.candyterminal.bean.vo.UserProfileVO;
import top.srcandy.candyterminal.constant.ResponseResult;
import top.srcandy.candyterminal.model.User;
import top.srcandy.candyterminal.request.LoginBySmsCodeRequest;
import top.srcandy.candyterminal.request.LoginRequest;
import top.srcandy.candyterminal.request.RegisterRequest;
import top.srcandy.candyterminal.request.UpdateProfileRequest;

public interface AuthService {

    ResponseResult<String> login(LoginRequest request);

    ResponseResult<String> loginBySmsCode(LoginBySmsCodeRequest request);

    ResponseResult<String> register(RegisterRequest request);

    ResponseResult<UserProfileVO> getUserProfile(String no_bearer_token);

    User getUserByUsername(String username);

    User getUserInfo(String username);

    String getSaltByUsername(String username);

    boolean verifyUserPassword(String token, String password);

    boolean updatePassword(String username, String oldPassword, String newPassword);

    ResponseResult<UserProfileVO> updateProfile(String token, UpdateProfileRequest request);



}
