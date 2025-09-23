package com.femsa.oxxo.voucher.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.femsa.oxxo.voucher.dto.ConsumerData;
import com.femsa.oxxo.voucher.dto.RequestValidateVoucher;
import com.femsa.oxxo.voucher.dto.ResponseValidateVoucher;
import com.femsa.oxxo.voucher.dto.voucherify.validations.ResponseValidateVoucherify;
import com.femsa.oxxo.voucher.entity.MsLog;
import com.femsa.oxxo.voucher.entity.MsTransaction;
import com.femsa.oxxo.voucher.mapper.voucherify.TransactionLogMapper;
import com.femsa.oxxo.voucher.service.ITransactionLogService;
import com.femsa.oxxo.voucher.service.IVoucherService;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuples;

@ExtendWith(MockitoExtension.class)
class VoucherControllerValidateTest {

	@InjectMocks
	private VoucherController voucherController;

	@Mock
	private IVoucherService voucherService;

	@Mock
	private ITransactionLogService transactionService;

	@Mock
	private TransactionLogMapper transactionLogMapper;

	private RequestValidateVoucher request;
	private ResponseValidateVoucherify responseVoucherify;
	private ResponseValidateVoucher responseBody;
	private MsTransaction transaction;
	private MsLog msLog;

	@Mock
	private ObjectMapper objectMapper;

	@BeforeEach
	void setup() {
		request = new RequestValidateVoucher();
		request.setCoupon("TEST123");
		request.setTransactionId("TX123");
		request.setTicket(1);
		request.setOperation("VAL");

		ConsumerData consumerData = new ConsumerData();
		consumerData.setApplication("APP1");
		consumerData.setEntity("ENT");
		consumerData.setOrigin("ORG");
		consumerData.setCrPlace("P001");
		consumerData.setCrStore("S001");
		consumerData.setCashRegister(1);
		request.setConsumerData(consumerData);

		responseBody = new ResponseValidateVoucher();
		responseBody.setValid(true);

		responseVoucherify = new ResponseValidateVoucherify();
		responseVoucherify.setRequestJson("{...}");
		responseVoucherify.setResponseJson("{...}");
		responseVoucherify.setStartTime(OffsetDateTime.now());
		responseVoucherify.setEndTime(OffsetDateTime.now());

		transaction = MsTransaction.builder().coupon("TEST123").build();
		msLog = MsLog.builder().errorId(400L).build();

	}

	@Test
	    void testValidateVoucher_Success() throws Exception {
			String rawJson = "{\"coupon\":\"TEST;|\\\\n\",\"transactionId\":\"TX123\"}";
			when(objectMapper.writeValueAsString(request)).thenReturn(rawJson);
		
	        when(voucherService.validateVoucher(any())).thenReturn(
	            Mono.just(Tuples.of(responseVoucherify, responseBody, 200))
	        );
	        when(transactionService.saveTransaction(any())).thenReturn(Mono.just(transaction));
	        
	        StepVerifier.create(voucherController.validateVoucher(request))
	            .consumeNextWith(response -> {
	    	        assertEquals(HttpStatus.OK, response.getStatusCode());
	    	        assertTrue(response.getBody().getValid());
	    	    })
	            .verifyComplete();

	        verify(transactionService, times(1)).saveTransaction(any());
	        verify(transactionService, never()).saveTransactionAndLog(any(), any());
	    }

	@Test
	void testValidateVoucher_InvalidCoupon() {
		responseBody.setValid(false);
		when(voucherService.validateVoucher(any()))
				.thenReturn(Mono.just(Tuples.of(responseVoucherify, responseBody, 400)));
		when(transactionLogMapper.toLogError(any(), any())).thenReturn(Mono.just(msLog));
		when(transactionService.saveTransactionAndLog(any(), any())).thenReturn(Mono.empty());

		StepVerifier.create(voucherController.validateVoucher(request)).consumeNextWith(response -> {
			assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
			assertFalse(response.getBody().getValid());
		}).verifyComplete();

		verify(transactionService).saveTransactionAndLog(any(), any());
	}

	@Test
	void testValidateVoucher_EmptyResponse() {
	        when(voucherService.validateVoucher(any())).thenReturn(Mono.empty());

	        StepVerifier.create(voucherController.validateVoucher(request))
	            .consumeNextWith(response -> {
	    	        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	    	    })
	            .verifyComplete();
	    }

	@Test
	void testValidateVoucher_Exception() {
	    when(voucherService.validateVoucher(any()))
	        .thenReturn(Mono.error(new RuntimeException("Error externo")));

	    StepVerifier.create(voucherController.validateVoucher(request))
	    .consumeNextWith(response -> {
	        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
	        assertEquals("Internal Server Error", response.getBody().getResult().getError().getMessage());
	    })
	    .verifyComplete();
	}

}
