package com.jwt.auth.auth_jwt.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiBaseResponse<T> {
    private int status;
    private boolean success;
    private String message;
    private T data;

    public static <T> ApiBaseResponse<T> success(T data, String message, int status) {
        return ApiBaseResponse.<T>builder()
                .success(true)
                .status(status)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiBaseResponse<T> success(T data) {
        return success(data, "Success", HttpStatus.OK.value());
    }

    public static <T> ApiBaseResponse<T> success(T data, String message) {
        return success(data, message, HttpStatus.OK.value());
    }

    public static <T> ApiBaseResponse<T> created(T data, String message) {
        return success(data, message, HttpStatus.CREATED.value());
    }

    public static <T> ApiBaseResponse<T> error(String message, int status) {
        return ApiBaseResponse.<T>builder()
                .success(false)
                .status(status)
                .message(message)
                .build();
    }
}
