package com.gamebench.tracker.common.exception;

import com.gamebench.tracker.common.error.ErrorCode;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 带有稳定错误码的应用异常。
 */
public class ApplicationException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Map<String, Object> details;

    public ApplicationException(ErrorCode errorCode) {
        this(errorCode, null, null);
    }

    public ApplicationException(ErrorCode errorCode, Map<String, Object> details) {
        this(errorCode, details, null);
    }

    public ApplicationException(ErrorCode errorCode, Throwable cause) {
        this(errorCode, null, cause);
    }

    public ApplicationException(ErrorCode errorCode, Map<String, Object> details, Throwable cause) {
        super(requireErrorCode(errorCode).defaultMessage(), cause);
        this.errorCode = errorCode;
        this.details = copyDetails(details);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    private static ErrorCode requireErrorCode(ErrorCode errorCode) {
        return Objects.requireNonNull(errorCode, "errorCode must not be null");
    }

    private static Map<String, Object> copyDetails(Map<String, Object> details) {
        if (details == null) {
            return Map.of();
        }
        if (details.values().stream().anyMatch(Throwable.class::isInstance)) {
            throw new IllegalArgumentException("details must not contain Throwable values");
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(details));
    }
}
