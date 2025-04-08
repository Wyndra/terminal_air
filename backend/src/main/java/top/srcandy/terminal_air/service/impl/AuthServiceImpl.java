package top.srcandy.terminal_air.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.mapstruct.control.MappingControl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.token.Sha512DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import top.srcandy.terminal_air.pojo.LoginUser;
import top.srcandy.terminal_air.security.token.SmsCodeAuthenticationToken;
import top.srcandy.terminal_air.security.token.TwoFactorAuthenticationToken;
import top.srcandy.terminal_air.pojo.dto.RegisterDTO;
import top.srcandy.terminal_air.pojo.vo.LoginResultVO;
import top.srcandy.terminal_air.pojo.vo.UserProfileVO;
import top.srcandy.terminal_air.constant.ResponseResult;
import top.srcandy.terminal_air.converter.UserProfileConverter;
import top.srcandy.terminal_air.exception.ServiceException;
import top.srcandy.terminal_air.mapper.UserMapper;
import top.srcandy.terminal_air.model.User;
import top.srcandy.terminal_air.request.*;
import top.srcandy.terminal_air.service.AuthService;
import top.srcandy.terminal_air.service.MinioService;
import top.srcandy.terminal_air.service.RedisService;
import top.srcandy.terminal_air.service.SmsService;
import top.srcandy.terminal_air.utils.AESUtils;
import top.srcandy.terminal_air.utils.JWTUtil;
import top.srcandy.terminal_air.utils.SaltUtils;
import top.srcandy.terminal_air.utils.SecurityUtils;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {


    @Autowired
    private UserProfileConverter userProfileConverter;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private AuthenticationManager authenticationManager;

//    @Autowired
//    private AuthenticationProvider authenticationProvider;
//
//    @Autowired
//    private SmsCodeAuthenticationProvider smsCodeAuthenticationProvider;

    @Autowired
    private SmsService smsService;

    @Autowired
    private MinioService minioService;

    @Autowired
    private MicrosoftAuth microsoftAuth;

//    public AuthServiceImpl(UserDao userDao) {
//        this.userDao = userDao;
//    }

    @Override
    @Deprecated
    public ResponseResult<LoginResultVO> login(LoginRequest request) {
        User result = userMapper.selectByUserName(request.getUsername());
        if (result == null) {
            return ResponseResult.fail(null, "用户不存在");
        }
        if (result.getPassword().equals(request.getPassword())) {
            if (result.getIsTwoFactorAuth().equals("1")) {
                return ResponseResult.success(LoginResultVO.builder().token(JWTUtil.generateTwoFactorAuthSecretToken(result)).requireTwoFactorAuth(true).build());
            } else {
                return ResponseResult.success(LoginResultVO.builder().token(JWTUtil.generateToken(result)).requireTwoFactorAuth(false).build());
            }
        }else {
            return ResponseResult.fail(null, "用户名或密码错误");
        }
    }



    @Override
    @Deprecated
    public ResponseResult<LoginResultVO> loginAndChangePassword(LoginRequest request) {
        User result = userMapper.selectByUserName(request.getUsername());
        Optional<User> userOptional = Optional.ofNullable(result);
        if (userOptional.isEmpty()) {
            return ResponseResult.fail(null, "用户不存在");
        }
        // 如果Password_hash为null
        Optional<String> passwordHashOptional = Optional.ofNullable(result.getPassword_hash());
        if (passwordHashOptional.isEmpty()) {
            // 说明已经是高安全性密码
            if (result.getPassword().equals(Sha512DigestUtils.shaHex(request.getPassword() + result.getSalt()))) {
                if (result.getIsTwoFactorAuth().equals("1")) {
                    // 生成两步验证的Token
                    return ResponseResult.success(LoginResultVO.builder().token(JWTUtil.generateTwoFactorAuthSecretToken(result)).requireTwoFactorAuth(true).build());
                } else {
                    // 直接生成token
                    redisService.setObject("security:" + result.getUsername(), result, 24, TimeUnit.HOURS);
                    return ResponseResult.success(LoginResultVO.builder().token(JWTUtil.generateToken(result)).requireTwoFactorAuth(false).build());
                }
            } else {
                return ResponseResult.fail(null, "用户名或密码错误");
            }
        }else {
            // 如果Password_hash不为null
            if (result.getPassword_hash().equals(DigestUtils.md5DigestAsHex(request.getPassword().getBytes()))) {
                // 说明是低安全性密码
                result.setPassword(Sha512DigestUtils.shaHex(request.getPassword() + result.getSalt()));
                // 原密码作废
                result.setPassword_hash(null);
                userMapper.update(result);
                if (result.getIsTwoFactorAuth().equals("1")) {
                    // 生成两步验证的Token
                    return ResponseResult.success(LoginResultVO.builder().token(JWTUtil.generateTwoFactorAuthSecretToken(result)).requireTwoFactorAuth(true).build());
                } else {
                    // 直接生成token
                    return ResponseResult.success(LoginResultVO.builder().token(JWTUtil.generateToken(result)).requireTwoFactorAuth(false).build());
                }
            } else {
                return ResponseResult.fail(null, "用户名或密码错误");
            }
        }
    }

    @Override
    public ResponseResult<LoginResultVO> loginSecurity(LoginRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        if (Objects.isNull(authentication)) {
            log.error("登录失败");
            throw new ServiceException("登录失败");
        }

        // 获取登录用户信息
        LoginUser principal = (LoginUser) authentication.getPrincipal();
        User user = principal.getUser();
        // 自动升级低安全性密码
        if (user.getPassword_hash() != null) {
            // 比对旧密码是否匹配（这里假设 authenticationProvider 认证已成功）
            String expectedMd5 = DigestUtils.md5DigestAsHex(request.getPassword().getBytes());
            if (user.getPassword_hash().equals(expectedMd5)) {
                log.info("检测到 {} 为低安全性密码，正在升级为高安全性加密...",user.getUsername());
                // 设置新密码为高安全性 SHA-512（加盐）
                String newSecurePassword = Sha512DigestUtils.shaHex(request.getPassword() + user.getSalt());
                user.setPassword(newSecurePassword);
                user.setPassword_hash(null); // 清除旧密码字段
                // 更新数据库密码
                userMapper.update(user);
                // 同时更新认证信息中的密码
                principal.getUser().setPassword(newSecurePassword);
                principal.getUser().setPassword_hash(null);
            }
        }

        // 登录流程
        if ("1".equals(user.getIsTwoFactorAuth())) {
            // 开启两步验证
            log.info("用户 {} 进入了两步验证流程", user.getUsername());
            return ResponseResult.success(LoginResultVO.builder()
                    .token(JWTUtil.generateTwoFactorAuthSecretToken(user))
                    .requireTwoFactorAuth(true)
                    .build());
        } else {
            // 直接登录
            redisService.setObject("security:" + user.getUsername(), principal, 24, TimeUnit.HOURS);
            log.info("登录成功，token:{}", JWTUtil.generateToken(user));
            return ResponseResult.success(LoginResultVO.builder()
                    .token(JWTUtil.generateToken(user))
                    .requireTwoFactorAuth(false)
                    .build());
        }
    }

    @Override
    public void logout() {
        String username = SecurityUtils.getUsername();
        redisService.delete("security:" + username);
        SecurityContextHolder.clearContext();
        ResponseResult.success("Logout success");
    }


    @Override
    @Deprecated
    public ResponseResult<String> loginRequireTwoFactorAuth(String twoFactorAuthToken, VerifyTwoFactorAuthCodeRequest request) throws GeneralSecurityException, UnsupportedEncodingException {
        String username = JWTUtil.getTokenClaimMap(twoFactorAuthToken).get("username").asString();
        User user = userMapper.selectByUserName(username);
        if (user == null) {
            return ResponseResult.fail(null, "用户不存在");
        }
        // 判断token中的twoFactorAuthSecret是否与用户的twoFactorAuthSecret一致
        String twoFactorAuthSecret = JWTUtil.getTokenClaimMap(twoFactorAuthToken).get("twoFactorAuthSecret").asString();
        if (!AESUtils.decryptFromHex(twoFactorAuthSecret,user.getSalt()).equals(user.getTwoFactorAuthSecret())) {
            log.info("用户{}的两步验证密钥不一致", username);
            return ResponseResult.fail(null, "校验失败");
        }
        boolean verifyTwoFactorAuthCodeResult = microsoftAuth.checkCode(user.getTwoFactorAuthSecret(), request.getCode(), request.getTime());
        if (verifyTwoFactorAuthCodeResult) {

            return ResponseResult.success(JWTUtil.generateToken(user));
        } else {
            return ResponseResult.fail(null, "验证码错误");
        }
    }

    @Override
    public ResponseResult<String> loginSecurityRequireTwoFactorAuth(String twoFactorAuthToken, VerifyTwoFactorAuthCodeRequest request) throws GeneralSecurityException, UnsupportedEncodingException {
        String username = JWTUtil.getTokenClaimMap(twoFactorAuthToken).get("username").asString();
        TwoFactorAuthenticationToken twoFactorAuthenticationToken = new TwoFactorAuthenticationToken(username, twoFactorAuthToken, request.getCode(), request.getTime());
        Authentication authentication = authenticationManager.authenticate(twoFactorAuthenticationToken);
        if (Objects.isNull(authentication)) {
            log.error("登录失败");
            throw new ServiceException("登录失败");
        }
        LoginUser principal = (LoginUser) authentication.getPrincipal();
        User currentUser = principal.getUser();
        redisService.setObject("security:" + currentUser.getUsername(), principal, 24, TimeUnit.HOURS);
        // 将认证信息存入 SecurityContextHolder
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return ResponseResult.success(JWTUtil.generateToken(currentUser));
    }


    @Override
    public ResponseResult<String> loginBySmsCode(LoginBySmsCodeRequest request) {
        User user = userMapper.selectByUserPhone(request.getPhone());
        if (user == null) {
            return ResponseResult.fail(null, "用户不存在");
        }
        if (smsService.verifySmsCode(request.getPhone(), request.getSerial(), request.getVerificationCode())) {
            return ResponseResult.success(JWTUtil.generateToken(user));
        } else {
            return ResponseResult.fail(null, "验证码错误");
        }
    }

    @Override
    public ResponseResult<String> loginSecurityBySmsCode(LoginBySmsCodeRequest request) {
        SmsCodeAuthenticationToken smsCodeAuthenticationToken = new SmsCodeAuthenticationToken(request.getPhone(), request.getSerial(), request.getVerificationCode());
        Authentication authentication = authenticationManager.authenticate(smsCodeAuthenticationToken);
        if (Objects.isNull(authentication)) {
            log.error("登录失败");
            throw new ServiceException("登录失败");
        }
        // 获取登录用户信息
        LoginUser principal = (LoginUser) authentication.getPrincipal();
        User user = principal.getUser();
        redisService.setObject("security:" + user.getUsername(), principal, 24, TimeUnit.HOURS);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return ResponseResult.success(JWTUtil.generateToken(user));
    }

    @Override
    public ResponseResult<String> register(RegisterRequest request) {
        User result = userMapper.selectByUserName(request.getUsername());
        // 通过用户名校验是否存在用户
        if (result != null) {
            throw new ServiceException("用户名已存在");
        }
        // 通过手机号校验是否存在用户
        if (userMapper.selectByUserPhone(request.getPhone()) != null) {
            throw new ServiceException("手机号已注册");
        }
        String salt = SaltUtils.generateSalt(16);   // 生成16位随机盐

        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername(request.getUsername());
        registerDTO.setPhone(request.getPhone());
        registerDTO.setSalt(salt);
        registerDTO.setPassword(Sha512DigestUtils.shaHex(request.getPassword() + salt));
        registerDTO.setTwoFactorSecret(microsoftAuth.getSecretKey());

        int rows = userMapper.insertSelective(registerDTO);
        if (rows == 0) {
            return ResponseResult.fail(null, "注册失败");
        }else {
            return ResponseResult.success("注册成功");
        }
    }

    @Override
    public ResponseResult<UserProfileVO> getUserProfile() {
        User user = SecurityUtils.getUser();
        if (user == null) {
            return ResponseResult.fail(null, "用户不存在");
        }
        UserProfileVO userProfileVO = userProfileConverter.user2UserProfileVO(user);
        try{
            userProfileVO.setAvatar(minioService.generateDisplaySignedUrl(user.getAvatar()));
        }catch (Exception e){
            log.error("获取用户头像失败", e);
            userProfileVO.setAvatar(null);
        }
        return ResponseResult.success(userProfileVO);
    }

    @Override
    public ResponseResult<String> getUserAvatar(String token) {
        String username = JWTUtil.getTokenClaimMap(token).get("username").asString();
        User user = userMapper.selectByUserName(username);
        if (user != null) {
            try {
                // 生成头像的临时访问链接
                String avatarUrl = minioService.generateDisplaySignedUrl(user.getAvatar());
                return ResponseResult.success(avatarUrl);
            } catch (Exception e) {
                log.error("获取用户头像失败", e);
                return ResponseResult.fail(null, "获取头像失败");
            }
        }
        return ResponseResult.fail(null, "用户不存在");
    }

    @Override
    public User getUserByUsername(String username) {
        return userMapper.selectByUserName(username);
    }

    @Override
    public User getUserInfo(String username) {
        return null;
    }

    @Override
    public String getSaltByUsername(String username) {
//        String username = SecurityUtils.getUsername();
        return userMapper.selectByUserName(username).getSalt();
    }

    @Override
    public boolean verifyUserPassword(String password) {
        User user = SecurityUtils.getUser();
        assert user != null;
        Optional<String> passwordHashOptional = Optional.ofNullable(user.getPassword_hash());
        if (passwordHashOptional.isEmpty()) {
            // 说明已经是高安全性密码
            if (user.getPassword().equals(Sha512DigestUtils.shaHex(password + user.getSalt()))) {
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        }else {
            // 如果Password_hash不为null
            if (user.getPassword_hash().equals(DigestUtils.md5DigestAsHex(password.getBytes()))) {
                // 说明是低安全性密码
                user.setPassword(Sha512DigestUtils.shaHex(password + user.getSalt()));
                // 原密码作废
                user.setPassword_hash(null);
                userMapper.update(user);
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        }
    }

    @Override
    public ResponseResult<String> updatePassword(UpdatePasswordRequest request) {
        User user = SecurityUtils.getUser();
        // 如果Password_hash为null
        Optional<String> passwordHashOptional = Optional.ofNullable(user.getPassword_hash());
        if (passwordHashOptional.isEmpty()) {
            // 说明已经是高安全性密码
            if (user.getPassword().equals(Sha512DigestUtils.shaHex(request.getOldPassword() + user.getSalt()))) {
                user.setPassword(Sha512DigestUtils.shaHex(request.getNewPassword() + user.getSalt()));
                userMapper.update(user);
                return ResponseResult.success(null);
            } else {
                return ResponseResult.fail(null, "修改失败");
            }
        }else {
            // 如果Password_hash不为null
            if (user.getPassword_hash().equals(DigestUtils.md5DigestAsHex(request.getOldPassword().getBytes()))) {
                // 说明是低安全性密码
                user.setPassword(Sha512DigestUtils.shaHex(request.getNewPassword() + user.getSalt()));
                // 原密码作废
                user.setPassword_hash(null);
                userMapper.update(user);
                return ResponseResult.success(null);
            } else {
                return ResponseResult.fail(null, "修改失败");
            }
        }
    }

    @Override
    public ResponseResult<UserProfileVO> updateProfile(UpdateProfileRequest request) {
        User user = SecurityUtils.getUser();
        String username = SecurityUtils.getUsername();

        // 校验新的手机号是否已经被注册
        if (request.getPhone() != null) {
            User existingUserWithPhone = userMapper.selectByUserPhone(request.getPhone());
            if (existingUserWithPhone != null && !existingUserWithPhone.getUsername().equals(username)) {
                return ResponseResult.fail(null, "手机号已被注册");
            }
        }

        // 更新用户资料
        try {
            userProfileConverter.updateUserProfileRequestToUser(request, user);
            userMapper.update(user);
        } catch (Exception e) {
            log.error("更新用户资料出错", e);
            return ResponseResult.fail(null, "更新资料失败");
        }



        // 返回更新后的用户资料
        UserProfileVO profileVO = UserProfileVO.builder()
                .uid(user.getUid())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .nickname(user.getNickname())
//                .salt(user.getSalt())
                .isTwoFactorAuth(user.getIsTwoFactorAuth())
                .build();

        return ResponseResult.success(profileVO);
    }


}
