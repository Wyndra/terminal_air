package top.srcandy.candyterminal.service.impl;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.Md5Crypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.token.Sha512DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import top.srcandy.candyterminal.bean.dto.RegisterDTO;
import top.srcandy.candyterminal.bean.vo.LoginResultVO;
import top.srcandy.candyterminal.bean.vo.UserProfileVO;
import top.srcandy.candyterminal.constant.ResponseResult;
import top.srcandy.candyterminal.converter.UserProfileConverter;
import top.srcandy.candyterminal.dao.UserDao;
import top.srcandy.candyterminal.bean.dto.LoginDTO;
import top.srcandy.candyterminal.model.User;
import top.srcandy.candyterminal.request.*;
import top.srcandy.candyterminal.service.AuthService;
import top.srcandy.candyterminal.service.SmsService;
import top.srcandy.candyterminal.utils.AESUtils;
import top.srcandy.candyterminal.utils.JWTUtil;
import top.srcandy.candyterminal.utils.SaltUtils;
import top.srcandy.candyterminal.utils.TwoFactorAuthUtil;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserDao userDao;

    @Autowired
    private UserProfileConverter userProfileConverter;

    @Autowired
    private SmsService smsService;

    @Autowired
    private MicrosoftAuth microsoftAuth;

    public AuthServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public ResponseResult<LoginResultVO> login(LoginRequest request) {
        User result = userDao.selectByUserName(request.getUsername());
        if (result == null) {
            return ResponseResult.fail(null, "用户不存在");
        }
        if (result.getPassword().equals(request.getPassword())) {
            if (result.getIsTwoFactorAuth().equals("1")) {
                // 生成两步验证的Token
                return ResponseResult.success(LoginResultVO.builder().token(JWTUtil.generateTwoFactorAuthSecretToken(result)).requireTwoFactorAuth(true).build());
            } else {
                // 直接生成token
                return ResponseResult.success(LoginResultVO.builder().token(JWTUtil.generateToken(result)).requireTwoFactorAuth(false).build());
            }
        }else {
            return ResponseResult.fail(null, "用户名或密码错误");
        }
    }

    @Override
    public ResponseResult<Map<String, Objects>> verifyTurnstile(String token) {
        String secretKey = System.getenv("CLOUDFLARE_SECRET_KEY");
        if (secretKey == null || secretKey.isEmpty()) {
            return null;
        }
        log.info("secretKey:{}", secretKey);
        if (token == null || token.isEmpty()) {
            return ResponseResult.fail(null, "缺少 Token");
        }

        RestTemplate restTemplate = new RestTemplate();
        String url = "https://challenges.cloudflare.com/turnstile/v0/siteverify";

        // 构建请求参数
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("secret", secretKey);
        params.add("response", token);

        // 发送请求
        Map<String, Objects> response = restTemplate.postForObject(url, params, Map.class);

        return ResponseResult.success(response);
    }

    @Override
    public ResponseResult<LoginResultVO> loginChangePassword(LoginRequest request) {
        User result = userDao.selectByUserName(request.getUsername());
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
                userDao.update(result);
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
    public ResponseResult<String> loginRequireTwoFactorAuth(String twoFactorAuthToken, VerifyTwoFactorAuthCodeRequest request) throws GeneralSecurityException, UnsupportedEncodingException {
        String username = JWTUtil.getTokenClaimMap(twoFactorAuthToken).get("username").asString();
        User user = userDao.selectByUserName(username);
        if (user == null) {
            return ResponseResult.fail(null, "用户不存在");
        }
        // 判断token中的twoFactorAuthSecret是否与用户的twoFactorAuthSecret一致
        String twoFactorAuthSecret = JWTUtil.getTokenClaimMap(twoFactorAuthToken).get("twoFactorAuthSecret").asString();
        if (!AESUtils.decryptFromHex(twoFactorAuthSecret,user.getSalt()).equals(user.getTwoFactorAuthSecret())) {
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
    public ResponseResult<String> loginBySmsCode(LoginBySmsCodeRequest request) {
        User user = userDao.selectByUserPhone(request.getPhone());
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
    public ResponseResult<String> register(RegisterRequest request) {
        User result = userDao.selectByUserName(request.getUsername());
        // 通过用户名校验是否存在用户
        if (result != null) {
            return ResponseResult.fail(null, "用户已存在");
        }
        // 通过手机号校验是否存在用户
        if (userDao.selectByUserPhone(request.getPhone()) != null) {
            return ResponseResult.fail(null, "手机号已注册");
        }
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername(request.getUsername());
        registerDTO.setPhone(request.getPhone());
        String salt = SaltUtils.getSalt(16);   // 生成16位随机盐
        registerDTO.setSalt(salt);
        registerDTO.setPassword(Sha512DigestUtils.shaHex(request.getPassword() + salt));
        registerDTO.setTwoFactorSecret(microsoftAuth.getSecretKey());

        int rows = userDao.insertSelective(registerDTO);
        if (rows == 0) {
            return ResponseResult.fail(null, "注册失败");
        }else {
            return ResponseResult.success("注册成功");
        }
    }

    @Override
    public ResponseResult<UserProfileVO> getUserProfile(String token_no_bearer) {
        String username = JWTUtil.getTokenClaimMap(token_no_bearer).get("username").asString();
        log.info("username:{}", username);
        User user = getUserByUsername(username);
        if (user == null) {
            return ResponseResult.fail(null, "用户不存在");
        }
        UserProfileVO userProfileVO = userProfileConverter.userToUserProfileVO(user);
//        UserProfileVO userProfileVO = UserProfileVO.builder().uid(user.getUid()).username(user.getUsername()).email(user.getEmail()).nickname(user.getNickname()).salt(user.getSalt()).phone(user.getPhone()).build();
        return ResponseResult.success(userProfileVO);
    }

    @Override
    public ResponseResult<String> getUserAvatar(String no_bearer_token) {
        String username = JWTUtil.getTokenClaimMap(no_bearer_token).get("username").asString();
        User user = userDao.selectByUserName(username);
        if (user != null) {
            return ResponseResult.success(user.getAvatar());
        }
        return ResponseResult.fail(null, "用户不存在");
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
    public boolean verifyUserPassword(String token, String password) {
        String username = JWTUtil.getTokenClaimMap(token).get("username").asString();
        User user = userDao.selectByUserName(username);
        Optional<User> userOptional = Optional.ofNullable(user);
        if (userOptional.isEmpty()) {
            return false;
        }
        // 如果Password_hash为null
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
                userDao.update(user);
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        }
    }

    @Override
    public ResponseResult<String> updatePassword(String token, UpdatePasswordRequest request) {

        String username = JWTUtil.getTokenClaimMap(token).get("username").asString();
        User user = userDao.selectByUserName(username);
        Optional<User> userOptional = Optional.ofNullable(user);
        if (userOptional.isEmpty()) {
            return ResponseResult.fail(null, "用户不存在");
        }
        // 如果Password_hash为null
        Optional<String> passwordHashOptional = Optional.ofNullable(user.getPassword_hash());
        if (passwordHashOptional.isEmpty()) {
            // 说明已经是高安全性密码
            if (user.getPassword().equals(Sha512DigestUtils.shaHex(request.getOldPassword() + user.getSalt()))) {
                user.setPassword(Sha512DigestUtils.shaHex(request.getNewPassword() + user.getSalt()));
                userDao.update(user);
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
                userDao.update(user);
                return ResponseResult.success(null);
            } else {
                return ResponseResult.fail(null, "修改失败");
            }
        }
    }

    @Override
    public ResponseResult<UserProfileVO> updateProfile(String token, UpdateProfileRequest request) {
        String username;
        try {
            // 从JWT Token中提取用户名
            username = JWTUtil.getTokenClaimMap(token).get("username").asString();
            if (username == null) {
                return ResponseResult.fail(null, "Token中不包含用户名");
            }
        } catch (Exception e) {
            log.error("从Token中提取用户名出错", e);
            return ResponseResult.fail(null, "无效的Token");
        }

        log.info("提取到的用户名:{}", username);

        // 根据用户名查询用户信息
        User user = getUserByUsername(username);
        if (user == null) {
            return ResponseResult.fail(null, "用户不存在");
        }

        // 校验新的手机号是否已经被注册
        if (request.getPhone() != null) {
            User existingUserWithPhone = userDao.selectByUserPhone(request.getPhone());
            if (existingUserWithPhone != null && !existingUserWithPhone.getUsername().equals(username)) {
                return ResponseResult.fail(null, "手机号已被注册");
            }
        }

        // 更新用户资料
        try {
            userProfileConverter.updateUserProfileRequestToUser(request, user);
            userDao.update(user);
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
                .salt(user.getSalt())
                .isTwoFactorAuth(user.getIsTwoFactorAuth())
//                .twoFactorAuthSecret(user.getTwoFactorAuthSecret())
                .build();

        return ResponseResult.success(profileVO);
    }

    @Override
    public String switchTwoFactorAuth(String token) {
        String username = JWTUtil.getTokenClaimMap(token).get("username").asString();
        User user = userDao.selectByUserName(username);
        if (user == null) {
            return null;
        }
        if (user.getIsTwoFactorAuth().equals("0")) {
            user.setIsTwoFactorAuth("1");
        } else {
            user.setIsTwoFactorAuth("0");
        }
        userDao.update(user);
        return user.getIsTwoFactorAuth();
    }

    @Override
    public String getTwoFactorAuthSecretQRCode(String token) {
        String username = JWTUtil.getTokenClaimMap(token).get("username").asString();
        User user = userDao.selectByUserName(username);
        if (user == null) {
            return null;
        }
        return new TwoFactorAuthUtil().getQrCode(username, user.getTwoFactorAuthSecret());
    }

    @Override
    public boolean verifyTwoFactorAuthCode(String twoFactorAuthToken, VerifyTwoFactorAuthCodeRequest request) throws GeneralSecurityException, UnsupportedEncodingException {
        String username = JWTUtil.getTokenClaimMap(twoFactorAuthToken).get("username").asString();
        User user = userDao.selectByUserName(username);
        if (user == null) {
            return false;
        }
        // 判断token中的twoFactorAuthSecret是否与用户的twoFactorAuthSecret一致
        if (!AESUtils.decryptFromHex(JWTUtil.getTokenClaimMap(twoFactorAuthToken).get("twoFactorAuthSecret").asString(),user.getSalt()).equals(user.getTwoFactorAuthSecret())) {
            return false;
        }
        return microsoftAuth.checkCode(user.getTwoFactorAuthSecret(), request.getCode(), request.getTime());
    }


}
