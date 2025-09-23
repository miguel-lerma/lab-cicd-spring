package com.femsa.oxxo.voucher.client.voucher.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

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
class ClientVoucherifyImplObjMapperTest {
	
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
	void testPublicationVoucher_whenRequestAndResponseSerializationFail_thenLogsErrorAndReturnsNullJsons() throws JsonProcessingException {
	    // Arrange
	    RequestPublicationVoucher request = new RequestPublicationVoucher();
	    RequestPublicationsVoucherify mappedBody = new RequestPublicationsVoucherify();

	    when(voucherifyMapper.mapRequestPublication(request)).thenReturn(mappedBody);

	    // Simular fallo en serialización del request
	    when(objectMapper.writeValueAsString(mappedBody)).thenThrow(new JsonProcessingException("Error serializing request") {});

	    // Mock WebClient
	    when(webClient.post()).thenReturn(requestBodyUriSpec);
	    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
	    when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
	    when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
	    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
	    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

	    ResponsePublications response = new ResponsePublications();
	    when(responseSpec.bodyToMono(ResponsePublications.class)).thenReturn(Mono.just(response));

	    // Simular fallo también en serialización del response
	    when(objectMapper.writeValueAsString(response)).thenThrow(new JsonProcessingException("Error serializing response") {});

	    // Act
	    Mono<ResponsePublicationsVoucherify> resultMono = client.publicationVoucher(request);

	    // Assert
	    StepVerifier.create(resultMono)
	        .assertNext(result -> {
	            assertNotNull(result);
	            assertEquals(response, result.getResponse());
	            assertNotNull(result.getStartTime());
	            assertNotNull(result.getEndTime());
	            assertNull(result.getRequestJson());
	            assertNull(result.getResponseJson());
	        })
	        .verifyComplete();

	    // Verificaciones
	    verify(objectMapper, times(1)).writeValueAsString(mappedBody);
	    verify(objectMapper, times(1)).writeValueAsString(response);
	}
	
	@Test
	void testRedeemVoucher_whenRequestAndResponseSerializationFail_thenLogsErrorAndReturnsNullJsons() throws JsonProcessingException {
	    // Arrange
		RequestRedeemVoucher request = new RequestRedeemVoucher();
		RequestRedeemVoucherify mappedBody = new RequestRedeemVoucherify();

	    when(voucherifyMapper.mapRequestRedeem(request)).thenReturn(mappedBody);

	    // Simular fallo en serialización del request
	    when(objectMapper.writeValueAsString(mappedBody)).thenThrow(new JsonProcessingException("Error serializing request") {});

	    // Mock WebClient
	    when(webClient.post()).thenReturn(requestBodyUriSpec);
	    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
	    when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
	    when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
	    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
	    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

	    ResponseRedeem response = new ResponseRedeem();
	    when(responseSpec.bodyToMono(ResponseRedeem.class)).thenReturn(Mono.just(response));

	    // Simular fallo también en serialización del response
	    when(objectMapper.writeValueAsString(response)).thenThrow(new JsonProcessingException("Error serializing response") {});

	    // Act
	    Mono<ResponseRedeemVoucherify> resultMono = client.redeemVoucher(request);

	    // Assert
	    StepVerifier.create(resultMono)
	        .assertNext(result -> {
	            assertNotNull(result);
	            assertEquals(response, result.getResponse());
	            assertNotNull(result.getStartTime());
	            assertNotNull(result.getEndTime());
	            assertNull(result.getRequestJson());
	            assertNull(result.getResponseJson());
	        })
	        .verifyComplete();

	    // Verificaciones
	    verify(objectMapper, times(1)).writeValueAsString(mappedBody);
	    verify(objectMapper, times(1)).writeValueAsString(response);
	}
	
