package com.femsa.oxxo.voucher.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ErrorTest {

	@Test
	void testErrorFields() {
		Error error = new Error();
		error.setCode(500);
		error.setMessage("Internal Server Error");

		assertEquals(500, error.getCode());
		assertEquals("Internal Server Error", error.getMessage());
	}

	@Test
	void testErrorEmptyConstructor() {
		Error error = new Error();

		assertEquals(0, error.getCode()); // default int value
		assertNull(error.getMessage());
	}

}
