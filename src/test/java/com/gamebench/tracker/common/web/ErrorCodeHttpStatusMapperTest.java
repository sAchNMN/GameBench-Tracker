package com.gamebench.tracker.common.web;

import com.gamebench.tracker.common.error.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErrorCodeHttpStatusMapperTest {

    @Test
    void mapsInvalidRequestToBadRequest() {
        assertEquals(HttpStatus.BAD_REQUEST, ErrorCodeHttpStatusMapper.toHttpStatus(ErrorCode.INVALID_REQUEST));
    }

    @Test
    void mapsValidationFailedToBadRequest() {
        assertEquals(HttpStatus.BAD_REQUEST, ErrorCodeHttpStatusMapper.toHttpStatus(ErrorCode.VALIDATION_FAILED));
    }

    @Test
    void mapsResourceNotFoundToNotFound() {
        assertEquals(HttpStatus.NOT_FOUND, ErrorCodeHttpStatusMapper.toHttpStatus(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    void mapsConflictToConflict() {
        assertEquals(HttpStatus.CONFLICT, ErrorCodeHttpStatusMapper.toHttpStatus(ErrorCode.CONFLICT));
    }

    @Test
    void mapsInternalErrorToInternalServerError() {
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCodeHttpStatusMapper.toHttpStatus(ErrorCode.INTERNAL_ERROR));
    }

    @Test
    void rejectsNullErrorCode() {
        assertThrows(NullPointerException.class, () -> ErrorCodeHttpStatusMapper.toHttpStatus(null));
    }

    @Test
    void mapsEveryCurrentErrorCode() {
        Arrays.stream(ErrorCode.values())
                .forEach(errorCode -> assertDoesNotThrow(() -> ErrorCodeHttpStatusMapper.toHttpStatus(errorCode)));
    }
}
