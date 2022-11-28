package com.auth.jwt.service;

import com.auth.jwt.domain.User;
import com.auth.jwt.dto.TokenResponse;
import com.auth.jwt.dto.UserRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

public interface UserService {
    User createUser(UserRequest userRequest);

    User findUser(String email);
    User findUserById(long id);

    User findByEmailAndPassword(String email, String password);

    User resignUser(long id);

    boolean validateRegister(String email);

    HttpHeaders setLogout(HttpServletRequest httpServletRequest);
    ResponseCookie setCookie(TokenResponse tokenResponse);

    ResponseCookie setRefreshTokenIndexCookie(long refreshToken);
}
