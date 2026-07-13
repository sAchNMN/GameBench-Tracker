package com.gamebench.tracker.common.api;

import com.gamebench.tracker.common.error.ErrorCode;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 统一响应中的错误信息。
 */
public record ApiError(String code, String message, Map<String, Object> details) {

    public ApiError {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("code must not be blank");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
        if (details != null && details.values().stream().anyMatch(Throwable.class::isInstance)) {
            throw new IllegalArgumentException("details must not contain Throwable values");
        }
        details = details == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(details));
    }

    public static ApiError of(ErrorCode errorCode) {
        ErrorCode requiredErrorCode = Objects.requireNonNull(errorCode, "errorCode must not be null");
        return new ApiError(requiredErrorCode.code(), requiredErrorCode.defaultMessage(), Map.of());
    }

    public static ApiError of(ErrorCode errorCode, Map<String, Object> details) {
        ErrorCode requiredErrorCode = Objects.requireNonNull(errorCode, "errorCode must not be null");
        return new ApiError(requiredErrorCode.code(), requiredErrorCode.defaultMessage(), details);
    }
}
