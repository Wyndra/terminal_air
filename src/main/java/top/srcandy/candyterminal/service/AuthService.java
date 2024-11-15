package top.srcandy.candyterminal.service;

import top.srcandy.candyterminal.bean.vo.UserProfileVO;
import top.srcandy.candyterminal.constant.ResponseResult;
import top.srcandy.candyterminal.model.User;

public interface AuthService {

    ResponseResult<String> login(User user);

    ResponseResult<String> register(User user);

    ResponseResult<UserProfileVO> getUserProfile(String no_bearer_token);

    User getUserByUsername(String username);

    User getUserInfo(String username);

    String getSaltByUsername(String username);

    boolean updatePassword(String username, String oldPassword, String newPassword);



}
