package com.auth.jwt.repository;

import com.auth.jwt.domain.UserTokenInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserTokenInfoRepository extends JpaRepository<UserTokenInfo, Long> {
    UserTokenInfo findByTokenIndex(long tokenIndex);

    long findIdByRefreshToken(String refreshToken);
}
