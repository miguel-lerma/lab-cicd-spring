package com.femsa.oxxo.voucher.mapper.voucherify;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.femsa.oxxo.voucher.dto.ConsumerData;
import com.femsa.oxxo.voucher.dto.ConsumerDataRequestPublication;
import com.femsa.oxxo.voucher.dto.RequestPublicationVoucher;
import com.femsa.oxxo.voucher.dto.RequestRedeemVoucher;
import com.femsa.oxxo.voucher.dto.RequestValidateVoucher;
import com.femsa.oxxo.voucher.dto.voucherify.validations.ResponseValidateVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.validations.ResponseValidations;
import com.femsa.oxxo.voucher.dto.voucherify.validations.Result;
import com.femsa.oxxo.voucher.entity.MsLog;
import com.femsa.oxxo.voucher.entity.MsRespCode;
import com.femsa.oxxo.voucher.repository.MsRespCodeRepository;
import com.femsa.oxxo.voucher.dto.voucherify.publications.ResponsePublications;
import com.femsa.oxxo.voucher.dto.voucherify.publications.ResponsePublicationsVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.redemptions.ResponseRedeem;
import com.femsa.oxxo.voucher.dto.voucherify.redemptions.ResponseRedeemVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.validations.Error;
import com.femsa.oxxo.voucher.dto.voucherify.validations.RedeemableResponse;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class LogMapperTest {

	@Mock
	private MsRespCodeRepository repoMsCode;

	@InjectMocks
	private TransactionLogMapper transactionLogMapper;
	
	private RequestPublicationVoucher request;
	private ResponsePublicationsVoucherify response;
	
	@BeforeEach
    void init() {
        ConsumerDataRequestPublication consumer = new ConsumerDataRequestPublication();
        consumer.setCrPlace("P1234");
        consumer.setCrStore("T1234");
        consumer.setCashRegister(5);

        request = new RequestPublicationVoucher();
        request.setOperation("PUB");
        request.setConsumerData(consumer);

        ResponsePublications res = new ResponsePublications();
        res.setKey("key-123");

        response = new ResponsePublicationsVoucherify();
        response.setResponse(res);
    }

	@Test
	void toLogError_ShouldMapCorrectly_WhenKeyIsPresentInResponse() {
		// Arrange
		String testKey = "test-key";
		String operation = "VAL";

		// Simula la clave encontrada en la respuesta
		ResponseValidations responseValidations = new ResponseValidations();
		responseValidations.setKey(testKey);

		ResponseValidateVoucherify responseVoucherify = new ResponseValidateVoucherify();
		responseVoucherify.setResponse(responseValidations);

		RequestValidateVoucher request = new RequestValidateVoucher();
		request.setOperation(operation);

		ConsumerData consumerData = new ConsumerData();
		consumerData.setCrPlace("P1234");
		consumerData.setCrStore("T1234");
		consumerData.setCashRegister(1);
		request.setConsumerData(consumerData);

		MsRespCode msRespCode = new MsRespCode();
		msRespCode.setSeverity("HIGH");
		msRespCode.setCodeHttpMs(400L);
		msRespCode.setCodeMs(1001L);
		msRespCode.setMessageKey(testKey);
		msRespCode.setMessagePos("Error del POS");
		msRespCode.setAction("Reintentar");

		Mockito.when(repoMsCode.findByMessageKeyIgnoreCase(Mockito.anyString(), Mockito.eq("validate")))
				.thenReturn(Mono.just(msRespCode));

		// Act & Assert
		StepVerifier.create(transactionLogMapper.toLogError(request, responseVoucherify)).assertNext(msLog -> {
			assertEquals(testKey, msLog.getDescription());
			assertEquals("HIGH", msLog.getSeverity());
			assertEquals("400", msLog.getErrorType());
			assertEquals(1001, msLog.getErrorCode());
			assertEquals("Error del POS", msLog.getMessage());
			assertEquals("Reintentar", msLog.getAction());
			assertEquals("P1234", msLog.getPlace());
			assertEquals("T1234", msLog.getStore());
			assertEquals(1, msLog.getCash());
			assertEquals("VAL", msLog.getOperation());
		}).verifyComplete();
	}

	@Test
	void testToLogError_KeyNotFound_UsesGenericMsCode() {
		// Arrange
		String originalKey = "unknownKey";
		String genericKey = "generic";

		RequestValidateVoucher request = new RequestValidateVoucher();
		ConsumerData consumerData = new ConsumerData();
		consumerData.setCrPlace("P0010");
		consumerData.setCrStore("T0020");
		consumerData.setCashRegister(3);
		request.setConsumerData(consumerData);
		request.setOperation("VAL");

		Error error = new Error();
		error.setKey(originalKey);

		Result result = new Result();
		result.setError(error);

		RedeemableResponse redeemable = new RedeemableResponse();
		redeemable.setResult(result);

		ResponseValidations responseValidations = new ResponseValidations();
		responseValidations.setRedeemables(Collections.singletonList(redeemable));

		ResponseValidateVoucherify response = new ResponseValidateVoucherify();
		response.setResponse(responseValidations);

		MsRespCode msCodeGeneric = new MsRespCode();
		msCodeGeneric.setSeverity("ERROR");
		msCodeGeneric.setCodeHttpMs(500L);
		msCodeGeneric.setCodeMs(999L);
		msCodeGeneric.setMessageKey("GENERIC_KEY");
		msCodeGeneric.setMessagePos("Mensaje genérico");
		msCodeGeneric.setAction("Revisar logs");

		// Mock comportamiento: primer lookup falla, segundo retorna valor
		when(repoMsCode.findByMessageKeyIgnoreCase(originalKey, "validate")).thenReturn(Mono.empty());
		when(repoMsCode.findByMessageKeyIgnoreCase(genericKey, "validate")).thenReturn(Mono.just(msCodeGeneric));

		// Act
		Mono<MsLog> resultMono = transactionLogMapper.toLogError(request, response);

		// Assert
		StepVerifier.create(resultMono).expectNextMatches(msLog -> {
			assertEquals("ERROR", msLog.getSeverity());
			assertEquals("500", msLog.getErrorType());
			assertEquals(999, msLog.getErrorCode());
			assertEquals("unknownKey", msLog.getDescription()); // se mantiene el key original
			assertEquals("Mensaje genérico", msLog.getMessage());
			assertEquals("Revisar logs", msLog.getAction());
			assertEquals("P0010", msLog.getPlace());
			assertEquals("T0020", msLog.getStore());
			assertEquals(3, msLog.getCash());
			assertEquals("VAL", msLog.getOperation());
			return true;
		}).verifyComplete();
	}
	
	@Test
	void testToLogErrorRedeem_KeyFound() {
	    // Arrange
	    String messageKey = "REDEEM_KEY";

	    RequestRedeemVoucher request = new RequestRedeemVoucher();
	    ConsumerData consumerData = new ConsumerData();
	    consumerData.setCrPlace("P100");
	    consumerData.setCrStore("S200");
	    consumerData.setCashRegister(10);
	    request.setConsumerData(consumerData);
	    request.setOperation("RED");

	    ResponseRedeem response = new ResponseRedeem(); 
	    response.setKey(messageKey);
	    ResponseRedeemVoucherify responseVoucherify = new ResponseRedeemVoucherify();
	    responseVoucherify.setResponse(response);

	    MsRespCode msCode = new MsRespCode();
	    msCode.setSeverity("WARN");
	    msCode.setCodeHttpMs(400L);
	    msCode.setCodeMs(123L);
	    msCode.setMessageKey(messageKey);
	    msCode.setMessagePos("Mensaje Redención");
	    msCode.setAction("Verificar usuario");
	    
	    Mockito.when(repoMsCode.findByMessageKeyIgnoreCase(Mockito.anyString(), Mockito.eq("redemption")))
	    .thenReturn(Mono.just(msCode));

	    // Act
	    Mono<MsLog> resultMono = transactionLogMapper.toLogErrorRedeem(request, responseVoucherify);

	    // Assert
	    StepVerifier.create(resultMono)
	        .expectNextMatches(msLog ->
	            msLog.getSeverity().equals("WARN")
	        )
	        .verifyComplete();
	}
	
	@Test
	void testToLogErrorRedeem_KeyNotFound_UsesGenericMsCode() {
	    // Arrange
	    String originalKey = "UNKNOWN_KEY";

	    RequestRedeemVoucher request = new RequestRedeemVoucher();
	    ConsumerData consumerData = new ConsumerData();
	    consumerData.setCrPlace("P100");
	    consumerData.setCrStore("S200");
	    consumerData.setCashRegister(10);
	    request.setConsumerData(consumerData);
	    request.setOperation("RED");

	    ResponseRedeem response = new ResponseRedeem();
	    response.setKey(originalKey);
	    ResponseRedeemVoucherify responseVoucherify = new ResponseRedeemVoucherify();
	    responseVoucherify.setResponse(response);

	    MsRespCode genericCode = new MsRespCode();
	    genericCode.setSeverity("ERROR");
	    genericCode.setCodeHttpMs(500L);
	    genericCode.setCodeMs(999L);
	    genericCode.setMessageKey("GENERIC_KEY");
	    genericCode.setMessagePos("Mensaje genérico redención");
	    genericCode.setAction("Contactar soporte");

	    when(repoMsCode.findByMessageKeyIgnoreCase("UNKNOWN_KEY", "redemption"))
	    .thenReturn(Mono.empty());

	    when(repoMsCode.findByMessageKeyIgnoreCase("generic", "redemption"))
	    .thenReturn(Mono.just(genericCode));

	    // Act
	    Mono<MsLog> resultMono = transactionLogMapper.toLogErrorRedeem(request, responseVoucherify);

	    // Assert
	    StepVerifier.create(resultMono)
	        .expectNextMatches(msLog ->
	            msLog.getSeverity().equals("ERROR")
	        )
	        .verifyComplete();
	}
	
	@Test
    void testToLogErrorPublications_foundKey() {
        MsRespCode msCode = new MsRespCode();
        msCode.setMessageKey("key");
        msCode.setCodeMs(999L);
        msCode.setMessagePos("error pos");
        msCode.setAction("action");
        msCode.setCodeHttpMs(400L);
        msCode.setSeverity("HIGH");

        //when(repoMsCode.findByMessageKeyIgnoreCase("key", "publication")).thenReturn(Mono.just(msCode));
        
        Mockito.when(repoMsCode.findByMessageKeyIgnoreCase(Mockito.anyString(), Mockito.eq("publication")))
	    .thenReturn(Mono.just(msCode));

        StepVerifier.create(transactionLogMapper.toLogErrorPublications(request, response))
            .assertNext(msLog -> {
                assertEquals("HIGH", msLog.getSeverity());
                assertEquals("400", msLog.getErrorType());
                assertEquals(999, msLog.getErrorCode());
            })
            .verifyComplete();
    }

    @Test
    void testToLogErrorPublications_fallbackToGeneric() {
        MsRespCode msCodeGeneric = new MsRespCode();
        msCodeGeneric.setMessageKey("generic");
        msCodeGeneric.setCodeMs(111L);
        msCodeGeneric.setMessagePos("fallback");
        msCodeGeneric.setAction("fallbackAction");
        msCodeGeneric.setCodeHttpMs(500L);
        msCodeGeneric.setSeverity("LOW");

        when(repoMsCode.findByMessageKeyIgnoreCase("key-123", "publication")).thenReturn(Mono.empty());
        when(repoMsCode.findByMessageKeyIgnoreCase("generic", "publication")).thenReturn(Mono.just(msCodeGeneric));

        StepVerifier.create(transactionLogMapper.toLogErrorPublications(request, response))
            .assertNext(msLog -> {
                assertEquals("LOW", msLog.getSeverity());
                assertEquals("500", msLog.getErrorType());
                assertEquals(111, msLog.getErrorCode());
                assertEquals("generic", msLog.getDescription());
                assertEquals("fallback", msLog.getMessage());
                assertEquals("fallbackAction", msLog.getAction());
                assertEquals("PUB", msLog.getOperation());
                assertEquals(1L, msLog.getIdTransaction());
            })
            .verifyComplete();
    }
    
    @Test
    void toLogError_whenNotFound_returnsDefaultLogViaDefer() {
        // Arrange
        RequestValidateVoucher request = new RequestValidateVoucher();
        ConsumerData consumerData = new ConsumerData();
        consumerData.setCrPlace("PLZ02");
        consumerData.setCrStore("STR02");
        consumerData.setCashRegister(222);
        request.setConsumerData(consumerData);
        request.setOperation("VAL");

        ResponseValidateVoucherify responseVoucherify = new ResponseValidateVoucherify();
        ResponseValidations response = new ResponseValidations();
        response.setMessage("unknown_key");
        response.setCode(500);
        response.setKey("KEY_123");
        responseVoucherify.setResponse(response);

        // Simular que no encuentra el key original ni el "GENERIC"
        when(repoMsCode.findByMessageKeyIgnoreCase(eq("KEY_123"), eq("validate")))
            .thenReturn(Mono.empty());
        when(repoMsCode.findByMessageKeyIgnoreCase(eq("generic"), eq("validate")))
            .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(transactionLogMapper.toLogError(request, responseVoucherify))
            .expectNextMatches(log -> {
                assertThat(log.getPlace()).isEqualTo("PLZ02");
                assertThat(log.getStore()).isEqualTo("STR02");
                assertThat(log.getCash()).isEqualTo(222);
                assertThat(log.getOperation()).isEqualTo("VAL");
                assertThat(log.getMessage()).isEqualTo("unknown_key");
                assertThat(log.getErrorCode()).isEqualTo(500);
                assertThat(log.getErrorType()).isEqualTo("500");
                return true;
            })
            .verifyComplete();
    }
    
    @Test
    void toLogError_onErrorResume_returnsDefaultLog() {
        // Arrange
        RequestValidateVoucher request = new RequestValidateVoucher();
        ConsumerData consumerData = new ConsumerData();
        consumerData.setCrPlace("PLZ01");
        consumerData.setCrStore("STR01");
        consumerData.setCashRegister(123);
        request.setConsumerData(consumerData);
        request.setOperation("VAL");

        ResponseValidateVoucherify responseVoucherify = new ResponseValidateVoucherify();
        ResponseValidations response = new ResponseValidations();
        response.setMessage("Internal error");
        response.setCode(500);
        responseVoucherify.setResponse(response);
        
        when(repoMsCode.findByMessageKeyIgnoreCase(any(), eq("validate")))
        .thenReturn(Mono.error(new RuntimeException("DB error")));

        // Act & Assert
        StepVerifier.create(transactionLogMapper.toLogError(request, responseVoucherify))
            .expectNextMatches(log -> {
                assertThat(log.getPlace()).isEqualTo("PLZ01");
                assertThat(log.getStore()).isEqualTo("STR01");
                assertThat(log.getCash()).isEqualTo(123);
                assertThat(log.getOperation()).isEqualTo("VAL");
                assertThat(log.getMessage()).isEqualTo("Error interno del sistema");
                assertThat(log.getDescription()).isEqualTo("Excepción al consultar MsRespCode");
                assertThat(log.getErrorType()).isEqualTo("500");
                return true;
            })
            .verifyComplete();
    }
    
    @Test
    void toLogErrorRedeem_onErrorResume_returnsDefaultLog() {
        // Arrange
    	RequestRedeemVoucher request = new RequestRedeemVoucher();
        ConsumerData consumerData = new ConsumerData();
        consumerData.setCrPlace("PLZ01");
        consumerData.setCrStore("STR01");
        consumerData.setCashRegister(123);
        request.setConsumerData(consumerData);
        request.setOperation("VAL");

        ResponseRedeemVoucherify responseVoucherify = new ResponseRedeemVoucherify();
        ResponseRedeem response = new ResponseRedeem();
        response.setMessage("Internal error");
        response.setCode(500);
        responseVoucherify.setResponse(response);
        
        when(repoMsCode.findByMessageKeyIgnoreCase(any(), eq("redemption")))
        .thenReturn(Mono.error(new RuntimeException("DB error")));

        // Act & Assert
        StepVerifier.create(transactionLogMapper.toLogErrorRedeem(request, responseVoucherify))
            .expectNextMatches(log -> {
                assertThat(log.getPlace()).isEqualTo("PLZ01");
                assertThat(log.getStore()).isEqualTo("STR01");
                assertThat(log.getCash()).isEqualTo(123);
                assertThat(log.getOperation()).isEqualTo("VAL");
                assertThat(log.getMessage()).isEqualTo("Error interno del sistema");
                assertThat(log.getDescription()).isEqualTo("Excepción al consultar MsRespCode");
                assertThat(log.getErrorType()).isEqualTo("500");
                return true;
            })
            .verifyComplete();
    }
    
    @Test
    void toLogErrorRedeem_whenNotFound_returnsDefaultLogViaDefer() {
        // Arrange
    	RequestRedeemVoucher request = new RequestRedeemVoucher();
        ConsumerData consumerData = new ConsumerData();
        consumerData.setCrPlace("PLZ02");
        consumerData.setCrStore("STR02");
        consumerData.setCashRegister(222);
        request.setConsumerData(consumerData);
        request.setOperation("VAL");

        ResponseRedeemVoucherify responseVoucherify = new ResponseRedeemVoucherify();
        ResponseRedeem response = new ResponseRedeem();
        response.setMessage("unknown_key");
        response.setCode(500);
        response.setKey("KEY_123");
        responseVoucherify.setResponse(response);

        // Simular que no encuentra el key original ni el "GENERIC"
        when(repoMsCode.findByMessageKeyIgnoreCase(eq("KEY_123"), eq("redemption")))
            .thenReturn(Mono.empty());
        when(repoMsCode.findByMessageKeyIgnoreCase(eq("generic"), eq("redemption")))
            .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(transactionLogMapper.toLogErrorRedeem(request, responseVoucherify))
            .expectNextMatches(log -> {
                assertThat(log.getPlace()).isEqualTo("PLZ02");
                assertThat(log.getStore()).isEqualTo("STR02");
                assertThat(log.getCash()).isEqualTo(222);
                assertThat(log.getOperation()).isEqualTo("VAL");
                assertThat(log.getMessage()).isEqualTo("KEY_123");
                assertThat(log.getErrorCode()).isEqualTo(500);
                assertThat(log.getErrorType()).isEqualTo("500");
                return true;
            })
            .verifyComplete();
    }
    
    @Test
    void toLogErrorPublication_onErrorResume_returnsDefaultLog() {
        // Arrange
    	RequestPublicationVoucher request = new RequestPublicationVoucher();
    	ConsumerDataRequestPublication consumerData = new ConsumerDataRequestPublication();
        consumerData.setCrPlace("PLZ01");
        consumerData.setCrStore("STR01");
        consumerData.setCashRegister(123);
        request.setConsumerData(consumerData);
        request.setOperation("VAL");

        ResponsePublicationsVoucherify responseVoucherify = new ResponsePublicationsVoucherify();
        ResponsePublications response = new ResponsePublications();
        response.setMessage("Internal error");
        response.setCode(500);
        responseVoucherify.setResponse(response);
        
        when(repoMsCode.findByMessageKeyIgnoreCase(any(), eq("publication")))
        .thenReturn(Mono.error(new RuntimeException("DB error")));

        // Act & Assert
        StepVerifier.create(transactionLogMapper.toLogErrorPublications(request, responseVoucherify))
            .expectNextMatches(log -> {
                assertThat(log.getPlace()).isEqualTo("PLZ01");
                assertThat(log.getStore()).isEqualTo("STR01");
                assertThat(log.getCash()).isEqualTo(123);
                assertThat(log.getOperation()).isEqualTo("VAL");
                assertThat(log.getMessage()).isEqualTo("Error interno del sistema");
                assertThat(log.getDescription()).isEqualTo("Excepción al consultar MsRespCode");
                assertThat(log.getErrorType()).isEqualTo("500");
                return true;
            })
            .verifyComplete();
    }
    
    @Test
    void toLogErrorPublication_whenNotFound_returnsDefaultLogViaDefer() {
        // Arrange
    	RequestPublicationVoucher request = new RequestPublicationVoucher();
    	ConsumerDataRequestPublication consumerData = new ConsumerDataRequestPublication();
        consumerData.setCrPlace("PLZ02");
        consumerData.setCrStore("STR02");
        consumerData.setCashRegister(222);
        request.setConsumerData(consumerData);
        request.setOperation("VAL");

        ResponsePublicationsVoucherify responseVoucherify = new ResponsePublicationsVoucherify();
        ResponsePublications response = new ResponsePublications();
        response.setMessage("unknown_key");
        response.setCode(500);
        response.setKey("KEY_123");
        responseVoucherify.setResponse(response);

        // Simular que no encuentra el key original ni el "GENERIC"
        when(repoMsCode.findByMessageKeyIgnoreCase(eq("KEY_123"), eq("publication")))
            .thenReturn(Mono.empty());
        when(repoMsCode.findByMessageKeyIgnoreCase(eq("generic"), eq("publication")))
            .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(transactionLogMapper.toLogErrorPublications(request, responseVoucherify))
            .expectNextMatches(log -> {
                assertThat(log.getPlace()).isEqualTo("PLZ02");
                assertThat(log.getStore()).isEqualTo("STR02");
                assertThat(log.getCash()).isEqualTo(222);
                assertThat(log.getOperation()).isEqualTo("VAL");
                assertThat(log.getMessage()).isEqualTo("KEY_123");
                assertThat(log.getErrorCode()).isEqualTo(500);
                assertThat(log.getErrorType()).isEqualTo("500");
                return true;
            })
            .verifyComplete();
    }
}
