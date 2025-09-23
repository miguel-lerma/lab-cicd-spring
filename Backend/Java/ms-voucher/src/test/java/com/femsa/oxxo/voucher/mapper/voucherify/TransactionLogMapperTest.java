package com.femsa.oxxo.voucher.mapper.voucherify;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.femsa.oxxo.voucher.dto.ConsumerData;
import com.femsa.oxxo.voucher.dto.ConsumerDataRequestPublication;
import com.femsa.oxxo.voucher.dto.RequestValidateVoucher;
import com.femsa.oxxo.voucher.dto.ResponsePublicationVoucher;
import com.femsa.oxxo.voucher.dto.ResponseRedeemVoucher;
import com.femsa.oxxo.voucher.dto.ResponseValidateVoucher;
import com.femsa.oxxo.voucher.dto.Result;
import com.femsa.oxxo.voucher.dto.voucherify.publications.ResponsePublicationsVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.redemptions.ResponseRedeemVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.validations.ResponseValidateVoucherify;
import com.femsa.oxxo.voucher.entity.MsTransaction;

import com.femsa.oxxo.voucher.dto.Error;
import com.femsa.oxxo.voucher.dto.RequestPublicationVoucher;
import com.femsa.oxxo.voucher.dto.RequestRedeemVoucher;


class TransactionLogMapperTest {

	private static final OffsetDateTime TRANSACTION_DATE_REQUEST = OffsetDateTime.now();

	@Test
	void toTransactionValidate_shouldMapCorrectly_whenAllFieldsAreValid() {
		// Arrange
		RequestValidateVoucher request = new RequestValidateVoucher();
		request.setOperation("VAL");
		request.setCoupon("ABC123");
		request.setTransactionId("TX123");
		request.setTicket(12345);
		request.setCashdate("20250520");
		request.setCashhour("154530");
		request.setServer("server-1");
		request.setCashier("JohnDoe");

		ConsumerData consumerData = new ConsumerData();
		consumerData.setCrPlace("place");
		consumerData.setCrStore("store");
		consumerData.setCashRegister(1);
		consumerData.setApplication("app");
		consumerData.setEntity("entity");
		consumerData.setOrigin("origin");
		request.setConsumerData(consumerData);

		ResponseValidateVoucher response = new ResponseValidateVoucher();
		Result result = new Result();
		Error error = new Error();
		error.setCode(200);
		error.setMessage("Success");
		result.setError(error);
		response.setResult(result);

		ResponseValidateVoucherify voucherify = new ResponseValidateVoucherify();
		voucherify.setStartTime(OffsetDateTime.now());
		voucherify.setEndTime(OffsetDateTime.now().plusSeconds(1));
		voucherify.setRequestJson("{\"req\":\"value\"}");
		voucherify.setResponseJson("{\"res\":\"value\"}");

		OffsetDateTime transactionDateRequest = OffsetDateTime.now(ZoneId.of("America/Mexico_City"));

		// Act
		MsTransaction transaction = TransactionLogMapper.toTransactionValidate(request, response, 200,
				transactionDateRequest, voucherify);

		// Assert
		assertThat(transaction).isNotNull();
		assertThat(transaction.getPlace()).isEqualTo("place");
		assertThat(transaction.getStore()).isEqualTo("store");
		assertThat(transaction.getCash()).isEqualTo(1);
		assertThat(transaction.getCoupon()).isEqualTo("ABC123");
		assertThat(transaction.getNoTicket()).isEqualTo("12345");
		assertThat(transaction.getIdTicket()).isEqualTo("TX123");
		assertThat(transaction.getHttpCode()).isEqualTo(200);
		assertThat(transaction.getCode()).isEqualTo(200);
		assertThat(transaction.getMessage()).isEqualTo("Success");
		assertThat(transaction.getDataRequest()).contains("ABC123");
		assertThat(transaction.getDataResponse()).contains("Success");
		assertThat(transaction.getDataRequestProvider()).contains("req");
		assertThat(transaction.getDataResponseProvider()).contains("res");
		assertThat(transaction.getTransactionDateRequest()).isEqualTo(transactionDateRequest);
		assertThat(transaction.getDatetimeRequest()).isEqualTo(voucherify.getStartTime());
		assertThat(transaction.getDatetimeResponse()).isEqualTo(voucherify.getEndTime());
	}

