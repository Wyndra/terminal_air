package top.srcandy.terminal_air.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import top.srcandy.terminal_air.mapper.UserMapper;
import top.srcandy.terminal_air.model.User;
import top.srcandy.terminal_air.pojo.LoginUser;

/*
* 根据用户名查询用户信息，供WebSecurityConfig的authenticationProvider调用。
 */
@Service
@Slf4j
public class UserNamePasswordDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectByUserName(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        LoginUser loginUser = new LoginUser();
        loginUser.setUser(user);
        loginUser.setUserId(user.getUid());
        loginUser.setSalt(user.getSalt());
        loginUser.setTwoFactorAuthSecret(user.getTwoFactorAuthSecret());
        loginUser.setPhone(user.getPhone());
        loginUser.setPassword_hash(user.getPassword_hash());
        return loginUser;
    }

}
