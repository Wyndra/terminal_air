package top.srcandy.candyterminal.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.interfaces.Claim;
import lombok.extern.slf4j.Slf4j;
import top.srcandy.candyterminal.exception.ServiceException;
import top.srcandy.candyterminal.model.User;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.Map;

@Slf4j
public class JWTUtil {
    private static final String SECRET = "1393**@&&*@&#!(#&*@#&@*^#^!3¥";
    private static final Algorithm algorithm = Algorithm.HMAC256(SECRET);

    private static JWTVerifier verifier = JWT.require(algorithm).withIssuer("candy").build();

    private static final long EXPIRATION = 1800L; //单位为秒

    public static String generateToken(User user){
        Date expireDate = new Date(System.currentTimeMillis() + EXPIRATION * 1000);
        try{
            return JWT.create()
                    .withIssuer("Terminal Air")
                    .withIssuedAt(new Date())
                    .withExpiresAt(expireDate)
                    .withClaim("username", user.getUsername())
                    .sign(algorithm);
        } catch (JWTCreationException e){
            log.info(e.getMessage());
        }
        return null;
    }

    public static String generateTwoFactorAuthSecretToken(User user){
        // 5分钟过期
        Date expireDate = new Date(System.currentTimeMillis() + 3000 * 1000);
        try{
            return JWT.create()
                    .withIssuer("TwoFactorAuth")
                    .withIssuedAt(new Date())
                    .withExpiresAt(expireDate)
                    .withClaim("username", user.getUsername())
                    // 签发带有两步验证密钥的token，密钥使用用户的私有密钥进行加密
                    .withClaim("twoFactorAuthSecret",AESUtils.encryptToHex(user.getTwoFactorAuthSecret(),user.getSalt()))
                    .sign(algorithm);
        } catch (JWTCreationException e){
            log.info(e.getMessage());
        } catch (GeneralSecurityException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static boolean validateToken(String token){
        try{
            verifier.verify(token);
            return true;
        } catch (Exception e){
            log.info(e.getMessage());
            throw new ServiceException("Token验证失败");
        }
    }

    public static Map<String, Claim> getTokenClaimMap(String token){
        return JWT.decode(token).getClaims();
    }
}
