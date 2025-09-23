package com.femsa.oxxo.voucher.dto.voucherify.validations;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;

class ResponseValidateVoucherifyTest {

	@Test
	void testConstructorAndGetters() {
		ResponseValidations response = new ResponseValidations(); // puedes mockear si lo necesitas
		OffsetDateTime startTime = OffsetDateTime.now().minusSeconds(1);
		OffsetDateTime endTime = OffsetDateTime.now();
		String requestJson = "{\"voucher\":\"ABC123\"}";
		String responseJson = "{\"status\":\"VALID\"}";

		ResponseValidateVoucherify dto = new ResponseValidateVoucherify(response, startTime, endTime, requestJson,
				responseJson);

		assertEquals(response, dto.getResponse());
		assertEquals(startTime, dto.getStartTime());
		assertEquals(endTime, dto.getEndTime());
		assertEquals(requestJson, dto.getRequestJson());
		assertEquals(responseJson, dto.getResponseJson());
	}

	@Test
	void testSetters() {
		ResponseValidateVoucherify dto = new ResponseValidateVoucherify();
		OffsetDateTime now = OffsetDateTime.now();

		dto.setResponse(new ResponseValidations());
		dto.setStartTime(now);
		dto.setEndTime(now);
		dto.setRequestJson("req");
		dto.setResponseJson("res");

		assertNotNull(dto.getResponse());
		assertEquals("req", dto.getRequestJson());
		assertEquals("res", dto.getResponseJson());
	}
}
