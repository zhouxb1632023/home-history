package com.homehistory.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String username;
    private String nickname;
    private String avatarUrl;
    private String role;

    public LoginResponse(String token, String username, String nickname, String avatarUrl, String role) {
        this.token = token;
        this.username = username;
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
        this.role = role;
    }
}
