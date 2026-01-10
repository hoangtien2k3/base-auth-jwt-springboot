package com.jwt.auth.auth_jwt.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogOutRequest {
    @NotNull(message = "Refresh token is required for logout")
    private String refreshToken;
}