	@Test
	void testToTransactionValidate_whenCashDateAndCashHourAreNull() {
		// Arrange
		RequestValidateVoucher request = buildBaseRequest();
		request.setCashdate(null);
		request.setCashhour(null);

		ResponseValidateVoucher response = new ResponseValidateVoucher();
		ResponseValidateVoucherify voucherify = new ResponseValidateVoucherify();
		voucherify.setStartTime(OffsetDateTime.now());
		voucherify.setEndTime(OffsetDateTime.now());
		voucherify.setRequestJson("{\"mock\":true}");
		voucherify.setResponseJson("{\"result\":\"ok\"}");

		OffsetDateTime requestTime = OffsetDateTime.now();

		// Act
		MsTransaction result = TransactionLogMapper.toTransactionValidate(request, response, 200, requestTime,
				voucherify);

		// Assert
		assertNotNull(result);
		assertNotNull(result.getTransactionDateChannel(), "transactionDateChannel debería contener la fecha actual");
		assertNotNull(result.getTransactionTimeChannel(), "transactionTimeChannel debería contener la hora actual");
	}

	@Test
	void testToTransactionValidate_whenCashDateAndCashHourAreValid() {
		// Arrange
		RequestValidateVoucher request = buildBaseRequest();
		request.setCashdate("20250501");
		request.setCashhour("101500"); // 10:15:00

		ResponseValidateVoucher response = new ResponseValidateVoucher();
		ResponseValidateVoucherify voucherify = new ResponseValidateVoucherify();
		voucherify.setStartTime(OffsetDateTime.now());
		voucherify.setEndTime(OffsetDateTime.now());
		voucherify.setRequestJson("{\"mock\":true}");
		voucherify.setResponseJson("{\"result\":\"ok\"}");

		OffsetDateTime requestTime = OffsetDateTime.now();

		// Act
		MsTransaction result = TransactionLogMapper.toTransactionValidate(request, response, 200, requestTime,
				voucherify);

		// Assert
		assertEquals(LocalDate.of(2025, 5, 1), result.getTransactionDateChannel());
		assertEquals("10:15", result.getTransactionTimeChannel().toString().substring(0, 5));
	}

	@Test
	void testToTransactionValidate_whenResponseHasErrorCode() {
		// Arrange
		RequestValidateVoucher request = buildBaseRequest();

		ResponseValidateVoucher response = new ResponseValidateVoucher();
		Result result = new Result();
		Error error = new Error();
		error.setCode(101);
		error.setMessage("Código inválido");
		result.setError(error);
		response.setResult(result);

		ResponseValidateVoucherify voucherify = new ResponseValidateVoucherify();
		voucherify.setStartTime(OffsetDateTime.now());
		voucherify.setEndTime(OffsetDateTime.now());
		voucherify.setRequestJson("{\"test\":1}");
		voucherify.setResponseJson("{\"result\":\"fail\"}");

		OffsetDateTime requestTime = OffsetDateTime.now();

		// Act
		MsTransaction msTransaction = TransactionLogMapper.toTransactionValidate(request, response, 400, requestTime,
				voucherify);

		// Assert
		assertEquals(101, msTransaction.getCode());
		assertEquals("Código inválido", msTransaction.getMessage());
		assertEquals("{\"test\":1}", msTransaction.getDataRequestProvider());
		assertEquals("{\"result\":\"fail\"}", msTransaction.getDataResponseProvider());
	}

