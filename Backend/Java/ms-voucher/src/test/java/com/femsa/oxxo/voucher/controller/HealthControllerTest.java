package com.femsa.oxxo.voucher.controller;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

class HealthControllerTest {

	@Test
	void status_shouldReturnOkResponse() {

		HealthController controller = new HealthController();

		ResponseEntity<String> response = controller.status();

		assertNotNull(response);
		assertEquals("OK", response.getBody());
	}
}