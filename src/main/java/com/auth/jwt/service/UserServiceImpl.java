package com.auth.jwt.service;

import com.auth.jwt.config.JwtUtils;
import com.auth.jwt.domain.User;
import com.auth.jwt.domain.UserTokenInfo;
import com.auth.jwt.dto.TokenResponse;
import com.auth.jwt.dto.UserRequest;
import com.auth.jwt.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final static  String REFRESH_TOKEN_INDEX = "REFRESHTOKENINDEX";

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final UserTokenInfoService userTokenInfoService;

    @Override
    public User createUser(UserRequest userRequest) {
        User user = userRepository.save(User.builder()
                .email(userRequest.getEmail())
                .password(bCryptPasswordEncoder.encode(userRequest.getPassword()))
                .name(userRequest.getName())
                .nickname(StringUtils.isEmpty(userRequest.getNickName()) ? userRequest.getName() : userRequest.getNickName())
                .phoneNo(userRequest.getPhoneNo().replaceAll("-", ""))
                .userRole(userRequest.getUserRole())
                .useYn("Y")
                .build());

        return user;
    }

    @Override
    public User findUser(String email) {
        return Optional.ofNullable(userRepository.findByEmailAndUseYn(email, "Y")).orElseThrow(()->new BadCredentialsException("유효하지 않은 아이디입니다."));
    }

    @Override
    public User findUserById(long id) {
        User user = Optional.ofNullable(userRepository.findById(id)).orElseThrow(()->new BadCredentialsException("유효하지 않은 아이디입니다."));
        return user;
    }

    @Override
    public User findByEmailAndPassword(String email, String password) {
        User user = Optional.ofNullable(userRepository.findByEmailAndUseYn(email, "Y")).orElseThrow(()->new BadCredentialsException("이메일이나 비밀번호를 확인해주세요."));

        if (bCryptPasswordEncoder.matches(password, user.getPassword()) == false) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        return user;
    }

    @Override
    @Transactional
    public User resignUser(long id) {
        User user = userRepository.findById(id);
        user.changeUseYn("N");

        return user;
    }

    @Override
    public boolean validateRegister(String email) {
        if(userRepository.countByEmail(email) > 0) {
            return false;
        }

        return true;
    }

    @Override
    public HttpHeaders setLogout(HttpServletRequest httpServletRequest) {
        long refreshTokenIndex = jwtUtils.getRefreshTokenByCookieIndex(httpServletRequest, REFRESH_TOKEN_INDEX);
        UserTokenInfo userTokenInfo = userTokenInfoService.getTokenInfo(refreshTokenIndex);

        TokenResponse tokenResponse = TokenResponse.builder()
                .id(userTokenInfo.getId())
                .refreshTokenIndex(refreshTokenIndex)
                .refreshToken(null)
                .build();

        jwtUtils.insertRefreshTokenInfo(tokenResponse);

        ResponseCookie responseCookie = ResponseCookie.from(REFRESH_TOKEN_INDEX, null)
                .httpOnly(true)
                .maxAge(0)
                .secure(true)
                .path("/")
                .domain("localhost")
                .secure(true)
                .build();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.SET_COOKIE, responseCookie.toString());

        return httpHeaders;
    }

    @Override
    public ResponseCookie setCookie(TokenResponse tokenResponse) {
//        return ResponseCookie.from(REFRESH_TOKEN_INDEX, String.valueOf(tokenResponse.getRefreshTokenIndex()))
//                .httpOnly(true)
//                .secure(true)
//                .path("/")
//                .domain("localhost")
//                .secure(true)
//                .build();
        return generateRefreshTokenCookie(tokenResponse.getRefreshTokenIndex());
    }

    @Override
    public ResponseCookie setRefreshTokenIndexCookie(long refreshTokenIndex) {
        return generateRefreshTokenCookie(refreshTokenIndex);
    }

    private ResponseCookie generateRefreshTokenCookie(long refreshTokenIndex) {
        return ResponseCookie.from(REFRESH_TOKEN_INDEX, String.valueOf(refreshTokenIndex))
                .httpOnly(true)
                .secure(true)
                .path("/")
                .domain("localhost")
                .secure(true)
                .build();

    }
}
