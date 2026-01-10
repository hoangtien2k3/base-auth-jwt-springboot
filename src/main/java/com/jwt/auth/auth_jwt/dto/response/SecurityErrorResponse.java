package com.jwt.auth.auth_jwt.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class SecurityErrorResponse {
    private int status;
    private String error;
    private String path;
    private String method;
    private String message;
    private Instant timestamp;
}
