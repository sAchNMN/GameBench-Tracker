package com.gamebench.tracker.common.api;

import com.gamebench.tracker.common.error.ErrorCode;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiResponseTest {

    @Test
    void successCreatesConsistentResponse() {
        ApiResponse<String> response = ApiResponse.success("benchmark");

        assertTrue(response.success());
        assertEquals("benchmark", response.data());
        assertNull(response.error());
        assertNotNull(response.timestamp());
    }

    @Test
    void failureCreatesConsistentResponse() {
        ApiError error = ApiError.of(ErrorCode.INVALID_REQUEST);

        ApiResponse<String> response = ApiResponse.failure(error);

        assertFalse(response.success());
        assertNull(response.data());
        assertEquals(error, response.error());
        assertNotNull(response.timestamp());
    }

    @Test
    void rejectsSuccessfulResponseWithError() {
        ApiError error = ApiError.of(ErrorCode.INVALID_REQUEST);

        assertThrows(IllegalArgumentException.class,
                () -> new ApiResponse<>(true, "data", error, Instant.now()));
    }

    @Test
    void rejectsFailedResponseWithoutError() {
        assertThrows(IllegalArgumentException.class,
                () -> new ApiResponse<>(false, null, null, Instant.now()));
    }

    @Test
    void rejectsNullTimestamp() {
        assertThrows(NullPointerException.class,
                () -> new ApiResponse<>(true, "data", null, null));
    }
}
