package com.gamebench.tracker.common.exception;

import com.gamebench.tracker.common.error.ErrorCode;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApplicationExceptionTest {

    @Test
    void createsExceptionFromErrorCode() {
        ApplicationException exception = new ApplicationException(ErrorCode.INVALID_REQUEST);

        assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());
        assertEquals(ErrorCode.INVALID_REQUEST.defaultMessage(), exception.getMessage());
        assertEquals(Map.of(), exception.getDetails());
        assertThrows(UnsupportedOperationException.class, () -> exception.getDetails().put("field", "value"));
        assertNull(exception.getCause());
    }

    @Test
    void createsUnmodifiableDefensiveCopyOfDetails() {
        Map<String, Object> source = new HashMap<>();
        source.put("field", "name");

        ApplicationException exception = new ApplicationException(ErrorCode.VALIDATION_FAILED, source);
        source.put("field", "changed");

        assertEquals("name", exception.getDetails().get("field"));
        assertThrows(UnsupportedOperationException.class, () -> exception.getDetails().put("another", "value"));
    }

    @Test
    void convertsNullDetailsToEmptyMap() {
        ApplicationException exception = new ApplicationException(ErrorCode.CONFLICT, (Map<String, Object>) null);

        assertEquals(Map.of(), exception.getDetails());
    }

    @Test
    void preservesCauseWithoutAddingItToDetails() {
        Throwable cause = new IllegalStateException("database failure");

        ApplicationException exception = new ApplicationException(ErrorCode.INTERNAL_ERROR, cause);

        assertSame(cause, exception.getCause());
        assertEquals(Map.of(), exception.getDetails());
    }

    @Test
    void preservesDetailsAndCauseTogether() {
        Throwable cause = new IllegalArgumentException("invalid input");
        Map<String, Object> details = Map.of("field", "name");

        ApplicationException exception = new ApplicationException(ErrorCode.VALIDATION_FAILED, details, cause);

        assertEquals(details, exception.getDetails());
        assertSame(cause, exception.getCause());
    }

    @Test
    void rejectsNullErrorCodeFromEveryConstructor() {
        Throwable cause = new IllegalStateException();
        Map<String, Object> details = Map.of("field", "name");

        assertAll(
                () -> assertThrows(NullPointerException.class, () -> new ApplicationException(null)),
                () -> assertThrows(NullPointerException.class, () -> new ApplicationException(null, details)),
                () -> assertThrows(NullPointerException.class, () -> new ApplicationException(null, cause)),
                () -> assertThrows(NullPointerException.class, () -> new ApplicationException(null, details, cause))
        );
    }

    @Test
    void rejectsThrowableValuesFromEveryDetailsConstructor() {
        Map<String, Object> details = Map.of("cause", new IllegalStateException());
        Throwable cause = new IllegalArgumentException();

        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new ApplicationException(ErrorCode.INTERNAL_ERROR, details)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new ApplicationException(ErrorCode.INTERNAL_ERROR, details, cause))
        );
    }
}
