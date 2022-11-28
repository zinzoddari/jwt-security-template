package com.auth.jwt.config;

import com.auth.jwt.domain.User;
import com.auth.jwt.domain.UserTokenInfo;
import com.auth.jwt.dto.TokenResponse;
import com.auth.jwt.service.UserTokenInfoService;
import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtils {
    @Value("${jwt.token.access}")
    private String secretKey;

    @Value("${jwt.token.refresh}")
    private String refreshKey;

    @Value("${jwt.expire.time}")
    private long expireTime;

    private final UserDetailsService userDetailsService;
    private final UserTokenInfoService userTokenInfoService;

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    public String createToken(User user) {
        Date now = new Date();
        return Jwts.builder()
                .setClaims(createClaims(user))
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expireTime))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public long createRefreshToken(User user) {
        String refreshToken = Jwts.builder()
                .setSubject(user.getEmail())
                .setHeader(createHeader())
                .setClaims(createClaims(user))
                .setExpiration(createExpireDate(1000 * 60 * 10))
                .signWith(SignatureAlgorithm.HS256, refreshKey)
                .compact();

        TokenResponse tokenResponse = TokenResponse.builder()
                .id(user.getId())
                .refreshToken(refreshToken)
                .build();

        return userTokenInfoService.createUserTokenInfo(tokenResponse).getTokenIndex();
    }

    public Authentication getAuthentication(String token) {
        String email = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String resolveToken(HttpServletRequest request) {
        return request.getHeader(HttpHeaders.AUTHORIZATION);
    }

    public boolean validateToken(String jwtToken) {

        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwtToken);
            return claims.getBody().getExpiration().before(new Date()) == false;
        } catch (SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }

        return false;
    }

    public boolean isValidRefreshToken(UserTokenInfo userTokenInfo) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(refreshKey).parseClaimsJws(userTokenInfo.getRefreshToken());
            return claims.getBody().getExpiration().before(new Date()) == false;
        } catch (SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("refreshToken 재발급");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }

        return false;
    }

    private Map<String, Object> createClaims(User user) {
        Claims claims = Jwts.claims().setSubject(user.getEmail());
        claims.put("id", user.getId());
        claims.put("roles", Arrays.asList(user.getUserRole()));
        return claims;
    }

    private Map<String, Object> createHeader() {
        Map<String, Object> header = new HashMap<>();

        header.put("typ", "ACCESS_TOKEN");
        header.put("alg", "HS256");
        header.put("regDate", System.currentTimeMillis());

        return header;
    }

    private Date createExpireDate(long expireDate) {
        long curTime = System.currentTimeMillis();
        return new Date(curTime + expireDate);
    }

    public long getRefreshTokenByCookieIndex(HttpServletRequest httpServletRequest, String cookieName) {
        Cookie[] cookies = httpServletRequest.getCookies();

        for (Cookie cookie : cookies) {
            if(cookieName.equals(cookie.getName()))
                return Long.parseLong(cookie.getValue());
        }

        return 0;
    }

    public long insertRefreshTokenInfo(TokenResponse tokenResponse) {
        return userTokenInfoService.clearUserTokenInfo(tokenResponse).getTokenIndex();
    }
}
