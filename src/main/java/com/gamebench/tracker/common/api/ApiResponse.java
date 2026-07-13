package com.gamebench.tracker.common.api;

import java.time.Instant;
import java.util.Objects;

/**
 * 统一 API 响应数据结构。
 */
public record ApiResponse<T>(boolean success, T data, ApiError error, Instant timestamp) {

    public ApiResponse {
        Objects.requireNonNull(timestamp, "timestamp must not be null");
        if (success && error != null) {
            throw new IllegalArgumentException("successful response must not contain an error");
        }
        if (!success && error == null) {
            throw new IllegalArgumentException("failed response must contain an error");
        }
        if (!success && data != null) {
            throw new IllegalArgumentException("failed response must not contain data");
        }
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, Instant.now());
    }

    public static <T> ApiResponse<T> failure(ApiError error) {
        return new ApiResponse<>(false, null, Objects.requireNonNull(error, "error must not be null"), Instant.now());
    }
}
