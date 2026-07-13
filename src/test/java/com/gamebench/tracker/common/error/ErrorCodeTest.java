package com.gamebench.tracker.common.error;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErrorCodeTest {

    @Test
    void allCodesAndMessagesAreNonBlank() {
        for (ErrorCode errorCode : ErrorCode.values()) {
            assertFalse(errorCode.code().isBlank());
            assertFalse(errorCode.defaultMessage().isBlank());
        }
    }

    @Test
    void allCodesAreUnique() {
        Set<String> codes = Arrays.stream(ErrorCode.values())
                .map(ErrorCode::code)
                .collect(Collectors.toSet());

        assertEquals(ErrorCode.values().length, codes.size());
    }

    @Test
    void allCodesUseUppercaseUnderscoreFormat() {
        for (ErrorCode errorCode : ErrorCode.values()) {
            assertTrue(errorCode.code().matches("[A-Z0-9_]+"));
        }
    }
}
