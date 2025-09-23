package com.femsa.oxxo.voucher.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.femsa.oxxo.voucher.utils.ValidatorUtil;

import jakarta.validation.ConstraintViolation;

class RequestPublicationVoucherTest {

	@Test
	void validRequestShouldPassValidation() {
		RequestPublicationVoucher request = new RequestPublicationVoucher();
		request.setOperation("PUB");
		request.setDate("20240514");
		request.setHour("150000");
		request.setUser("USER1");
		request.setMemberId("MEM123");
		request.setCampaign("CAMP_1");
		request.setCount(5);
		request.setConsumerData(new ConsumerDataRequestPublication());

		Set<ConstraintViolation<RequestPublicationVoucher>> violations = ValidatorUtil.getValidator().validate(request);
		assertTrue(violations.isEmpty());
	}

	@Test
	void validCountShouldDefaultToOneIfNullOrZero() {
		RequestPublicationVoucher request = new RequestPublicationVoucher();

		request.setCount(null);
		request.validCount();
		assertEquals(1, request.getCount());

		request.setCount(0);
		request.validCount();
		assertEquals(1, request.getCount());
	}

	@Test
	void invalidRequestShouldReturnViolations() {
		RequestPublicationVoucher request = new RequestPublicationVoucher(); // vacío
		Set<ConstraintViolation<RequestPublicationVoucher>> violations = ValidatorUtil.getValidator().validate(request);
		assertFalse(violations.isEmpty());
		assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("obligatorio")));
	}

}
