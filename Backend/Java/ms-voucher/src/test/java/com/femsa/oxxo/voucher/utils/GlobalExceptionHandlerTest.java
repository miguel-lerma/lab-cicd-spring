package com.femsa.oxxo.voucher.utils;

import com.femsa.oxxo.voucher.dto.ResponseValidateVoucher;
import com.femsa.oxxo.voucher.dto.Result;
import com.femsa.oxxo.voucher.dto.Error;
import com.femsa.oxxo.voucher.dto.Redemption;
import com.femsa.oxxo.voucher.dto.ResponsePublicationVoucher;
import com.femsa.oxxo.voucher.dto.ResponseRedeemVoucher;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.NoHandlerFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {
	
	GlobalExceptionHandler handler = new GlobalExceptionHandler();

	@Test
	void handleArgumentsvalidExceptions_whenPathContainsValidate_returnsExpectedValidateErrorResponse() {

		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getRequestURI()).thenReturn("/api/voucher/validate");

		MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);

		ResponseEntity<?> responseEntity = handler.handleArgumentsvalidExceptions(exception, request);

		assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
		assertTrue(responseEntity.getBody() instanceof ResponseValidateVoucher);

		ResponseValidateVoucher response = (ResponseValidateVoucher) responseEntity.getBody();

		assertFalse(response.getValid());
		assertEquals(0, response.getTypeCoupon());
		assertEquals("0", response.getCoupon());
		assertEquals(0L, response.getVoucherRmsId());
		assertEquals("0", response.getMemberId());
		assertEquals(0, response.getRedeemablesLimit());
		assertEquals(0, response.getApplicableRedeemablesLimit());
		assertEquals(0, response.getApplicableExclusiveRedeemablesLimit());

		assertNotNull(response.getResult());
		Result result = response.getResult();
		assertNotNull(result.getError());

		Error error = result.getError();
		assertEquals(400, error.getCode());
		assertEquals("Datos recibidos inválidos", error.getMessage());
	}

	@Test
	void handleArgumentsvalidExceptions_whenPathContainsRedeem_returnsExpectedRedeemErrorResponse() {

		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getRequestURI()).thenReturn("/api/voucher/redeem");

		MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);

		ResponseEntity<?> responseEntity = handler.handleArgumentsvalidExceptions(exception, request);

		assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
		assertTrue(responseEntity.getBody() instanceof ResponseRedeemVoucher);

		ResponseRedeemVoucher response = (ResponseRedeemVoucher) responseEntity.getBody();

		assertFalse(response.getValid());
		assertEquals("0", response.getCoupon());
		assertEquals("0", response.getMemberId());

		Redemption redemption = response.getRedemption();

		assertNull(redemption);

		Result result = response.getResult();
		assertNotNull(result);
		Error error = result.getError();
		assertNotNull(error);
		assertEquals(400, error.getCode());
		assertEquals("Datos recibidos inválidos", error.getMessage());
	}

	@Test
	void handleArgumentsvalidExceptions_shouldReturnErrorPublicationResponse_whenPathContainsPublication() {

		MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);

		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getRequestURI()).thenReturn("/publication");

		ResponseEntity<?> responseEntity = handler.handleArgumentsvalidExceptions(exception, request);

		assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

		Object body = responseEntity.getBody();
		assertTrue(body instanceof ResponsePublicationVoucher);

		ResponsePublicationVoucher response = (ResponsePublicationVoucher) body;
		assertFalse(response.getValid());
		assertEquals("0", response.getMemberId());
		assertEquals("0", response.getCampaign());
		assertEquals(0, response.getCount());
		assertNull(response.getVouchers());

		assertNotNull(response.getResult());
		assertNotNull(response.getResult().getError());
		assertEquals(HttpStatus.BAD_REQUEST.value(), response.getResult().getError().getCode());
		assertEquals("Datos recibidos inválidos", response.getResult().getError().getMessage());
	}

	@Test
	void testHandleArgumentsValidExceptions_GenericPath() {

		MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
		HttpServletRequest request = mock(HttpServletRequest.class);

		when(request.getRequestURI()).thenReturn("/generic/path");

		ResponseEntity<?> response = handler.handleArgumentsvalidExceptions(ex, request);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

		assertTrue(response.getBody() instanceof Result);
		Result result = (Result) response.getBody();
		Error error = result.getError();

		assertNotNull(error);
		assertEquals(400, error.getCode());
		assertEquals("Datos recibidos inválidos", error.getMessage());
	}
	
	@Test
	void testHandleNotFoundException_Validate() {

	    NoHandlerFoundException ex = new NoHandlerFoundException("GET", "/validate", HttpHeaders.EMPTY);
	    HttpServletRequest request = mock(HttpServletRequest.class);
	    when(request.getRequestURI()).thenReturn("/validate");

	    ResponseEntity<?> response = handler.handleNotFoundException(ex, request);

	    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	    assertTrue(response.getBody() instanceof ResponseValidateVoucher);

	    ResponseValidateVoucher body = (ResponseValidateVoucher) response.getBody();
	    assertFalse(body.getValid());
	    assertEquals(404, body.getResult().getError().getCode());
	    assertEquals("Ruta no encontrada", body.getResult().getError().getMessage());
	}
	
	@Test
	void testHandleNotFoundException_Redeem() {
		
	    NoHandlerFoundException ex = new NoHandlerFoundException("GET", "/redeem", HttpHeaders.EMPTY);
	    HttpServletRequest request = mock(HttpServletRequest.class);
	    when(request.getRequestURI()).thenReturn("/redeem");

	    ResponseEntity<?> response = handler.handleNotFoundException(ex, request);

	    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	    assertTrue(response.getBody() instanceof ResponseRedeemVoucher);

	    ResponseRedeemVoucher body = (ResponseRedeemVoucher) response.getBody();
	    assertFalse(body.getValid());
	    assertEquals(404, body.getResult().getError().getCode());
	    assertEquals("Ruta no encontrada", body.getResult().getError().getMessage());
	}
	
	@Test
	void testHandleNotFoundException_Publication() {
	    NoHandlerFoundException ex = new NoHandlerFoundException("GET", "/publication", HttpHeaders.EMPTY);
	    HttpServletRequest request = mock(HttpServletRequest.class);
	    when(request.getRequestURI()).thenReturn("/publication");

	    ResponseEntity<?> response = handler.handleNotFoundException(ex, request);

	    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	    assertTrue(response.getBody() instanceof ResponsePublicationVoucher);

	    ResponsePublicationVoucher body = (ResponsePublicationVoucher) response.getBody();
	    assertFalse(body.getValid());
	    assertEquals(404, body.getResult().getError().getCode());
	    assertEquals("Ruta no encontrada", body.getResult().getError().getMessage());
	}
	
	@Test
	void testHandleNotFoundException_Generic() {
	    NoHandlerFoundException ex = new NoHandlerFoundException("GET", "/otraRuta", HttpHeaders.EMPTY);
	    HttpServletRequest request = mock(HttpServletRequest.class);
	    when(request.getRequestURI()).thenReturn("/otraRuta");

	    ResponseEntity<?> response = handler.handleNotFoundException(ex, request);

	    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	    assertTrue(response.getBody() instanceof Result);

	    Result result = (Result) response.getBody();
	    assertEquals(404, result.getError().getCode());
	    assertEquals("Ruta no encontrada", result.getError().getMessage());
	}
	
	@Test
	void testHandleMethodNotAllowedException_Validate() {
	    HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("POST");
	    HttpServletRequest request = mock(HttpServletRequest.class);
	    when(request.getRequestURI()).thenReturn("/validate");

	    ResponseEntity<?> response = handler.handleMethodNotAllowedException(ex, request);

	    assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
	    assertTrue(response.getBody() instanceof ResponseValidateVoucher);

	    ResponseValidateVoucher body = (ResponseValidateVoucher) response.getBody();
	    assertFalse(body.getValid());
	    assertEquals(405, body.getResult().getError().getCode());
	    assertEquals("Método no permitido", body.getResult().getError().getMessage());
	}
	
	@Test
	void testHandleMethodNotAllowedException_Redeem() {
	    HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("POST");
	    HttpServletRequest request = mock(HttpServletRequest.class);
	    when(request.getRequestURI()).thenReturn("/redeem");

	    ResponseEntity<?> response = handler.handleMethodNotAllowedException(ex, request);

	    assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
	    assertTrue(response.getBody() instanceof ResponseRedeemVoucher);

	    ResponseRedeemVoucher body = (ResponseRedeemVoucher) response.getBody();
	    assertFalse(body.getValid());
	    assertEquals(405, body.getResult().getError().getCode());
	    assertEquals("Método no permitido", body.getResult().getError().getMessage());
	}
	
	@Test
	void testHandleMethodNotAllowedException_Publication() {
	    HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("POST");
	    HttpServletRequest request = mock(HttpServletRequest.class);
	    when(request.getRequestURI()).thenReturn("/publication");

	    ResponseEntity<?> response = handler.handleMethodNotAllowedException(ex, request);

	    assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
	    assertTrue(response.getBody() instanceof ResponsePublicationVoucher);

	    ResponsePublicationVoucher body = (ResponsePublicationVoucher) response.getBody();
	    assertFalse(body.getValid());
	    assertEquals(405, body.getResult().getError().getCode());
	    assertEquals("Método no permitido", body.getResult().getError().getMessage());
	}
	
	@Test
	void testHandleMethodNotAllowedException_Generic() {
	    HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("POST");
	    HttpServletRequest request = mock(HttpServletRequest.class);
	    when(request.getRequestURI()).thenReturn("/otraRuta");

	    ResponseEntity<?> response = handler.handleMethodNotAllowedException(ex, request);

	    assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
	    assertTrue(response.getBody() instanceof Result);

	    Result result = (Result) response.getBody();
	    assertEquals(405, result.getError().getCode());
	    assertEquals("Método no permitido", result.getError().getMessage());
	}

}