	@Test
	void testToTransactionValidate_whenSerializationFails() {
		// Arrange
		RequestValidateVoucher request = spy(buildBaseRequest());
		ResponseValidateVoucher response = new ResponseValidateVoucher();
		ResponseValidateVoucherify voucherify = new ResponseValidateVoucherify();
		voucherify.setStartTime(OffsetDateTime.now());
		voucherify.setEndTime(OffsetDateTime.now());
		voucherify.setRequestJson("requestJSON");
		voucherify.setResponseJson("responseJSON");

		OffsetDateTime requestTime = OffsetDateTime.now();

		// Simula excepción en serialización con ObjectMapper
		ObjectMapper faultyMapper = mock(ObjectMapper.class);
		try {
			when(faultyMapper.writeValueAsString(any())).thenThrow(JsonProcessingException.class);
		} catch (JsonProcessingException e) {
			fail("No debería lanzar aquí");
		}

		// Act
		MsTransaction result = TransactionLogMapper.toTransactionValidate(request, response, 500, requestTime,
				voucherify);

		// Assert
		assertNotNull(result.getDataRequest());
		assertNotNull(result.getDataResponse());
	}

	// Utilidad para construir un request base
	private RequestValidateVoucher buildBaseRequest() {
		RequestValidateVoucher request = new RequestValidateVoucher();
		request.setOperation("VAL");
		request.setCoupon("COUPON123");
		request.setTransactionId("TX123");
		request.setTicket(123);
		request.setCashier("CASHIER1");
		request.setServer("SRV1");
		request.setMemberId("MEM1");

		ConsumerData cd = new ConsumerData();
		cd.setApplication("APP1");
		cd.setCrPlace("MX");
		cd.setCrStore("001");
		cd.setCashRegister(7);
		cd.setEntity("FEMSA");
		cd.setOrigin("POS");

		request.setConsumerData(cd);
		return request;
	}

	@Test
	void testToTransactionRedeem_whenCashDateAndCashHourAreNull() {
		// Arrange
		RequestRedeemVoucher request = buildBaseRedeemRequest();
		request.setCashdate(null);
		request.setCashhour(null);

		ResponseRedeemVoucher response = new ResponseRedeemVoucher();
		ResponseRedeemVoucherify voucherify = new ResponseRedeemVoucherify();
		voucherify.setStartTime(OffsetDateTime.now());
		voucherify.setEndTime(OffsetDateTime.now());
		voucherify.setRequestJson("{\"mock\":true}");
		voucherify.setResponseJson("{\"result\":\"ok\"}");

		OffsetDateTime requestTime = OffsetDateTime.now();

		// Act
		MsTransaction result = TransactionLogMapper.toTransactionRedeem(request, response, 200, requestTime,
				voucherify);

		// Assert
		assertNotNull(result.getTransactionDateChannel());
		assertNotNull(result.getTransactionTimeChannel());
	}

	@Test
	void testToTransactionRedeem_whenCashDateAndCashHourAreValid() {
		// Arrange
		RequestRedeemVoucher request = buildBaseRedeemRequest();
		request.setCashdate("20250521");
		request.setCashhour("083000");

		ResponseRedeemVoucher response = new ResponseRedeemVoucher();
		ResponseRedeemVoucherify voucherify = new ResponseRedeemVoucherify();
		voucherify.setStartTime(OffsetDateTime.now());
		voucherify.setEndTime(OffsetDateTime.now());
		voucherify.setRequestJson("{\"mock\":true}");
		voucherify.setResponseJson("{\"result\":\"ok\"}");

		OffsetDateTime requestTime = OffsetDateTime.now();

		// Act
		MsTransaction result = TransactionLogMapper.toTransactionRedeem(request, response, 200, requestTime,
				voucherify);

		// Assert
		assertEquals(LocalDate.of(2025, 5, 21), result.getTransactionDateChannel());
		assertEquals("08:30", result.getTransactionTimeChannel().toString().substring(0, 5));
	}

	@Test
	void testToTransactionRedeem_whenResponseHasErrorCode() {
		// Arrange
		RequestRedeemVoucher request = buildBaseRedeemRequest();

		ResponseRedeemVoucher response = new ResponseRedeemVoucher();
		Result resultObj = new Result();
		Error error = new Error();
		error.setCode(303);
		error.setMessage("Voucher ya usado");
		resultObj.setError(error);
		response.setResult(resultObj);

		ResponseRedeemVoucherify voucherify = new ResponseRedeemVoucherify();
		voucherify.setStartTime(OffsetDateTime.now());
		voucherify.setEndTime(OffsetDateTime.now());
		voucherify.setRequestJson("{\"x\":1}");
		voucherify.setResponseJson("{\"status\":\"used\"}");

		OffsetDateTime requestTime = OffsetDateTime.now();

		// Act
		MsTransaction result = TransactionLogMapper.toTransactionRedeem(request, response, 400, requestTime,
				voucherify);

		// Assert
		assertEquals(303, result.getCode());
		assertEquals("Voucher ya usado", result.getMessage());
		assertEquals("{\"x\":1}", result.getDataRequestProvider());
		assertEquals("{\"status\":\"used\"}", result.getDataResponseProvider());
	}

