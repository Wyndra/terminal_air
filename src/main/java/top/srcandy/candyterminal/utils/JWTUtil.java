package top.srcandy.candyterminal.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.interfaces.Claim;
import lombok.extern.slf4j.Slf4j;
import top.srcandy.candyterminal.exception.ServiceException;
import top.srcandy.candyterminal.model.User;

import java.util.Date;
import java.util.Map;

@Slf4j
public class JWTUtil {
    private static final String SECRET = "1393**@&&*@&#!(#&*@#&@*^#^!3¥";
    private static final Algorithm algorithm = Algorithm.HMAC256(SECRET);

    private static JWTVerifier verifier = JWT.require(algorithm).withIssuer("candy").build();

    private static final long EXPIRATION = 1800L;//单位为秒

    public static String generateToken(User user){
        Date expireDate = new Date(System.currentTimeMillis() + EXPIRATION * 1000);
        try{
            return JWT.create()
                    .withIssuer("candy")
                    .withIssuedAt(new Date())
                    .withExpiresAt(expireDate)
                    .withClaim("username", user.getUsername())
                    .sign(algorithm);
        } catch (JWTCreationException e){
            log.info(e.getMessage());
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
