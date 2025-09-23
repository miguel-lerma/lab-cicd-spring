package com.femsa.oxxo.voucher.client.voucher.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.femsa.oxxo.voucher.config.App;
import com.femsa.oxxo.voucher.config.Url;
import com.femsa.oxxo.voucher.config.VoucherifyProperties;
import com.femsa.oxxo.voucher.dto.RequestPublicationVoucher;
import com.femsa.oxxo.voucher.dto.RequestRedeemVoucher;
import com.femsa.oxxo.voucher.dto.RequestValidateVoucher;
import com.femsa.oxxo.voucher.dto.voucherify.publications.RequestPublicationsVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.publications.ResponsePublications;
import com.femsa.oxxo.voucher.dto.voucherify.publications.ResponsePublicationsVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.redemptions.RequestRedeemVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.redemptions.ResponseRedeem;
import com.femsa.oxxo.voucher.dto.voucherify.redemptions.ResponseRedeemVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.validations.RequestValidationsVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.validations.ResponseValidateVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.validations.ResponseValidations;
import com.femsa.oxxo.voucher.handler.voucherify.VoucherifyResponseHandler;
import com.femsa.oxxo.voucher.mapper.voucherify.RequestVoucherifyMapper;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class ClientVoucherifyImplTest {

	@Mock
	private WebClient.Builder webClientBuilder;
	@Mock
	private WebClient webClient;
	@Mock
	private WebClient.RequestBodyUriSpec requestBodyUriSpec;
	@Mock
	private WebClient.RequestBodySpec requestBodySpec;
	@Mock
	@SuppressWarnings("rawtypes")
	private WebClient.RequestHeadersSpec requestHeadersSpec;
	@Mock
	private WebClient.ResponseSpec responseSpec;
	@Mock
	private VoucherifyResponseHandler responseHandler;
	@Mock
	private ObjectMapper objectMapper;
	@Mock
	private RequestVoucherifyMapper voucherifyMapper;
	@Mock
	private VoucherifyProperties properties;
	@Mock
	private Url urlEndpoints;
	@Mock
	private App app;

	private ClientVoucherifyImpl client;

	@BeforeEach
    void setUp() {
    	when(properties.getUrl()).thenReturn("http://dummy.url");
    	when(properties.getUrlEndpoint()).thenReturn(urlEndpoints);
    	lenient().when(urlEndpoints.getValidate()).thenReturn("/validate");
    	lenient().when(urlEndpoints.getRedeem()).thenReturn("/redeem");
    	lenient().when(urlEndpoints.getPublication()).thenReturn("/publication");

    	when(properties.getApp()).thenReturn(app);
    	when(app.getId()).thenReturn("app-id");
    	when(app.getToken()).thenReturn("app-token");
    	when(properties.getChannel()).thenReturn("channel");

        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);

        client = new ClientVoucherifyImpl(webClientBuilder, responseHandler, objectMapper, voucherifyMapper, properties);
    }

	@Test
	void validateVoucher_success() throws JsonProcessingException {
		// Arrange
		RequestValidateVoucher request = new RequestValidateVoucher();
		RequestValidationsVoucherify mappedRequest = new RequestValidationsVoucherify();
		ResponseValidations response = new ResponseValidations();

		when(voucherifyMapper.mapRequestValidate(any())).thenReturn(mappedRequest);
		when(objectMapper.writeValueAsString(any())).thenReturn("{}");

		when(webClient.post()).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
		when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
		when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
		when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
		when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
		when(responseSpec.bodyToMono(ResponseValidations.class)).thenReturn(Mono.just(response));

		// Act
		Mono<ResponseValidateVoucherify> result = client.validateVoucher(request);

		// Assert
		StepVerifier.create(result).assertNext(res -> {
			assertEquals(response, res.getResponse());
			assertNotNull(res.getStartTime());
			assertNotNull(res.getEndTime());
			assertNotNull(res.getRequestJson());
			assertNotNull(res.getResponseJson());
		}).verifyComplete();
	}

	@Test
	void redeemVoucher_success() throws JsonProcessingException {
		// Arrange
		RequestRedeemVoucher request = new RequestRedeemVoucher();
		RequestRedeemVoucherify mappedRequest = new RequestRedeemVoucherify();
		ResponseRedeem response = new ResponseRedeem();

		when(voucherifyMapper.mapRequestRedeem(any())).thenReturn(mappedRequest);
		when(objectMapper.writeValueAsString(any())).thenReturn("{}");

		when(webClient.post()).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
		when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
		when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
		when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
		when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
		when(responseSpec.bodyToMono(ResponseRedeem.class)).thenReturn(Mono.just(response));

		// Act
		Mono<ResponseRedeemVoucherify> result = client.redeemVoucher(request);

		// Assert
		StepVerifier.create(result).assertNext(res -> {
			assertEquals(response, res.getResponse());
			assertNotNull(res.getStartTime());
			assertNotNull(res.getEndTime());
			assertNotNull(res.getRequestJson());
			assertNotNull(res.getResponseJson());
		}).verifyComplete();
	}

	@Test
	void publicationVoucher_success() throws JsonProcessingException {
		// Arrange
		RequestPublicationVoucher request = new RequestPublicationVoucher();
		RequestPublicationsVoucherify mappedRequest = new RequestPublicationsVoucherify();
		ResponsePublications response = new ResponsePublications();

		when(voucherifyMapper.mapRequestPublication(any())).thenReturn(mappedRequest);
		when(objectMapper.writeValueAsString(any())).thenReturn("{}");

		when(webClient.post()).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
		when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
		when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
		when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
		when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
		when(responseSpec.bodyToMono(ResponsePublications.class)).thenReturn(Mono.just(response));

		// Act
		Mono<ResponsePublicationsVoucherify> result = client.publicationVoucher(request);

		// Assert
		StepVerifier.create(result).assertNext(res -> {
			assertEquals(response, res.getResponse());
			assertNotNull(res.getStartTime());
			assertNotNull(res.getEndTime());
			assertNotNull(res.getRequestJson());
			assertNotNull(res.getResponseJson());
		}).verifyComplete();
	}

}
