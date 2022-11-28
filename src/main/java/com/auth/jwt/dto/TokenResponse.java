package com.auth.jwt.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TokenResponse {
    private long index;
    private long id;
    private long refreshTokenIndex;

    private String accessToken;
    private String refreshToken;
}
