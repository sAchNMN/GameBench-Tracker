package com.gamebench.tracker.common.api;

import com.gamebench.tracker.common.error.ErrorCode;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApiErrorTest {

    @Test
    void createsErrorFromErrorCode() {
        ApiError error = ApiError.of(ErrorCode.RESOURCE_NOT_FOUND);

        assertEquals("RESOURCE_NOT_FOUND", error.code());
        assertEquals("资源不存在", error.message());
        assertEquals(Map.of(), error.details());
    }

    @Test
    void convertsNullDetailsToEmptyMap() {
        ApiError error = ApiError.of(ErrorCode.INVALID_REQUEST, null);

        assertEquals(Map.of(), error.details());
    }

    @Test
    void createsUnmodifiableDefensiveCopyOfDetails() {
        Map<String, Object> source = new HashMap<>();
        source.put("field", "name");

        ApiError error = ApiError.of(ErrorCode.VALIDATION_FAILED, source);
        source.put("field", "changed");

        assertEquals("name", error.details().get("field"));
        assertThrows(UnsupportedOperationException.class,
                () -> error.details().put("anotherField", "value"));
    }

    @Test
    void rejectsNullEmptyOrBlankCode() {
        assertThrows(IllegalArgumentException.class, () -> new ApiError(null, "message", null));
        assertThrows(IllegalArgumentException.class, () -> new ApiError("", "message", null));
        assertThrows(IllegalArgumentException.class, () -> new ApiError("   ", "message", null));
    }

    @Test
    void rejectsNullEmptyOrBlankMessage() {
        assertThrows(IllegalArgumentException.class, () -> new ApiError("CODE", null, null));
        assertThrows(IllegalArgumentException.class, () -> new ApiError("CODE", "", null));
        assertThrows(IllegalArgumentException.class, () -> new ApiError("CODE", "   ", null));
    }

    @Test
    void rejectsThrowableInDetails() {
        assertThrows(IllegalArgumentException.class,
                () -> ApiError.of(ErrorCode.INTERNAL_ERROR, Map.of("cause", new IllegalStateException())));
    }
}
