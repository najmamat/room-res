package cz.cvut.user.security.jwt;

import cz.cvut.user.security.model.UserDetails;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtils {
    private static final Logger LOG = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${nss.jwt.secret}")
    private String jwtSecret;

    @Value("${nss.jwt.expiration}")
    private int jwtExpiration;

    public String generateJwtToken(UserDetails userPrincipal) {
        return Jwts.builder().setSubject((userPrincipal.getUsername())).setIssuedAt(new Date())
                // isAdmin boolean is saved in Audience claim
                .setAudience(Boolean.valueOf(userPrincipal.getUser().isAdmin()).toString())
                .setExpiration(new Date((new Date()).getTime() + jwtExpiration))
                .signWith(SignatureAlgorithm.HS512, jwtSecret).compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
    }


    public boolean getIsAdminClaim(String token) {
        String claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getAudience();
        return Boolean.parseBoolean(claims);
    }


    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            LOG.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            LOG.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            LOG.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            LOG.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            LOG.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }

}