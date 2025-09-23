package com.femsa.oxxo.voucher.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ResultTest {

	@Test
	void testCreateResultWithError() {
		Error error = new Error();
		error.setCode(404);
		error.setMessage("Not Found");

		Result result = new Result();
		result.setError(error);

		assertNotNull(result.getError());
		assertEquals(404, result.getError().getCode());
		assertEquals("Not Found", result.getError().getMessage());
	}

	@Test
	void testCreateResultWithoutError() {
		Result result = new Result();
		assertNull(result.getError());
	}

}
