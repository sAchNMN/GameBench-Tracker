package com.gamebench.tracker.common.web;

import com.gamebench.tracker.common.api.ApiError;
import com.gamebench.tracker.common.api.ApiResponse;
import com.gamebench.tracker.common.error.ErrorCode;
import com.gamebench.tracker.common.exception.ApplicationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.StringUtils;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 将基础异常转换为统一 API 响应。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiResponse<Void>> handleApplicationException(ApplicationException exception) {
        return buildResponse(exception.getErrorCode(), exception.getDetails());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception) {
        List<Map<String, String>> fieldErrors = exception.getBindingResult().getFieldErrors().stream()
                .sorted(Comparator.comparing(FieldError::getField)
                        .thenComparing(error -> Objects.toString(error.getDefaultMessage(), "")))
                .map(error -> Map.of(
                        "field", error.getField(),
                        "message", Objects.toString(error.getDefaultMessage(), "")))
                .toList();
        return buildResponse(ErrorCode.VALIDATION_FAILED, Map.of("fieldErrors", List.copyOf(fieldErrors)));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException exception) {
        List<Map<String, String>> violations = exception.getConstraintViolations().stream()
                .sorted(Comparator.comparing((ConstraintViolation<?> violation) -> violation.getPropertyPath().toString())
                        .thenComparing((ConstraintViolation<?> violation) -> Objects.toString(violation.getMessage(), "")))
                .map(violation -> Map.of(
                        "path", violation.getPropertyPath().toString(),
                        "message", Objects.toString(violation.getMessage(), "")))
                .toList();
        return buildResponse(ErrorCode.VALIDATION_FAILED, Map.of("violations", List.copyOf(violations)));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleHandlerMethodValidation(
            HandlerMethodValidationException exception) {
        List<Map<String, String>> violations = exception.getAllValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> Map.of(
                                "path", resolveParameterPath(result),
                                "message", Objects.toString(error.getDefaultMessage(), ""))))
                .sorted(Comparator.comparing((Map<String, String> violation) -> violation.get("path"))
                        .thenComparing(violation -> violation.get("message")))
                .toList();
        return buildResponse(ErrorCode.VALIDATION_FAILED, Map.of("violations", List.copyOf(violations)));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameter(
            MissingServletRequestParameterException exception) {
        return buildResponse(ErrorCode.INVALID_REQUEST, Map.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException exception) {
        return buildResponse(ErrorCode.INVALID_REQUEST, Map.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        return buildResponse(ErrorCode.CONFLICT, Map.of());
    }
    @ExceptionHandler(UncategorizedSQLException.class)
    public ResponseEntity<ApiResponse<Void>> handleUncategorizedSql(UncategorizedSQLException exception) {
        if (exception.getSQLException() != null && exception.getSQLException().getErrorCode() == 19) {
            return buildResponse(ErrorCode.CONFLICT, Map.of());
        }
        return handleUnexpectedException(exception);
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException exception) {
        return buildResponse(ErrorCode.INVALID_REQUEST, Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception exception) {
        LOGGER.error("Unhandled application exception", exception);
        return buildResponse(ErrorCode.INTERNAL_ERROR, Map.of());
    }

    private ResponseEntity<ApiResponse<Void>> buildResponse(ErrorCode errorCode, Map<String, Object> details) {
        ApiError error = ApiError.of(errorCode, details);
        ApiResponse<Void> response = ApiResponse.failure(error);
        HttpStatus status = ErrorCodeHttpStatusMapper.toHttpStatus(errorCode);
        return ResponseEntity.status(status).body(response);
    }

    private String resolveParameterPath(ParameterValidationResult result) {
        MethodParameter parameter = result.getMethodParameter();
        RequestParam requestParam = parameter.getParameterAnnotation(RequestParam.class);
        if (requestParam != null) {
            String name = firstNonBlank(requestParam.name(), requestParam.value());
            if (name != null) {
                return name;
            }
        }
        PathVariable pathVariable = parameter.getParameterAnnotation(PathVariable.class);
        if (pathVariable != null) {
            String name = firstNonBlank(pathVariable.name(), pathVariable.value());
            if (name != null) {
                return name;
            }
        }
        if (StringUtils.hasText(parameter.getParameterName())) {
            return parameter.getParameterName();
        }
        return "arg" + parameter.getParameterIndex();
    }

    private String firstNonBlank(String first, String second) {
        if (StringUtils.hasText(first)) {
            return first;
        }
        return StringUtils.hasText(second) ? second : null;
    }
}