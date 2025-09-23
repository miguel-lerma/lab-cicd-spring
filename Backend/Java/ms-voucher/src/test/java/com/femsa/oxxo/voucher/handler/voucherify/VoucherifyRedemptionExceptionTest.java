package com.femsa.oxxo.voucher.handler.voucherify;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.femsa.oxxo.voucher.dto.voucherify.redemptions.ResponseRedeem;

class VoucherifyRedemptionExceptionTest {

	@Test
	void testVoucherifyRedemptionExceptionStoresResponse() {
		// Arrange
		ResponseRedeem mockResponse = new ResponseRedeem(); // o usa Mockito.mock(ResponseRedeem.class)

		// Act
		VoucherifyRedemptionException exception = new VoucherifyRedemptionException(mockResponse);

		// Assert
		assertEquals(mockResponse, exception.getResponse());
		assertTrue(exception instanceof RuntimeException);
	}
}
