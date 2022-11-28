package com.auth.jwt.service;

import com.auth.jwt.domain.UserTokenInfo;
import com.auth.jwt.dto.TokenResponse;
import com.auth.jwt.repository.UserTokenInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserTokenInfoServiceImpl implements UserTokenInfoService {
    private final UserTokenInfoRepository userTokenInfoRepository;

    @Override
    public UserTokenInfo createUserTokenInfo(TokenResponse tokenResponse) {
        return userTokenInfoRepository.save(UserTokenInfo.builder()
                .id(tokenResponse.getId())
                .refreshToken(tokenResponse.getRefreshToken())
                .build());
    }

    @Override
    public UserTokenInfo clearUserTokenInfo(TokenResponse tokenResponse) {
        return userTokenInfoRepository.save(UserTokenInfo.builder()
                .id(tokenResponse.getId())
                .tokenIndex(tokenResponse.getRefreshTokenIndex())
                .refreshToken(tokenResponse.getRefreshToken())
                .build());
    }

    @Override
    public UserTokenInfo getTokenInfo(long tokenIndex) {
        return userTokenInfoRepository.findByTokenIndex(tokenIndex);
    }
}
