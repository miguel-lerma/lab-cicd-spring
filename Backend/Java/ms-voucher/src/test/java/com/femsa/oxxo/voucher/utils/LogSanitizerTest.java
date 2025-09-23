package com.femsa.oxxo.voucher.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class LogSanitizerTest {
	
    @Test
    void sanitize_nullInput_returnsNull() {
        String result = LogSanitizer.sanitize(null);
        assertNull(result);
    }

    @Test
    void sanitize_emptyString_returnsEmpty() {
        String result = LogSanitizer.sanitize("");
        assertEquals("", result);
    }

    @Test
    void sanitize_removesControlCharacters() {
        String input = "Line1\nLine2\rTab\tBack\b";
        String expected = "Line1Line2TabBack";
        String result = LogSanitizer.sanitize(input);
        assertEquals(expected, result);
    }

    @Test
    void sanitize_replacesDangerousCharacters() {
        String input = "Hello; DROP TABLE 'users' | \\";
        String expected = "Hello_ DROP TABLE _users_ _ _";
        String result = LogSanitizer.sanitize(input);
        assertEquals(expected, result);
    }

    @Test
    void sanitize_mixedCharacters_sanitizedCorrectly() {
        String input = "Coupon: ABC123\nTransactionId: 456;\\";
        String expected = "Coupon: ABC123TransactionId: 456__";
        String result = LogSanitizer.sanitize(input);
        assertEquals(expected, result);
    }

    @Test
    void sanitize_inputWithoutSpecialCharacters_returnsSame() {
        String input = "SafeString123";
        String result = LogSanitizer.sanitize(input);
        assertEquals(input, result);
    }

}