	@Test
	void testValidateVoucher_whenRequestAndResponseSerializationFail_thenLogsErrorAndReturnsNullJsons() throws JsonProcessingException {
	    // Arrange
		RequestValidateVoucher request = new RequestValidateVoucher();
		RequestValidationsVoucherify mappedBody = new RequestValidationsVoucherify();

	    when(voucherifyMapper.mapRequestValidate(request)).thenReturn(mappedBody);

	    // Simular fallo en serialización del request
	    when(objectMapper.writeValueAsString(mappedBody)).thenThrow(new JsonProcessingException("Error serializing request") {});

	    // Mock WebClient
	    when(webClient.post()).thenReturn(requestBodyUriSpec);
	    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
	    when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
	    when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
	    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
	    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

	    ResponseValidations response = new ResponseValidations();
	    when(responseSpec.bodyToMono(ResponseValidations.class)).thenReturn(Mono.just(response));

	    // Simular fallo también en serialización del response
	    when(objectMapper.writeValueAsString(response)).thenThrow(new JsonProcessingException("Error serializing response") {});

	    // Act
	    Mono<ResponseValidateVoucherify> resultMono = client.validateVoucher(request);

	    // Assert
	    StepVerifier.create(resultMono)
	        .assertNext(result -> {
	            assertNotNull(result);
	            assertEquals(response, result.getResponse());
	            assertNotNull(result.getStartTime());
	            assertNotNull(result.getEndTime());
	            assertNull(result.getRequestJson());
	            assertNull(result.getResponseJson());
	        })
	        .verifyComplete();

	    // Verificaciones
	    verify(objectMapper, times(1)).writeValueAsString(mappedBody);
	    verify(objectMapper, times(1)).writeValueAsString(response);
	}
	
	@Test
	void testPublicationVoucher_whenWebClientThrowsException_thenOnErrorResumeCallsResponseHandler() throws JsonProcessingException {
	    // Arrange
	    RequestPublicationVoucher request = new RequestPublicationVoucher();
	    RequestPublicationsVoucherify mappedBody = new RequestPublicationsVoucherify();

	    when(voucherifyMapper.mapRequestPublication(request)).thenReturn(mappedBody);

	    // Serialización exitosa del request
	    when(objectMapper.writeValueAsString(mappedBody)).thenReturn("{\"sample\":\"request\"}");

	    // WebClient mocks
	    when(webClient.post()).thenReturn(requestBodyUriSpec);
	    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
	    when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
	    when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
	    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

	    // Simular excepción en bodyToMono para activar onErrorResume
	    WebClientResponseException exception = WebClientResponseException.create(
	            500, "Internal Server Error", null, null, null);
	    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

	    // Simular error en flujo reactivo (para que onErrorResume lo capture)
	    when(responseSpec.bodyToMono(ResponsePublications.class)).thenReturn(Mono.error(exception));

	    
	    // Simular valores de tiempo esperados (usaremos captor para verificarlos luego)
	    OffsetDateTime startTime = OffsetDateTime.now();
	    OffsetDateTime endTime = startTime.plusSeconds(1);

	    // Simular respuesta del handler
	    ResponsePublications response = new ResponsePublications();
	    response.setCode(500);
	    response.setKey("Internal Server Error");
	    response.setMessage("Internal Server Error");
	    String responseJson = "{\"code\":500}";

	    ResponsePublicationsVoucherify fallback = new ResponsePublicationsVoucherify(
	            response, startTime, endTime, "{\"sample\":\"request\"}", responseJson
	    );

	    // Mock responseHandler
	    when(responseHandler.responseErrorOrExceptionPublication(
	            eq(exception), any(), any(), eq("{\"sample\":\"request\"}")
	    )).thenReturn(Mono.just(fallback));

	    // Ejecutar el método
	    Mono<ResponsePublicationsVoucherify> resultMono = client.publicationVoucher(request);

	    // Verificar
	    StepVerifier.create(resultMono)
	        .assertNext(result -> {
                assertEquals(500, result.getResponse().getCode());
                assertEquals("Internal Server Error", result.getResponse().getMessage());
                assertEquals("{\"sample\":\"request\"}", result.getRequestJson());
            })
	        .verifyComplete();

	    // Validar que el método del handler fue llamado
	    verify(responseHandler).responseErrorOrExceptionPublication(
	            eq(exception), any(), any(), eq("{\"sample\":\"request\"}")
	    );
	}
	
