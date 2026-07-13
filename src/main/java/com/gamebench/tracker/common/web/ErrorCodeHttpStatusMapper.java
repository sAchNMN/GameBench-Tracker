package com.gamebench.tracker.common.web;

import com.gamebench.tracker.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Objects;

/**
 * 通用错误码到 HTTP 状态码的映射。
 */
public final class ErrorCodeHttpStatusMapper {

    private ErrorCodeHttpStatusMapper() {
    }

    public static HttpStatus toHttpStatus(ErrorCode errorCode) {
        return switch (Objects.requireNonNull(errorCode, "errorCode must not be null")) {
            case INVALID_REQUEST, VALIDATION_FAILED -> HttpStatus.BAD_REQUEST;
            case RESOURCE_NOT_FOUND, GAME_NOT_FOUND, SCENE_NOT_FOUND, TEMPLATE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
            case INTERNAL_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