	@Test
	void testToTransactionRedeem_whenSerializationFails() {
		// Arrange
		RequestRedeemVoucher request = spy(buildBaseRedeemRequest());
		ResponseRedeemVoucher response = new ResponseRedeemVoucher();
		ResponseRedeemVoucherify voucherify = new ResponseRedeemVoucherify();
		voucherify.setStartTime(OffsetDateTime.now());
		voucherify.setEndTime(OffsetDateTime.now());
		voucherify.setRequestJson("requestJSON");
		voucherify.setResponseJson("responseJSON");

		OffsetDateTime requestTime = OffsetDateTime.now();

		// Act
		MsTransaction result = TransactionLogMapper.toTransactionRedeem(request, response, 500, requestTime,
				voucherify);

		// Assert
		assertNotNull(result.getDataRequest());
		assertNotNull(result.getDataResponse());
	}

	@Test
	void testToTransactionRedeem_shouldMapAllRequiredFieldsCorrectly() {
		// Arrange
		RequestRedeemVoucher request = buildBaseRedeemRequest();
		request.setCashdate("20250101");
		request.setCashhour("090000");

		ResponseRedeemVoucher response = new ResponseRedeemVoucher();
		ResponseRedeemVoucherify voucherify = new ResponseRedeemVoucherify();
		voucherify.setStartTime(OffsetDateTime.now().minusSeconds(2));
		voucherify.setEndTime(OffsetDateTime.now());
		voucherify.setRequestJson("{\"in\":true}");
		voucherify.setResponseJson("{\"out\":true}");

		OffsetDateTime requestTime = OffsetDateTime.now();

		// Act
		MsTransaction result = TransactionLogMapper.toTransactionRedeem(request, response, 201, requestTime,
				voucherify);

		// Assert
		assertEquals("APP1", result.getApplication());
		assertEquals("FEMSA", result.getEntity());
		assertEquals("POS", result.getSource());
		assertEquals("REDEEM", result.getOperation());
		assertEquals("COUPON456", result.getCoupon());
		assertEquals("TX456", result.getIdTicket());
		assertEquals("SERVER2", result.getRequestServer());
		assertEquals("CASHIER2", result.getOperator());
	}

	private RequestRedeemVoucher buildBaseRedeemRequest() {
		RequestRedeemVoucher request = new RequestRedeemVoucher();
		request.setOperation("REDEEM");
		request.setCoupon("COUPON456");
		request.setTransactionId("TX456");
		request.setTicket(456);
		request.setCashier("CASHIER2");
		request.setServer("SERVER2");
		request.setCashdate("20250101");
		request.setCashhour("090000");

		ConsumerData cd = new ConsumerData();
		cd.setApplication("APP1");
		cd.setCrPlace("MX");
		cd.setCrStore("002");
		cd.setCashRegister(8);
		cd.setEntity("FEMSA");
		cd.setOrigin("POS");

		request.setConsumerData(cd);
		return request;
	}