	@Test
	void testRedeemVoucher_whenWebClientThrowsException_thenOnErrorResumeCallsResponseHandler() throws JsonProcessingException {
	    // Arrange
		RequestRedeemVoucher request = new RequestRedeemVoucher();
		RequestRedeemVoucherify mappedBody = new RequestRedeemVoucherify();

	    when(voucherifyMapper.mapRequestRedeem(request)).thenReturn(mappedBody);

	    // Serialización exitosa del request
	    when(objectMapper.writeValueAsString(mappedBody)).thenReturn("{\"sample\":\"request\"}");

	    // WebClient mocks
	    when(webClient.post()).thenReturn(requestBodyUriSpec);
	    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
	    when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
	    when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
	    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

	    // Simular excepción en bodyToMono para activar onErrorResume
	    WebClientResponseException exception = WebClientResponseException.create(
	            500, "Internal Server Error", null, null, null);
	    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

	    // Simular error
	    when(responseSpec.bodyToMono(ResponseRedeem.class)).thenReturn(Mono.error(exception));

	    
	    // Simular valores de tiempo
	    OffsetDateTime startTime = OffsetDateTime.now();
	    OffsetDateTime endTime = startTime.plusSeconds(1);

	    // Simular respuesta del handler
	    ResponseRedeem response = new ResponseRedeem();
	    response.setCode(500);
	    response.setKey("Internal Server Error");
	    response.setMessage("Internal Server Error");
	    String responseJson = "{\"code\":500}";

	    ResponseRedeemVoucherify fallback = new ResponseRedeemVoucherify(
	            response, startTime, endTime, "{\"sample\":\"request\"}", responseJson
	    );

	    // Mock responseHandler
	    when(responseHandler.responseErrorOrExceptionRedeem(
	            eq(exception), any(), any(), eq("{\"sample\":\"request\"}")
	    )).thenReturn(Mono.just(fallback));

	    // Ejecutar el método
	    Mono<ResponseRedeemVoucherify> resultMono = client.redeemVoucher(request);

	    // Verificar
	    StepVerifier.create(resultMono)
	        .assertNext(result -> {
                assertEquals(500, result.getResponse().getCode());
                assertEquals("Internal Server Error", result.getResponse().getMessage());
                assertEquals("{\"sample\":\"request\"}", result.getRequestJson());
            })
	        .verifyComplete();

	    // Validar que el método del handler fue llamado
	    verify(responseHandler).responseErrorOrExceptionRedeem(
	            eq(exception), any(), any(), eq("{\"sample\":\"request\"}")
	    );
	}
	
	@Test
	void testValidateVoucher_whenWebClientThrowsException_thenOnErrorResumeCallsResponseHandler() throws JsonProcessingException {
	    // Arrange
		RequestValidateVoucher request = new RequestValidateVoucher();
		RequestValidationsVoucherify mappedBody = new RequestValidationsVoucherify();

	    when(voucherifyMapper.mapRequestValidate(request)).thenReturn(mappedBody);

	    // Serialización exitosa del request
	    when(objectMapper.writeValueAsString(mappedBody)).thenReturn("{\"sample\":\"request\"}");

	    // WebClient mocks
	    when(webClient.post()).thenReturn(requestBodyUriSpec);
	    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
	    when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
	    when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
	    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

	    // Simular excepción en bodyToMono para activar onErrorResume
	    WebClientResponseException exception = WebClientResponseException.create(
	            500, "Internal Server Error", null, null, null);
	    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

	    // Simular error
	    when(responseSpec.bodyToMono(ResponseValidations.class)).thenReturn(Mono.error(exception));

	    
	    // Simular valores de tiempo
	    OffsetDateTime startTime = OffsetDateTime.now();
	    OffsetDateTime endTime = startTime.plusSeconds(1);

	    // Simular respuesta del handler
	    ResponseValidations response = new ResponseValidations();
	    response.setCode(500);
	    response.setKey("Internal Server Error");
	    response.setMessage("Internal Server Error");
	    String responseJson = "{\"code\":500}";

	    ResponseValidateVoucherify fallback = new ResponseValidateVoucherify(
	            response, startTime, endTime, "{\"sample\":\"request\"}", responseJson
	    );

	    // Mock responseHandler
	    when(responseHandler.responseErrorOrExceptionValidate(
	            eq(exception), any(), any(), eq("{\"sample\":\"request\"}")
	    )).thenReturn(Mono.just(fallback));

	    // Ejecutar el método
	    Mono<ResponseValidateVoucherify> resultMono = client.validateVoucher(request);

	    // Verificar
	    StepVerifier.create(resultMono)
	        .assertNext(result -> {
                assertEquals(500, result.getResponse().getCode());
                assertEquals("Internal Server Error", result.getResponse().getMessage());
                assertEquals("{\"sample\":\"request\"}", result.getRequestJson());
            })
	        .verifyComplete();

	    // Validar que el método del handler fue llamado
	    verify(responseHandler).responseErrorOrExceptionValidate(
	            eq(exception), any(), any(), eq("{\"sample\":\"request\"}")
	    );
	}
}
