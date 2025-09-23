package com.femsa.oxxo.voucher.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.femsa.oxxo.voucher.dto.ConsumerData;
import com.femsa.oxxo.voucher.dto.RequestRedeemVoucher;
import com.femsa.oxxo.voucher.dto.ResponseRedeemVoucher;
import com.femsa.oxxo.voucher.dto.voucherify.redemptions.ResponseRedeemVoucherify;
import com.femsa.oxxo.voucher.entity.MsTransaction;
import com.femsa.oxxo.voucher.mapper.voucherify.TransactionLogMapper;
import com.femsa.oxxo.voucher.service.ITransactionLogService;
import com.femsa.oxxo.voucher.service.IVoucherService;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuples;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;

@ExtendWith(MockitoExtension.class)
class VoucherControllerRedeemTest {

	@InjectMocks
	private VoucherController voucherController;

	@Mock
	private IVoucherService voucherService;

	@Mock
	private ITransactionLogService transactionService;
	
	@Mock
	private TransactionLogMapper transactionLogMapper;

	private RequestRedeemVoucher request;
	private ResponseRedeemVoucherify responseVoucherify;
	private ResponseRedeemVoucher responseBody;
	private MsTransaction transaction;
	
	@Mock
	private ObjectMapper objectMapper;
	

	@BeforeEach
	void setUp() {
		request = new RequestRedeemVoucher();
		request.setCoupon("TESTCOUPON");
		request.setTransactionId("TX123456");
		request.setTicket(1);
		request.setOperation("RED");
		
		ConsumerData consumerData = new ConsumerData();
		consumerData.setApplication("APP1");
		consumerData.setEntity("ENT");
		consumerData.setOrigin("ORG");
		consumerData.setCrPlace("P001");
		consumerData.setCrStore("S001");
		consumerData.setCashRegister(1);
		request.setConsumerData(consumerData);
		
		responseBody = new ResponseRedeemVoucher();
		responseBody.setValid(true);
		
		responseVoucherify = new ResponseRedeemVoucherify();
		responseVoucherify.setRequestJson("{...}");
		responseVoucherify.setResponseJson("{...}");
		responseVoucherify.setStartTime(OffsetDateTime.now());
		responseVoucherify.setEndTime(OffsetDateTime.now());
		
		transaction = MsTransaction.builder().coupon("TEST123").build();
		
	}

	@Test
	void testRedeemVoucher_ValidTrue() throws Exception {
		
		String rawJson = "{\"coupon\":\"TEST;|\\\\n\",\"transactionId\":\"TX123\"}";
		when(objectMapper.writeValueAsString(request)).thenReturn(rawJson);

		when(voucherService.redeemVoucher(any())).thenReturn(
				Mono.just(Tuples.of(responseVoucherify, responseBody, 200)));

		when(transactionService.saveTransaction(any())).thenReturn(Mono.just(transaction));

		StepVerifier.create(voucherController.redeemVoucher(request)).consumeNextWith(response -> {
			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertNotNull(response.getBody());
			assertTrue(response.getBody().getValid());
		}).verifyComplete();
		
		verify(transactionService, times(1)).saveTransaction(any());
        verify(transactionService, never()).saveTransactionAndLog(any(), any());
	}

	@Test
	void testRedeemVoucher_ValidFalse() {
		responseBody.setValid(false);

		when(voucherService.redeemVoucher(any()))
				.thenReturn(Mono.just(Tuples.of(responseVoucherify, responseBody, 400)));

		when(transactionService.saveTransactionAndLog(any(), any())).thenReturn(Mono.empty());

		StepVerifier.create(voucherController.redeemVoucher(request)).consumeNextWith(response -> {
			assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
			assertNotNull(response.getBody());
			assertFalse(response.getBody().getValid());
		}).verifyComplete();
	}

	@Test
    void testRedeemVoucher_Exception() {
        when(voucherService.redeemVoucher(any()))
            .thenReturn(Mono.error(new RuntimeException("Simulated error")));

        StepVerifier.create(voucherController.redeemVoucher(request))
            .consumeNextWith(response -> {
                assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                assertNotNull(response.getBody());
                assertFalse(response.getBody().getValid());
                assertEquals("Internal Server Error", response.getBody().getResult().getError().getMessage());
            })
            .verifyComplete();
    }

	@Test
    void testRedeemVoucher_SwitchIfEmpty() {
        when(voucherService.redeemVoucher(any())).thenReturn(Mono.empty());

        StepVerifier.create(voucherController.redeemVoucher(request))
            .consumeNextWith(response -> {
                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            })
            .verifyComplete();
    }

}