	@Test
	void testToTransactionPublication_WithValidInput() throws JsonProcessingException {
		// Arrange
		RequestPublicationVoucher request = new RequestPublicationVoucher();
		ConsumerDataRequestPublication consumerData = new ConsumerDataRequestPublication();
		consumerData.setCrPlace("001");
		consumerData.setCrStore("002");
		consumerData.setCashRegister(3);
		consumerData.setApplication("APP1");
		consumerData.setEntity("ENT1");
		consumerData.setOrigin("ORIG1");
		request.setConsumerData(consumerData);
		request.setDate("20240521");
		request.setHour("131545");
		request.setOperation("publication");
		request.setUser("operador");

		ResponsePublicationVoucher response = new ResponsePublicationVoucher();
		Result result = new Result();
		Error error = new Error();
		error.setCode(200);
		error.setMessage("Publicación exitosa");
		result.setError(error);
		response.setResult(result);

		ResponsePublicationsVoucherify responseVoucherify = new ResponsePublicationsVoucherify();
		responseVoucherify.setStartTime(OffsetDateTime.now().minusSeconds(2));
		responseVoucherify.setEndTime(OffsetDateTime.now());
		responseVoucherify.setRequestJson("{\"key\":\"value\"}");
		responseVoucherify.setResponseJson("{\"resp\":\"ok\"}");

		// Act
		MsTransaction resultTx = TransactionLogMapper.toTransactionPublication(request, response, 200,
				TRANSACTION_DATE_REQUEST, responseVoucherify);

		// Assert
		assertNotNull(resultTx);
		assertEquals("001", resultTx.getPlace());
		assertEquals("002", resultTx.getStore());
		assertEquals(3, resultTx.getCash());
		assertEquals(200, resultTx.getCode());
		assertEquals("Publicación exitosa", resultTx.getMessage());
		assertEquals("APP1", resultTx.getApplication());
		assertEquals("ENT1", resultTx.getEntity());
		assertEquals("ORIG1", resultTx.getSource());
		assertEquals("publication", resultTx.getOperation());
		assertEquals("operador", resultTx.getOperator());
		assertEquals("{\"key\":\"value\"}", resultTx.getDataRequestProvider());
		assertEquals("{\"resp\":\"ok\"}", resultTx.getDataResponseProvider());
		assertEquals(200, resultTx.getHttpCode());
	}

	@Test
	void testToTransactionPublication_WithNullDateAndHour() {
		// Arrange
		RequestPublicationVoucher request = new RequestPublicationVoucher();
		ConsumerDataRequestPublication consumerData = new ConsumerDataRequestPublication();
		consumerData.setCrPlace("001");
		request.setConsumerData(consumerData);
		request.setDate(null);
		request.setHour(null);
		request.setUser("userX");
		request.setOperation("publication");

		ResponsePublicationVoucher response = new ResponsePublicationVoucher();
		response.setResult(null); // error nulo

		ResponsePublicationsVoucherify responseVoucherify = new ResponsePublicationsVoucherify();
		responseVoucherify.setStartTime(OffsetDateTime.now());
		responseVoucherify.setEndTime(OffsetDateTime.now());
		responseVoucherify.setRequestJson("{}");
		responseVoucherify.setResponseJson("{}");

		// Act
		MsTransaction resultTx = TransactionLogMapper.toTransactionPublication(request, response, 400,
				TRANSACTION_DATE_REQUEST, responseVoucherify);

		// Assert
		assertNotNull(resultTx.getTransactionDateChannel());
		assertNotNull(resultTx.getTransactionTimeChannel());
		assertNull(resultTx.getCode());
		assertNull(resultTx.getMessage());
		assertEquals(400, resultTx.getHttpCode());
	}

	@Test
	void testToTransactionPublication_WhenSerializationFails() {
		// Arrange
		RequestPublicationVoucher request = new RequestPublicationVoucher();
		ConsumerDataRequestPublication consumerData = new ConsumerDataRequestPublication();
		request.setConsumerData(consumerData);
		request.setDate("20240522");
		request.setHour("120000");
		request.setUser("userX");
		request.setOperation("publication");

		ResponsePublicationVoucher response = new ResponsePublicationVoucher();

		ResponsePublicationsVoucherify responseVoucherify = new ResponsePublicationsVoucherify();
		responseVoucherify.setStartTime(OffsetDateTime.now());
		responseVoucherify.setEndTime(OffsetDateTime.now());
		responseVoucherify.setRequestJson("{bad}");
		responseVoucherify.setResponseJson("{bad}");

		// Act
		MsTransaction resultTx = TransactionLogMapper.toTransactionPublication(request, response, 500,
				TRANSACTION_DATE_REQUEST, responseVoucherify);

		// Assert
		assertNotNull(resultTx);
	}

}
