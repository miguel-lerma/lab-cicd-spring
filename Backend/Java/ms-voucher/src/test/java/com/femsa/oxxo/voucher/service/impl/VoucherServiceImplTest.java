package com.femsa.oxxo.voucher.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.femsa.oxxo.voucher.client.voucher.IClientVoucher;
import com.femsa.oxxo.voucher.dto.RequestPublicationVoucher;
import com.femsa.oxxo.voucher.dto.RequestRedeemVoucher;
import com.femsa.oxxo.voucher.dto.RequestValidateVoucher;
import com.femsa.oxxo.voucher.dto.ResponsePublicationVoucher;
import com.femsa.oxxo.voucher.dto.ResponseRedeemVoucher;
import com.femsa.oxxo.voucher.dto.ResponseValidateVoucher;
import com.femsa.oxxo.voucher.dto.voucherify.publications.ResponsePublicationsVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.redemptions.ResponseRedeemVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.validations.ResponseValidateVoucherify;
import com.femsa.oxxo.voucher.mapper.voucherify.ResponseVoucherifyMapper;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

@ExtendWith(MockitoExtension.class)
class VoucherServiceImplTest {

	@Mock
	private IClientVoucher clientVoucherify;

	@Mock
	private ResponseVoucherifyMapper responseVoucherifyMapper;

	@InjectMocks
	private VoucherServiceImpl voucherService;

	// === validateVoucher ===
	@Test
	void validateVoucher_Success() {

		RequestValidateVoucher request = new RequestValidateVoucher();
		ResponseValidateVoucherify responseFromClient = new ResponseValidateVoucherify();
		ResponseValidateVoucher mappedResponse = new ResponseValidateVoucher();
		Integer httpStatus = HttpStatus.OK.value();

		when(clientVoucherify.validateVoucher(request)).thenReturn(Mono.just(responseFromClient));
		when(responseVoucherifyMapper.mapValidation(responseFromClient, request))
				.thenReturn(Mono.just(Tuples.of(mappedResponse, httpStatus)));

		Mono<Tuple3<ResponseValidateVoucherify, ResponseValidateVoucher, Integer>> resultMono = voucherService
				.validateVoucher(request);

		StepVerifier.create(resultMono).assertNext(tuple -> {
			assertEquals(responseFromClient, tuple.getT1());
			assertEquals(mappedResponse, tuple.getT2());
			assertEquals(httpStatus, tuple.getT3());
		}).verifyComplete();
	}

	@Test
	void validateVoucher_Error() {

		RequestValidateVoucher request = new RequestValidateVoucher();

		when(clientVoucherify.validateVoucher(request)).thenReturn(Mono.error(new RuntimeException("API failure")));

		Mono<Tuple3<ResponseValidateVoucherify, ResponseValidateVoucher, Integer>> resultMono = voucherService
				.validateVoucher(request);

		StepVerifier.create(resultMono).assertNext(tuple -> {
			assertNotNull(tuple.getT1());
			assertNotNull(tuple.getT2());
			assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), tuple.getT3());
		}).verifyComplete();
	}

	// === redeemVoucher ===

	@Test
	void redeemVoucher_Success() {
		RequestRedeemVoucher request = new RequestRedeemVoucher();
		ResponseRedeemVoucherify clientResponse = new ResponseRedeemVoucherify();
		ResponseRedeemVoucher mappedResponse = new ResponseRedeemVoucher();
		int status = HttpStatus.OK.value();

		when(clientVoucherify.redeemVoucher(request)).thenReturn(Mono.just(clientResponse));
		when(responseVoucherifyMapper.mapRedeem(clientResponse, request))
				.thenReturn(Mono.just(Tuples.of(mappedResponse, status)));

		Mono<Tuple3<ResponseRedeemVoucherify, ResponseRedeemVoucher, Integer>> result = voucherService
				.redeemVoucher(request);

		StepVerifier.create(result).assertNext(tuple -> {
			assertEquals(clientResponse, tuple.getT1());
			assertEquals(mappedResponse, tuple.getT2());
			assertEquals(status, tuple.getT3());
		}).verifyComplete();
	}

	@Test
	void redeemVoucher_Error() {
		RequestRedeemVoucher request = new RequestRedeemVoucher();

		when(clientVoucherify.redeemVoucher(request)).thenReturn(Mono.error(new RuntimeException("API error")));

		Mono<Tuple3<ResponseRedeemVoucherify, ResponseRedeemVoucher, Integer>> result = voucherService
				.redeemVoucher(request);

		StepVerifier.create(result).assertNext(tuple -> {
			assertNotNull(tuple.getT1());
			assertNotNull(tuple.getT2());
			assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), tuple.getT3());
		}).verifyComplete();
	}

	// === publicationVoucher ===

	@Test
	void publicationVoucher_Success() {
		RequestPublicationVoucher request = new RequestPublicationVoucher();
		ResponsePublicationsVoucherify clientResponse = new ResponsePublicationsVoucherify();
		ResponsePublicationVoucher mappedResponse = new ResponsePublicationVoucher();
		int status = HttpStatus.OK.value();

		when(clientVoucherify.publicationVoucher(request)).thenReturn(Mono.just(clientResponse));
		when(responseVoucherifyMapper.mapPublications(clientResponse, request))
				.thenReturn(Mono.just(Tuples.of(mappedResponse, status)));

		Mono<Tuple3<ResponsePublicationsVoucherify, ResponsePublicationVoucher, Integer>> result = voucherService
				.publicationVoucher(request);

		StepVerifier.create(result).assertNext(tuple -> {
			assertEquals(clientResponse, tuple.getT1());
			assertEquals(mappedResponse, tuple.getT2());
			assertEquals(status, tuple.getT3());
		}).verifyComplete();
	}

	@Test
	void publicationVoucher_Error() {
		RequestPublicationVoucher request = new RequestPublicationVoucher();

		when(clientVoucherify.publicationVoucher(request)).thenReturn(Mono.error(new RuntimeException("API error")));

		Mono<Tuple3<ResponsePublicationsVoucherify, ResponsePublicationVoucher, Integer>> result = voucherService
				.publicationVoucher(request);

		StepVerifier.create(result).assertNext(tuple -> {
			assertNotNull(tuple.getT1());
			assertNotNull(tuple.getT2());
			assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), tuple.getT3());
		}).verifyComplete();
	}

}
