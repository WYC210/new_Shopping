package com.wyc.utils;

import com.wyc.security.SecurityUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtTokenUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Autowired
    private RedisCache redisCache;

    private SecretKey getSigningKey() {
        try {
            // 使用 SHA-512 生成一个足够长的密钥
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hash = digest.digest(secret.getBytes(StandardCharsets.UTF_8));
            return Keys.hmacShaKeyFor(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating signing key", e);
        }
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        SecurityUserDetails securityUserDetails = (SecurityUserDetails) userDetails;
        Long userId = securityUserDetails.getUserId();
        claims.put("userId", userId);

        // 生成唯一的token ID
        String tokenId = UUID.randomUUID().toString();
        claims.put("tokenId", tokenId);

        String token = createToken(claims, userId.toString());

        // 将token存储到Redis中
        String redisKey = "token:" + userId;
        redisCache.setCacheObject(redisKey, token, expiration.intValue(), java.util.concurrent.TimeUnit.SECONDS);

        return token;
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return Long.parseLong(claims.getSubject());
        } catch (Exception e) {
            return null;
        }
    }

    public String getTokenIdFromToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return claims.get("tokenId", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final Long userId = getUserIdFromToken(token);
            if (userId == null) {
                return false;
            }
            SecurityUserDetails securityUserDetails = (SecurityUserDetails) userDetails;

            // 检查Redis中是否存在该token
            String redisKey = "token:" + userId;
            String storedToken = redisCache.getCacheObject(redisKey);
            if (storedToken == null || !storedToken.equals(token)) {
                return false;
            }

            return (userId.equals(securityUserDetails.getUserId()) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }

    public void invalidateToken(Long userId) {
        String redisKey = "token:" + userId;
        redisCache.deleteObject(redisKey);
    }
}