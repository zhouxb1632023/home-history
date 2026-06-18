package com.homehistory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank @Size(min = 2, max = 50)
    private String username;
    @NotBlank @Size(min = 4, max = 100)
    private String password;
    @Size(max = 50)
    private String nickname;
}
