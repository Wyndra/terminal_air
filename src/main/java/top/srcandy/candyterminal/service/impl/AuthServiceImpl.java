package top.srcandy.candyterminal.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.srcandy.candyterminal.constant.ResponseResult;
import top.srcandy.candyterminal.dao.UserDao;
import top.srcandy.candyterminal.dto.LoginDTO;
import top.srcandy.candyterminal.model.User;
import top.srcandy.candyterminal.service.AuthService;
import top.srcandy.candyterminal.utils.JWTUtil;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserDao userDao;

    public AuthServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public ResponseResult<String> login(User user) {
        User result = userDao.selectByUserName(user.getUsername());
        if (result == null) {
            return ResponseResult.fail(null, "用户不存在");
        }
        if (result.getPassword().equals(user.getPassword())) {
            return ResponseResult.success(JWTUtil.generateToken(user));
        } else {
            return ResponseResult.fail(null, "密码错误");
        }
    }

    @Override
    public ResponseResult<String> register(User user) {
        User result = userDao.selectByUserName(user.getUsername());
        if (result != null) {
            return ResponseResult.fail(null, "用户已存在");
        }
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername(user.getUsername());
        loginDTO.setPassword(user.getPassword());
        int rows = userDao.insert(loginDTO);
        if (rows == 0) {
            return ResponseResult.fail(null, "注册失败");
        }else {
            return ResponseResult.success("注册成功");
        }
    }

    @Override
    public User getUserByUsername(String username) {
        return userDao.selectByUserName(username);
    }

    @Override
    public User getUserInfo(String username) {
        return null;
    }

    @Override
    public String getSaltByUsername(String username) {
        return userDao.selectByUserName(username).getSalt();
    }

    @Override
    public boolean updatePassword(String username, String oldPassword, String newPassword) {
        return false;
    }
}
