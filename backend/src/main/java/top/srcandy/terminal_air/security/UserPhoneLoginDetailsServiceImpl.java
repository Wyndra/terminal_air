package top.srcandy.terminal_air.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import top.srcandy.terminal_air.mapper.UserMapper;
import top.srcandy.terminal_air.model.User;
import top.srcandy.terminal_air.pojo.LoginUser;

@Service
@Slf4j
public class UserPhoneLoginDetailsServiceImpl {
    @Autowired
    private UserMapper userMapper;

    public UserDetails loadUserByUserPhone(String username) throws UsernameNotFoundException {
        User user = userMapper.selectByUserPhone(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        LoginUser loginUser = new LoginUser();
        loginUser.setUser(user);
        loginUser.setSalt(user.getSalt());
        loginUser.setUserId(user.getUid());
        loginUser.setTwoFactorAuthSecret(user.getTwoFactorAuthSecret());
        loginUser.setPhone(user.getPhone());
        loginUser.setPassword_hash(user.getPassword_hash());
        return loginUser;
    }
}
