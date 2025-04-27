package top.srcandy.terminal_air.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import top.srcandy.terminal_air.exception.ServiceException;
import top.srcandy.terminal_air.pojo.model.User;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class JWTUtil {
    private static final String SECRET = "Aj/k73A6sXZUyHyt";
    private static final Algorithm algorithm = Algorithm.HMAC256(SECRET);

    private static final JWTVerifier verifier = JWT.require(algorithm).withIssuer("Terminal Air").acceptExpiresAt(1800).build();

    private static final JWTVerifier publicAccessVerifier = JWT.require(algorithm)
            .withIssuer("Terminal Air", "Terminal Air TwoFactorAuth") // 允许多个 Issuer
            .acceptExpiresAt(1800)
            .build();
    private static final JWTVerifier twoFactorAuthSecretVerifier = JWT.require(algorithm).withIssuer("Terminal Air TwoFactorAuth").acceptExpiresAt(300).build();
    private static final long EXPIRATION = 86400L; //单位为秒

    public static String generateToken(User user) {
        Date expireDate = new Date(System.currentTimeMillis() + EXPIRATION * 1000);
        try {
            return JWT.create()
                    .withIssuer("Terminal Air")
                    .withIssuedAt(new Date())
                    .withExpiresAt(expireDate)
                    .withClaim("username", user.getUsername())
                    .sign(algorithm);
        } catch (JWTCreationException e) {
            log.info(e.getMessage());
        }
        return null;
    }

    public static String generateTwoFactorAuthSecretToken(User user, String twoFactorAuthSecret) {
        // 5分钟过期
        Date expireDate = new Date(System.currentTimeMillis() + 600 * 1000);
        try {
            return JWT.create()
                    .withIssuer("Terminal Air TwoFactorAuth")
                    .withIssuedAt(new Date())
                    .withExpiresAt(expireDate)
                    .withClaim("username", user.getUsername())
                    .withClaim("twoFactorAuthSecret", AESUtils.encryptToHex(twoFactorAuthSecret, user.getSalt()))
                    .sign(algorithm);
        } catch (JWTCreationException e) {
            log.info(e.getMessage());
        } catch (GeneralSecurityException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static void validateToken(String token) {
        try {
            DecodedJWT jwt = verifier.verify(token);
        } catch (InvalidClaimException e) {
            if (e.getMessage().contains("issuer")) {
                String username = Optional.ofNullable(getTokenClaimMap(token).get("username")).map(Claim::asString).orElse("匿名用户");
                log.error("{} 非法登录 {}", username,e.getMessage());
                throw new ServiceException("非法登录");
            }
        } catch (TokenExpiredException e) {
            String username = Optional.ofNullable(getTokenClaimMap(token).get("username")).map(Claim::asString).orElse("匿名用户");
            log.error("{} token已过期 {}", username,e.getMessage());
            throw new ServiceException("登录已过期，请重新登录");
        }
    }

    public static void validateTwoFactorAuthSecretToken(String token) {
        try {
            DecodedJWT jwt = twoFactorAuthSecretVerifier.verify(token);
        } catch (InvalidClaimException e) {
            if (e.getMessage().contains("issuer")) {
                String username = Optional.ofNullable(getTokenClaimMap(token).get("username")).map(Claim::asString).orElse("匿名用户");
                log.error("{} 非法登录 {}", username,e.getMessage());
                throw new ServiceException("非法登录");
            }
        } catch (TokenExpiredException e) {
            String username = Optional.ofNullable(getTokenClaimMap(token).get("username")).map(Claim::asString).orElse("匿名用户");
            log.error("{} 二步验证密钥token已过期 {}", username,e.getMessage());
            throw new ServiceException("登录已过期，请重新登录");
        }
    }


    public static Map<String, Claim> getTokenClaimMap(String token) {
        if (token == null || token.isEmpty()) {
            throw new ServiceException("Token不能为空");
        }
        // 可以检查 token 格式，简单地判断是否包含 '.'
        if (token.split("\\.").length != 3) {
            throw new ServiceException("无效的Token格式");
        }
        return JWT.decode(token).getClaims();
    }
}
