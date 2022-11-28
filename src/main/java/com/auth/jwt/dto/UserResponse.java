package com.auth.jwt.dto;

import com.auth.jwt.domain.User;
import lombok.Getter;

@Getter
public class UserResponse {
    private String email;
    private String name;
    private String phoneNo;
    private String nickName;

    public UserResponse(String email, String name, String phoneNo, String nickName) {
        this.email = email;
        this.name = name;
        this.phoneNo = phoneNo;
        this.nickName = nickName;
    }

    public static UserResponse from(User user) {
        return new UserResponse(user.getEmail(), user.getName(), user.getPhoneNo(), user.getNickname());
    }
}
