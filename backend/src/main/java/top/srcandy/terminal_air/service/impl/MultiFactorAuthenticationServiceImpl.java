package top.srcandy.terminal_air.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.srcandy.terminal_air.mapper.UserMapper;
import top.srcandy.terminal_air.pojo.model.User;
import top.srcandy.terminal_air.request.VerifyTwoFactorAuthCodeRequest;
import top.srcandy.terminal_air.service.MultiFactorAuthenticationService;
import top.srcandy.terminal_air.utils.AESUtils;
import top.srcandy.terminal_air.utils.JWTUtil;
import top.srcandy.terminal_air.utils.SecuritySessionUtils;
import top.srcandy.terminal_air.utils.TwoFactorAuthUtil;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

@Service
public class MultiFactorAuthenticationServiceImpl implements MultiFactorAuthenticationService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MicrosoftAuth microsoftAuth;

    @Override
    public Boolean switchTwoFactorAuth() {
        String username = SecuritySessionUtils.getUsername();
        User user = userMapper.selectByUserName(username);
        if (user.getIsTwoFactorAuth().equals("0")) {
            user.setIsTwoFactorAuth("1");
        } else {
            user.setIsTwoFactorAuth("0");
        }
        userMapper.update(user);
        return user.getIsTwoFactorAuth().equals("1");
    }

    @Override
    public String getTwoFactorAuthSecretQRCode() {
        User user = SecuritySessionUtils.getUser();
        String username = SecuritySessionUtils.getUsername();
        // 如果用户的twoFactorAuthSecret为空，则生成一个新的twoFactorAuthSecret
        if (user.getTwoFactorAuthSecret() == null || user.getTwoFactorAuthSecret().isEmpty()) {
            String twoFactorAuthSecret = microsoftAuth.getSecretKey();
            user.setTwoFactorAuthSecret(twoFactorAuthSecret);
            userMapper.update(user);
        }
        return new TwoFactorAuthUtil().getQrCode(username, user.getTwoFactorAuthSecret());
    }

    @Override
    public boolean verifyTwoFactorAuthCode(String twoFactorAuthToken, VerifyTwoFactorAuthCodeRequest request) throws GeneralSecurityException, UnsupportedEncodingException {
        String username = JWTUtil.getTokenClaimMap(twoFactorAuthToken).get("username").asString();
        User user = userMapper.selectByUserName(username);
        // 判断token中的twoFactorAuthSecret是否与用户的twoFactorAuthSecret一致
        if (!AESUtils.decryptFromHex(JWTUtil.getTokenClaimMap(twoFactorAuthToken).get("twoFactorAuthSecret").asString(),user.getSalt()).equals(user.getTwoFactorAuthSecret())) {
            return false;
        }
        return microsoftAuth.checkCode(user.getTwoFactorAuthSecret(), request.getCode(), request.getTime());
    }
}
