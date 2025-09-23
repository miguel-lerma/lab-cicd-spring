package com.femsa.oxxo.voucher.handler.voucherify;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.femsa.oxxo.voucher.dto.voucherify.publications.ResponsePublications;
import com.femsa.oxxo.voucher.dto.voucherify.publications.ResponsePublicationsVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.redemptions.ResponseRedeem;
import com.femsa.oxxo.voucher.dto.voucherify.redemptions.ResponseRedeemVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.validations.ResponseValidateVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.validations.ResponseValidations;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class VoucherifyResponseHandlerTest {

	private VoucherifyResponseHandler handler;
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper();
		handler = new VoucherifyResponseHandler(objectMapper);
	}

	@Test
	void testResponseErrorValidate_success() {

		// Crear un objeto de error simulado
		ResponseValidations simulatedError = new ResponseValidations();
		simulatedError.setCode(404);
		simulatedError.setKey("voucher.not_found");
		simulatedError.setMessage("Voucher no encontrado");

		// Mock del ClientResponse
		ClientResponse mockResponse = mock(ClientResponse.class);
		when(mockResponse.bodyToMono(ResponseValidations.class)).thenReturn(Mono.just(simulatedError));

		String errorMessage = "Error al validar voucher";

		// Act
		Mono<ResponseValidations> result = handler.responseErrorValidate(mockResponse, errorMessage);

		// Assert
		StepVerifier.create(result).assertNext(response -> {
			assertFalse(response.isValid());
			assertEquals(404, response.getCode());
			assertEquals("voucher.not_found", response.getKey());
			assertEquals("Voucher no encontrado", response.getMessage());
		}).verifyComplete();
	}

	@Test
	void testResponseErrorOrExceptionValidate_withVoucherifyValidateException() {

		OffsetDateTime start = OffsetDateTime.now().minusSeconds(1);
		OffsetDateTime end = OffsetDateTime.now();
		String requestJson = "{\"some\":\"request\"}";

		ResponseValidations mockedResponse = new ResponseValidations();
		mockedResponse.setValid(false);
		mockedResponse.setCode(400);
		mockedResponse.setKey("INVALID");
		mockedResponse.setMessage("Invalid request");

		VoucherifyValidateException exception = new VoucherifyValidateException(mockedResponse);

		// Act
		Mono<ResponseValidateVoucherify> result = handler.responseErrorOrExceptionValidate(exception, start, end,
				requestJson);

		// Assert
		StepVerifier.create(result).assertNext(response -> {
			assertEquals(mockedResponse, response.getResponse());
			assertEquals(start, response.getStartTime());
			assertEquals(end, response.getEndTime());
			assertEquals(requestJson, response.getRequestJson());
			assertNotNull(response.getResponseJson());
			assertTrue(response.getResponseJson().contains("Invalid request"));
		}).verifyComplete();
	}

	@Test
	void testResponseErrorOrExceptionValidate_withUnexpectedException() {

		OffsetDateTime start = OffsetDateTime.now().minusSeconds(1);
		OffsetDateTime end = OffsetDateTime.now();
		String requestJson = "{\"request\":true}";

		RuntimeException exception = new RuntimeException("Unexpected failure");

		// Act
		Mono<ResponseValidateVoucherify> result = handler.responseErrorOrExceptionValidate(exception, start, end,
				requestJson);

		// Assert
		StepVerifier.create(result).assertNext(response -> {
			ResponseValidations validations = response.getResponse();

			assertNotNull(validations);
			assertFalse(validations.isValid());
			assertEquals(500, validations.getCode());
			assertEquals("Internal Server Error", validations.getKey());
			assertEquals("Internal Server error", validations.getMessage());

			assertEquals(start, response.getStartTime());
			assertEquals(end, response.getEndTime());
			assertEquals(requestJson, response.getRequestJson());

			assertNotNull(response.getResponseJson());
			assertTrue(response.getResponseJson().contains("Internal Server Error"));
		}).verifyComplete();
	}

	@Test
	void testResponseErrorRedeem_shouldMapErrorCorrectly() {

		// Crear un objeto de error simulado
		ResponseRedeem simulatedError = new ResponseRedeem();
		simulatedError.setCode(406);
		simulatedError.setKey("voucher.not_redeem");
		simulatedError.setMessage("Voucher no redimido");

		// Mock del ClientResponse
		ClientResponse mockResponse = mock(ClientResponse.class);
		when(mockResponse.bodyToMono(ResponseRedeem.class)).thenReturn(Mono.just(simulatedError));

		String errorMessage = "Error al redimir voucher";

		// Act
		Mono<ResponseRedeem> result = handler.responseErrorRedeem(mockResponse, errorMessage);

		// Assert
		StepVerifier.create(result).assertNext(response -> {
			assertEquals(406, response.getCode());
			assertEquals("voucher.not_redeem", response.getKey());
			assertEquals("Voucher no redimido", response.getMessage());
		}).verifyComplete();
	}

	@Test
	void testResponseErrorOrExceptionRedeem_VoucherifyRedemptionException() throws JsonProcessingException {

		// Arrange
		ObjectMapper objectMapper = mock(ObjectMapper.class);
		VoucherifyResponseHandler handler = new VoucherifyResponseHandler(objectMapper);

		ResponseRedeem mockedResponse = new ResponseRedeem();
		mockedResponse.setCode(400);
		mockedResponse.setKey("Invalid Code");
		mockedResponse.setMessage("The voucher code is invalid");

		String expectedJson = "{\"code\":400,\"key\":\"Invalid Code\",\"message\":\"The voucher code is invalid\"}";

		VoucherifyRedemptionException ex = mock(VoucherifyRedemptionException.class);
		when(ex.getResponse()).thenReturn(mockedResponse);
		when(objectMapper.writeValueAsString(mockedResponse)).thenReturn(expectedJson);

		OffsetDateTime startTime = OffsetDateTime.now();
		OffsetDateTime endTime = OffsetDateTime.now();
		String requestJson = "{\"voucher\":\"ABC123\"}";

		// Act
		Mono<ResponseRedeemVoucherify> resultMono = handler.responseErrorOrExceptionRedeem(ex, startTime, endTime,
				requestJson);
		ResponseRedeemVoucherify result = resultMono.block();

		// Assert
		assertNotNull(result);
		assertEquals(mockedResponse, result.getResponse());
		assertEquals(startTime, result.getStartTime());
		assertEquals(endTime, result.getEndTime());
		assertEquals(requestJson, result.getRequestJson());
		assertEquals(expectedJson, result.getResponseJson());
	}

	@Test
	void testResponseErrorOrExceptionRedeem_GenericException() throws JsonProcessingException {

		// Arrange
		ObjectMapper objectMapper = mock(ObjectMapper.class);
		VoucherifyResponseHandler handler = new VoucherifyResponseHandler(objectMapper);

		OffsetDateTime startTime = OffsetDateTime.now();
		OffsetDateTime endTime = OffsetDateTime.now();
		String requestJson = "{\"voucher\":\"XYZ999\"}";

		ResponseRedeem expectedFallback = new ResponseRedeem();
		expectedFallback.setCode(500);
		expectedFallback.setKey("Internal Server Error");
		expectedFallback.setMessage("Internal Server Error");

		String expectedJson = "{\"code\":500,\"key\":\"Internal Server Error\",\"message\":\"Internal Server Error\"}";

		RuntimeException ex = new RuntimeException("Unexpected failure");

		when(objectMapper.writeValueAsString(any(ResponseRedeem.class))).thenReturn(expectedJson);

		// Act
		Mono<ResponseRedeemVoucherify> resultMono = handler.responseErrorOrExceptionRedeem(ex, startTime, endTime,
				requestJson);
		ResponseRedeemVoucherify result = resultMono.block();

		// Assert
		assertNotNull(result);
		assertEquals(500, result.getResponse().getCode());
		assertEquals("Internal Server Error", result.getResponse().getKey());
		assertEquals("Internal Server Error", result.getResponse().getMessage());
		assertEquals(startTime, result.getStartTime());
		assertEquals(endTime, result.getEndTime());
		assertEquals(requestJson, result.getRequestJson());
		assertEquals(expectedJson, result.getResponseJson());
	}

	@Test
	void testResponseErrorOrExceptionRedeem_SerializationFailure() throws JsonProcessingException {

		// Arrange
		ObjectMapper objectMapper = mock(ObjectMapper.class);
		VoucherifyResponseHandler handler = new VoucherifyResponseHandler(objectMapper);

		OffsetDateTime startTime = OffsetDateTime.now();
		OffsetDateTime endTime = OffsetDateTime.now();
		String requestJson = "{\"voucher\":\"ERROR500\"}";

		// Simulamos excepción de serialización
		doThrow(new JsonProcessingException("Serialization error") {
		}).when(objectMapper).writeValueAsString(any(ResponseRedeem.class));

		RuntimeException ex = new RuntimeException("General error");

		// Act
		Mono<ResponseRedeemVoucherify> resultMono = handler.responseErrorOrExceptionRedeem(ex, startTime, endTime,
				requestJson);
		ResponseRedeemVoucherify result = resultMono.block();

		// Assert
		assertNotNull(result);
		assertEquals(500, result.getResponse().getCode());
		assertEquals("Internal Server Error", result.getResponse().getKey());
		assertEquals("Internal Server Error", result.getResponse().getMessage());
		assertEquals(startTime, result.getStartTime());
		assertEquals(endTime, result.getEndTime());
		assertEquals(requestJson, result.getRequestJson());
		assertNull(result.getResponseJson()); // No se pudo serializar, debe ser null
	}

	@Test
	void testResponseErrorPublication_ReturnsErrorResponse() {

		String errorMessage = "Error en publicación de cupón";

		// Crear un mock de ClientResponse
		ClientResponse clientResponse = mock(ClientResponse.class);

		// Simular el body con un ResponsePublications de error
		ResponsePublications errorBody = new ResponsePublications();
		errorBody.setCode(400);
		errorBody.setKey("Bad Request");
		errorBody.setMessage("El cupón no es válido para esta campaña");

		when(clientResponse.bodyToMono(ResponsePublications.class)).thenReturn(Mono.just(errorBody));

		// Act
		Mono<ResponsePublications> resultMono = handler.responseErrorPublication(clientResponse, errorMessage);

		// Assert
		StepVerifier.create(resultMono).assertNext(response -> {
			assertEquals(400, response.getCode());
			assertEquals("Bad Request", response.getKey());
			assertEquals("El cupón no es válido para esta campaña", response.getMessage());
		}).verifyComplete();

	}

	@Test
	void testResponseErrorOrExceptionPublication_WithVoucherifyPublicationException() throws Exception {
		// Arrange
		OffsetDateTime startTime = OffsetDateTime.now();
		OffsetDateTime endTime = startTime.plusSeconds(2);
		String requestJson = "{\"code\":\"TEST\"}";

		ResponsePublications errorResponse = new ResponsePublications();
		errorResponse.setCode(400);
		errorResponse.setKey("InvalidRequest");
		errorResponse.setMessage("Cupón no aplicable");

		VoucherifyPublicationException exception = new VoucherifyPublicationException(errorResponse);

		// Act
		Mono<ResponsePublicationsVoucherify> result = handler.responseErrorOrExceptionPublication(exception, startTime,
				endTime, requestJson);

		// Assert
		StepVerifier.create(result).assertNext(response -> {
			assertEquals(errorResponse.getCode(), response.getResponse().getCode());
			assertEquals(errorResponse.getKey(), response.getResponse().getKey());
			assertEquals(errorResponse.getMessage(), response.getResponse().getMessage());
			assertEquals(startTime, response.getStartTime());
			assertEquals(endTime, response.getEndTime());
			assertEquals(requestJson, response.getRequestJson());

			try {
				String expectedJson = objectMapper.writeValueAsString(errorResponse);
				assertEquals(expectedJson, response.getResponseJson());
			} catch (Exception e) {
				fail("Error serializando JSON esperado");
			}
		}).verifyComplete();
	}

	@Test
	void testResponseErrorOrExceptionPublication_WithGenericException() {
		// Arrange
		OffsetDateTime startTime = OffsetDateTime.now();
		OffsetDateTime endTime = startTime.plusSeconds(1);
		String requestJson = "{\"code\":\"GENERIC\"}";

		Exception genericException = new RuntimeException("Excepción desconocida");

		// Act
		Mono<ResponsePublicationsVoucherify> result = handler.responseErrorOrExceptionPublication(genericException,
				startTime, endTime, requestJson);

		// Assert
		StepVerifier.create(result).assertNext(response -> {
			ResponsePublications fallback = response.getResponse();
			assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), fallback.getCode());
			assertEquals("Internal Server Error", fallback.getKey());
			assertEquals("Internal Server Error", fallback.getMessage());
			assertEquals(requestJson, response.getRequestJson());
			assertEquals(startTime, response.getStartTime());
			assertEquals(endTime, response.getEndTime());

			assertNotNull(response.getResponseJson());
			assertTrue(response.getResponseJson().contains("Internal Server Error"));
		}).verifyComplete();
	}

}
