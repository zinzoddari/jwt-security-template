package com.auth.jwt.service;


import com.auth.jwt.domain.UserTokenInfo;
import com.auth.jwt.dto.TokenResponse;

public interface UserTokenInfoService {
    UserTokenInfo createUserTokenInfo(TokenResponse tokenResponse);
    UserTokenInfo clearUserTokenInfo(TokenResponse tokenResponse);
    UserTokenInfo getTokenInfo(long refreshTokenIndex);
}
