package top.srcandy.candyterminal.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.srcandy.candyterminal.dao.UserDao;
import top.srcandy.candyterminal.model.User;
import top.srcandy.candyterminal.request.VerifyTwoFactorAuthCodeRequest;
import top.srcandy.candyterminal.service.MultiFactorAuthenticationService;
import top.srcandy.candyterminal.utils.AESUtils;
import top.srcandy.candyterminal.utils.JWTUtil;
import top.srcandy.candyterminal.utils.TwoFactorAuthUtil;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

@Service
public class MultiFactorAuthenticationServiceImpl implements MultiFactorAuthenticationService {

    private final UserDao userDao;

    public MultiFactorAuthenticationServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Autowired
    private MicrosoftAuth microsoftAuth;

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
