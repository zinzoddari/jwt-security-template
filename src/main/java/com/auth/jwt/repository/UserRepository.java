package com.auth.jwt.repository;

import com.auth.jwt.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmailAndUseYn(String email, String useYn);
    User findById(long id);

    Long countByEmail(String email);
}
