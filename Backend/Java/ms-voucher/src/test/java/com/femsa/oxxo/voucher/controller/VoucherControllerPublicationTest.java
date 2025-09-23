package com.femsa.oxxo.voucher.controller;

import static org.junit.jupiter.api.Assertions.*;
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
import com.femsa.oxxo.voucher.dto.ConsumerDataRequestPublication;
import com.femsa.oxxo.voucher.dto.RequestPublicationVoucher;
import com.femsa.oxxo.voucher.dto.ResponsePublicationVoucher;
import com.femsa.oxxo.voucher.dto.voucherify.publications.ResponsePublicationsVoucherify;
import com.femsa.oxxo.voucher.entity.MsTransaction;
import com.femsa.oxxo.voucher.mapper.voucherify.TransactionLogMapper;
import com.femsa.oxxo.voucher.service.ITransactionLogService;
import com.femsa.oxxo.voucher.service.IVoucherService;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuples;

@ExtendWith(MockitoExtension.class)
class VoucherControllerPublicationTest {

	@InjectMocks
	private VoucherController voucherController;

	@Mock
	private IVoucherService voucherService;

	@Mock
	private ITransactionLogService transactionService;

	@Mock
	private TransactionLogMapper transactionLogMapper;

	private RequestPublicationVoucher request;
	private ResponsePublicationsVoucherify responseVoucherify;
	private ResponsePublicationVoucher responseBody;
	private MsTransaction transaction;
	
	@Mock
	private ObjectMapper objectMapper;

	@BeforeEach
	void setup() {
		request = new RequestPublicationVoucher();
		request.setCampaign("TEST123");
		request.setMemberId("Member123");
		request.setOperation("PUB");

		ConsumerDataRequestPublication consumerData = new ConsumerDataRequestPublication();
		consumerData.setApplication("APP1");
		consumerData.setEntity("ENT");
		consumerData.setOrigin("ORG");
		consumerData.setCrPlace("P001");
		consumerData.setCrStore("S001");
		consumerData.setCashRegister(1);
		request.setConsumerData(consumerData);

		responseBody = new ResponsePublicationVoucher();
		responseBody.setValid(true);

		responseVoucherify = new ResponsePublicationsVoucherify();
		responseVoucherify.setRequestJson("{...}");
		responseVoucherify.setResponseJson("{...}");
		responseVoucherify.setStartTime(OffsetDateTime.now());
		responseVoucherify.setEndTime(OffsetDateTime.now());

		transaction = MsTransaction.builder().coupon("TEST123").build();
		//msLog = MsLog.builder().errorId(400L).build();
	}

	@Test
	    void testPublication_Success() throws Exception {
		
			String rawJson = "{\"coupon\":\"TEST;|\\\\n\",\"transactionId\":\"TX123\"}";
			when(objectMapper.writeValueAsString(request)).thenReturn(rawJson);
			
	        when(voucherService.publicationVoucher(any())).thenReturn(
	            Mono.just(Tuples.of(responseVoucherify, responseBody, 200))
	        );
	        when(transactionService.saveTransaction(any())).thenReturn(Mono.just(transaction));
	        
	        StepVerifier.create(voucherController.publicationVoucher(request))
	            .consumeNextWith(response -> {
	    	        assertEquals(HttpStatus.OK, response.getStatusCode());
	    	        assertTrue(response.getBody().getValid());
	    	    })
	            .verifyComplete();

	        verify(transactionService, times(1)).saveTransaction(any());
	        verify(transactionService, never()).saveTransactionAndLog(any(), any());
	    }

	@Test
	void testPublicationVoucher_Invalid() {
		responseBody.setValid(false);
		when(voucherService.publicationVoucher(any()))
				.thenReturn(Mono.just(Tuples.of(responseVoucherify, responseBody, 400)));
		when(transactionService.saveTransactionAndLog(any(), any())).thenReturn(Mono.empty());

		StepVerifier.create(voucherController.publicationVoucher(request))
				.consumeNextWith(response -> {
	    	        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	    	        assertFalse(response.getBody().getValid());
	    	    })
				.verifyComplete();

		verify(transactionService).saveTransactionAndLog(any(), any());
	}

	@Test
	void testPublication_EmptyResponse() {
	        when(voucherService.publicationVoucher(any())).thenReturn(Mono.empty());

	        StepVerifier.create(voucherController.publicationVoucher(request))
	            .consumeNextWith(response -> {
	    	        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	    	    })
	            .verifyComplete();
	    }
	
	@Test
	void testPublication_Exception() {
	    when(voucherService.publicationVoucher(any()))
	        .thenReturn(Mono.error(new RuntimeException("Error externo")));

	    StepVerifier.create(voucherController.publicationVoucher(request))
	    .consumeNextWith(response -> {
	        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
	        assertEquals("Internal Server Error", response.getBody().getResult().getError().getMessage());
	    })
	    .verifyComplete();
	}

}
