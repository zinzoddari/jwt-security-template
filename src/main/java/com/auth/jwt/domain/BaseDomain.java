package com.auth.jwt.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseDomain {
    @CreatedDate
    @Column(name = "reg_date", nullable = false, updatable = false)
    private LocalDateTime regDate;

    @CreatedBy
    @Column(name = "reg_id", nullable = false, updatable = false)
    private String regId;

    @LastModifiedDate
    @Column(name = "upd_date")
    private LocalDateTime updDate;

    @LastModifiedBy
    @Column(name = "upd_id")
    private String updId;
}
