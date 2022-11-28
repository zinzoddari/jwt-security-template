package com.auth.jwt.controller;

import com.auth.jwt.config.JwtUtils;
import com.auth.jwt.domain.User;
import com.auth.jwt.domain.UserTokenInfo;
import com.auth.jwt.dto.ResultResponse;
import com.auth.jwt.dto.UserRequest;
import com.auth.jwt.dto.UserResponse;
import com.auth.jwt.service.UserService;
import com.auth.jwt.service.UserTokenInfoService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final static  String REFRESH_TOKEN_INDEX = "REFRESHTOKENINDEX";

    private final UserService userService;
    private final UserTokenInfoService userTokenInfoService;
    private final JwtUtils jwtUtils;

    @PostMapping(value = "/join")
    public ResponseEntity<UserResponse> signUp(@RequestBody UserRequest userRequest) {
        if(userService.validateRegister(userRequest.getEmail())) {
            return new ResponseEntity<>(UserResponse.from(userService.createUser(userRequest)), HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

    @PostMapping(value = "/refresh")
    public ResponseEntity<ResultResponse> accessTokenRefresh(HttpServletRequest httpServletRequest) {
        long refreshTokenIndex = jwtUtils.getRefreshTokenByCookieIndex(httpServletRequest, REFRESH_TOKEN_INDEX);

        UserTokenInfo userTokenInfo = userTokenInfoService.getTokenInfo(refreshTokenIndex);

        if(jwtUtils.isValidRefreshToken(userTokenInfo)) {
            User user = userService.findUserById(userTokenInfo.getId());

            return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, jwtUtils.createToken(user))
                    .build();
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .build();
    }

    @PostMapping("/sign-in")
    public ResponseEntity<ResultResponse> login(@RequestBody UserRequest userRequest) {
        User user = userService.findByEmailAndPassword(userRequest.getEmail(), userRequest.getPassword());

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, jwtUtils.createToken(user))
                .header(HttpHeaders.SET_COOKIE, userService.setRefreshTokenIndexCookie(jwtUtils.createRefreshToken(user)).toString())
                .body(ResultResponse.builder()
                    .status(HttpStatus.OK.value())
                    .data(UserResponse.from(user)).build()
                );
    }

    @PostMapping(value = "/sign-out")
    public ResponseEntity<Void> logout(HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok()
                .headers(userService.setLogout(httpServletRequest))
                .build();
    }

    @PostMapping(value = "/resign")
    public ResponseEntity<Void> resign(HttpServletRequest httpServletRequest) {
        Authentication authentication = jwtUtils.getAuthentication(jwtUtils.resolveToken(httpServletRequest));
        User user = (User) authentication.getPrincipal();

        userService.resignUser(user.getId());

        return ResponseEntity.ok()
                .headers(userService.setLogout(httpServletRequest))
                .build();
    }

    @PostMapping(value = "/info/{email}")
    public ResponseEntity<UserResponse> getInfo(@PathVariable String email) {
        return ResponseEntity.ok(UserResponse.from(userService.findUser(email)));
    }
}
