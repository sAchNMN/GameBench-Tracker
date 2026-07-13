package com.gamebench.tracker.common.web;

import com.gamebench.tracker.common.api.ApiResponse;
import com.gamebench.tracker.common.error.ErrorCode;
import com.gamebench.tracker.common.exception.ApplicationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void convertsApplicationConflictWithoutExposingCause() {
        Throwable cause = new IllegalStateException("internal cause");
        Map<String, Object> details = Map.of("field", "name");

        ResponseEntity<ApiResponse<Void>> response = handler.handleApplicationException(
                new ApplicationException(ErrorCode.CONFLICT, details, cause));

        assertFailure(response, HttpStatus.CONFLICT, ErrorCode.CONFLICT);
        assertEquals(details, response.getBody().error().details());
        assertFalse(response.getBody().error().details().containsKey("cause"));
        assertFalse(response.getBody().error().message().contains("internal cause"));
    }

    @Test
    void convertsApplicationResourceNotFound() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleApplicationException(
                new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND));

        assertFailure(response, HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    void convertsUnreadableRequestWithoutLeakingParserMessage() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleHttpMessageNotReadable(
                new HttpMessageNotReadableException("parser details must not leak", new MockHttpInputMessage(new byte[0])));

        assertFailure(response, HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST);
        assertEquals(Map.of(), response.getBody().error().details());
        assertFalse(response.getBody().error().message().contains("parser details must not leak"));
    }

    @Test
    void convertsUnexpectedExceptionWithoutLeakingMessage() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleUnexpectedException(
                new IllegalStateException("unexpected details must not leak"));

        assertFailure(response, HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
        assertEquals(Map.of(), response.getBody().error().details());
        assertFalse(response.getBody().error().message().contains("unexpected details must not leak"));
    }

    private void assertFailure(
            ResponseEntity<ApiResponse<Void>> response,
            HttpStatus expectedStatus,
            ErrorCode expectedErrorCode) {
        assertEquals(expectedStatus, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().success());
        assertNull(response.getBody().data());
        assertNotNull(response.getBody().error());
        assertEquals(expectedErrorCode.code(), response.getBody().error().code());
        assertNotNull(response.getBody().timestamp());
    }
}
