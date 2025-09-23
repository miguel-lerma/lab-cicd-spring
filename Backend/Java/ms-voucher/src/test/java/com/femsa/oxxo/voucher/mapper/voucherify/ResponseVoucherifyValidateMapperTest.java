package com.femsa.oxxo.voucher.mapper.voucherify;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.femsa.oxxo.voucher.dto.RequestValidateVoucher;
import com.femsa.oxxo.voucher.dto.ResponseValidateVoucher;
import com.femsa.oxxo.voucher.dto.voucherify.validations.MetadataResponse;
import com.femsa.oxxo.voucher.dto.voucherify.validations.RedeemableResponse;
import com.femsa.oxxo.voucher.dto.voucherify.validations.ResponseValidateVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.validations.ResponseValidations;
import com.femsa.oxxo.voucher.dto.voucherify.validations.Result;
import com.femsa.oxxo.voucher.dto.voucherify.validations.StackingRules;
import com.femsa.oxxo.voucher.entity.MsRespCode;
import com.femsa.oxxo.voucher.repository.MsRespCodeRepository;
import com.femsa.oxxo.voucher.dto.voucherify.validations.Error;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class ResponseVoucherifyValidateMapperTest {
	
	@Mock
    private MsRespCodeRepository repoMsCode;

    private ResponseVoucherifyMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ResponseVoucherifyMapper(repoMsCode);
    } 

    @Test
    void mapValidation_withRedeemablesNoError_shouldReturnValidResponse() {
        // Arrange
        RequestValidateVoucher request = new RequestValidateVoucher();
        request.setCoupon("TESTCOUPON");

        MetadataResponse metadata = new MetadataResponse();
        metadata.setMdVoucherTypeCoupon(2);
        metadata.setMdVoucherRmsTransactionId(123L);
        metadata.setMdVoucherMemberIdAssigned("MEM123");

        RedeemableResponse redeemable = new RedeemableResponse();
        redeemable.setMetadata(metadata);
        
        ResponseValidations responseData = new ResponseValidations();
        responseData.setValid(true);
        responseData.setRedeemables(List.of(redeemable));

        ResponseValidateVoucherify response = new ResponseValidateVoucherify();
        response.setResponse(responseData);

        //when(repoMsCode.findByMessageKeyIgnoreCase(anyString(), eq("validate"))).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(mapper.mapValidation(response, request))
            .assertNext(tuple -> {
                ResponseValidateVoucher result = tuple.getT1();
                assertTrue(result.getValid());
                assertEquals("TESTCOUPON", result.getCoupon());
                assertEquals(2, result.getTypeCoupon());
                assertEquals(123L, result.getVoucherRmsId());
                assertEquals("MEM123", result.getMemberId());
                assertEquals(HttpStatus.OK.value(), tuple.getT2());
            })
            .verifyComplete();
    }
    
    @Test
    void mapValidation_withRedeemablesNoError_shouldReturnValidResponseAndStackingRules() {
        // Arrange
        RequestValidateVoucher request = new RequestValidateVoucher();
        request.setCoupon("TESTCOUPON");

        MetadataResponse metadata = new MetadataResponse();
        metadata.setMdVoucherTypeCoupon(2);
        metadata.setMdVoucherRmsTransactionId(123L);
        metadata.setMdVoucherMemberIdAssigned("MEM123");

        RedeemableResponse redeemable = new RedeemableResponse();
        redeemable.setMetadata(metadata);
        
        StackingRules stackingRules = new StackingRules();
        stackingRules.setRedeemablesLimit(1);
        stackingRules.setApplicableExclusiveRedeemablesLimit(2);
        stackingRules.setApplicableRedeemablesLimit(3);

        ResponseValidations responseData = new ResponseValidations();
        responseData.setValid(true);
        responseData.setRedeemables(List.of(redeemable));
        responseData.setStackingRules(stackingRules);

        ResponseValidateVoucherify response = new ResponseValidateVoucherify();
        response.setResponse(responseData);

        //when(repoMsCode.findByMessageKeyIgnoreCase(anyString(), eq("validate"))).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(mapper.mapValidation(response, request))
            .assertNext(tuple -> {
                ResponseValidateVoucher result = tuple.getT1();
                assertTrue(result.getValid());
                assertEquals("TESTCOUPON", result.getCoupon());
                assertEquals(2, result.getTypeCoupon());
                assertEquals(123L, result.getVoucherRmsId());
                assertEquals("MEM123", result.getMemberId());
                assertEquals(HttpStatus.OK.value(), tuple.getT2());
            })
            .verifyComplete();
    }
    
    @Test
    void mapValidation_withRedeemableError_shouldMapErrorFromRepo() {
        // Arrange
        RequestValidateVoucher request = new RequestValidateVoucher();
        request.setCoupon("TESTCOUPON");

        // Simula el error dentro de redeemable
        Error errorResponse = new Error();
        errorResponse.setKey("voucherify_error_key");

        Result result = new Result();
        result.setError(errorResponse);

        RedeemableResponse redeemable = new RedeemableResponse();
        redeemable.setResult(result);
        redeemable.setMetadata(new MetadataResponse());

        ResponseValidations responseData = new ResponseValidations();
        responseData.setValid(false);
        responseData.setRedeemables(List.of(redeemable));

        ResponseValidateVoucherify response = new ResponseValidateVoucherify();
        response.setResponse(responseData);

        MsRespCode msRespCode = new MsRespCode();
        msRespCode.setCodeMs(400L);
        msRespCode.setCodeHttpMs(400L);
        msRespCode.setMessagePos("Código inválido");

        when(repoMsCode.findByMessageKeyIgnoreCase(anyString(), eq("validate")))
            .thenReturn(Mono.just(msRespCode));

        // Act & Assert
        StepVerifier.create(mapper.mapValidation(response, request))
            .assertNext(tuple -> {
                ResponseValidateVoucher resultDto = tuple.getT1();
                assertFalse(resultDto.getValid());
                assertNotNull(resultDto.getResult());
                assertNotNull(resultDto.getResult().getError());
                assertEquals(400, resultDto.getResult().getError().getCode());
                assertEquals("Código inválido", resultDto.getResult().getError().getMessage());
                assertEquals(400, tuple.getT2());
            })
            .verifyComplete();
    }
    
    @Test
    void mapValidation_withRedeemableError_andGenericFallback_shouldUseGenericMessage() {
        // Arrange
        RequestValidateVoucher request = new RequestValidateVoucher();
        request.setCoupon("TESTCOUPON");

        // Simula el error dentro de redeemable
        Error errorResponse = new Error();
        errorResponse.setKey("some_unknown_key");

        Result result = new Result();
        result.setError(errorResponse);

        RedeemableResponse redeemable = new RedeemableResponse();
        redeemable.setResult(result);
        redeemable.setMetadata(new MetadataResponse());

        ResponseValidations responseData = new ResponseValidations();
        responseData.setValid(false);
        responseData.setRedeemables(List.of(redeemable));

        ResponseValidateVoucherify response = new ResponseValidateVoucherify();
        response.setResponse(responseData);

        // Simula que el primer lookup no devuelve nada
        when(repoMsCode.findByMessageKeyIgnoreCase(eq("some_unknown_key"), eq("validate")))
            .thenReturn(Mono.empty());

        // Simula que el fallback al mensaje "generic" sí devuelve algo
        MsRespCode genericResp = new MsRespCode();
        genericResp.setCodeMs(999L);
        genericResp.setCodeHttpMs(409L);
        genericResp.setMessagePos("Mensaje genérico de error");

        when(repoMsCode.findByMessageKeyIgnoreCase(eq("generic"), eq("validate")))
            .thenReturn(Mono.just(genericResp));

        // Act & Assert
        StepVerifier.create(mapper.mapValidation(response, request))
            .assertNext(tuple -> {
                ResponseValidateVoucher dto = tuple.getT1();
                assertFalse(dto.getValid());
                assertNotNull(dto.getResult());
                assertEquals(999, dto.getResult().getError().getCode());
                assertEquals("Mensaje genérico de error", dto.getResult().getError().getMessage());
                assertEquals(409, tuple.getT2());
            })
            .verifyComplete();
    }
    
    @Test
    void mapValidation_withRedeemableError_andNoRepoMatch_shouldUseOriginalMessage() {
        // Arrange
        RequestValidateVoucher request = new RequestValidateVoucher();
        request.setCoupon("TESTCOUPON");

        // Simula error en el redeemable
        Error errorResponse = new Error();
        errorResponse.setKey("unknown_key");
        errorResponse.setCode(412);
        errorResponse.setMessage("Mensaje original de Voucherify");

        Result result = new Result();
        result.setError(errorResponse);

        MetadataResponse metadata = new MetadataResponse();
        
        RedeemableResponse redeemable = new RedeemableResponse();
        redeemable.setResult(result);
        redeemable.setMetadata(metadata);

        ResponseValidations responseData = new ResponseValidations();
        responseData.setValid(false);
        responseData.setRedeemables(List.of(redeemable));

        ResponseValidateVoucherify response = new ResponseValidateVoucherify();
        response.setResponse(responseData);

        // Simula que no se encuentra ni el mensaje específico ni el genérico
        when(repoMsCode.findByMessageKeyIgnoreCase(eq("unknown_key"), eq("validate")))
            .thenReturn(Mono.empty());
        when(repoMsCode.findByMessageKeyIgnoreCase(eq("generic"), eq("validate")))
            .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(mapper.mapValidation(response, request))
            .assertNext(tuple -> {
                ResponseValidateVoucher dto = tuple.getT1();
                assertFalse(dto.getValid());
                assertNotNull(dto.getResult());
                assertNotNull(dto.getResult().getError());
                assertEquals(412, dto.getResult().getError().getCode() );
                assertEquals("Mensaje original de Voucherify", dto.getResult().getError().getMessage());
                System.out.println("Test Code:"+tuple.getT2());
                assertEquals(412, tuple.getT2());
            })
            .verifyComplete();
    }
    
    @Test
    void mapValidation_withRedeemableError_andRepoException_shouldUseOriginalMessage() {
        // Arrange
        RequestValidateVoucher request = new RequestValidateVoucher();
        request.setCoupon("ERRORCOUPON");

        Error errorResponse = new Error();
        errorResponse.setKey("some_error_key");
        errorResponse.setCode(500);
        errorResponse.setMessage("Error inesperado en Voucherify");

        Result result = new Result();
        result.setError(errorResponse);

        MetadataResponse metadata = new MetadataResponse();
        RedeemableResponse redeemable = new RedeemableResponse();
        redeemable.setResult(result);
        redeemable.setMetadata(metadata);

        ResponseValidations responseData = new ResponseValidations();
        responseData.setValid(false);
        responseData.setCode(500);
        responseData.setMessage("Error inesperado en Voucherify");
        responseData.setRedeemables(List.of(redeemable));

        ResponseValidateVoucherify response = new ResponseValidateVoucherify();
        response.setResponse(responseData);

        // Simular excepción lanzada por el repositorio
        when(repoMsCode.findByMessageKeyIgnoreCase(anyString(), eq("validate")))
            .thenReturn(Mono.error(new RuntimeException("DB unavailable")));

        // Act & Assert
        StepVerifier.create(mapper.mapValidation(response, request))
            .assertNext(tuple -> {
                ResponseValidateVoucher dto = tuple.getT1();
                assertFalse(dto.getValid());
                assertNotNull(dto.getResult());
                assertNotNull(dto.getResult().getError());
                assertEquals(500, dto.getResult().getError().getCode());
                assertEquals("Error inesperado en Voucherify", dto.getResult().getError().getMessage());
                assertEquals(500, tuple.getT2());
            })
            .verifyComplete();
    }
    
    @Test
    void mapValidation_noRedeemables_repoReturnsResult_shouldMapErrorFromDb() {
        // Arrange
        RequestValidateVoucher request = new RequestValidateVoucher();
        request.setCoupon("COUPON123");

        ResponseValidations responseData = new ResponseValidations();
        responseData.setValid(false);
        responseData.setCode(400);
        responseData.setMessage("voucher.invalid");
        responseData.setKey("voucher.invalid");

        // redeemables null or empty
        responseData.setRedeemables(null);

        ResponseValidateVoucherify response = new ResponseValidateVoucherify();
        response.setResponse(responseData);

        MsRespCode msRespCode = new MsRespCode();
        msRespCode.setCodeMs(400L);
        msRespCode.setMessagePos("Voucher inválido");
        msRespCode.setCodeHttpMs(400L);

        when(repoMsCode.findByMessageKeyIgnoreCase(anyString(), eq("validate")))
            .thenReturn(Mono.just(msRespCode));

        // Act & Assert
        StepVerifier.create(mapper.mapValidation(response, request))
            .assertNext(tuple -> {
                ResponseValidateVoucher dto = tuple.getT1();
                assertFalse(dto.getValid());
                assertEquals("COUPON123", dto.getCoupon());
                assertNotNull(dto.getResult());
                assertEquals(400, dto.getResult().getError().getCode());
                assertEquals("Voucher inválido", dto.getResult().getError().getMessage());
                assertEquals(400, tuple.getT2());
            })
            .verifyComplete();
    }
    
    @Test
    void mapValidation_noRedeemables_repoReturnsEmptyThenGeneric_shouldMapGenericMessage() {
        // Arrange
        RequestValidateVoucher request = new RequestValidateVoucher();
        request.setCoupon("COUPON123");

        ResponseValidations responseData = new ResponseValidations();
        responseData.setValid(false);
        responseData.setCode(400);
        responseData.setMessage("voucher.invalid");
        responseData.setKey("voucher.invalid");
        responseData.setRedeemables(Collections.emptyList()); // vacio

        ResponseValidateVoucherify response = new ResponseValidateVoucherify();
        response.setResponse(responseData);

        MsRespCode genericRespCode = new MsRespCode();
        genericRespCode.setCodeMs(999L);
        genericRespCode.setMessagePos("Mensaje genérico de error");
        genericRespCode.setCodeHttpMs(500L);

        when(repoMsCode.findByMessageKeyIgnoreCase(eq("voucher.invalid"), eq("validate")))
            .thenReturn(Mono.empty());
        when(repoMsCode.findByMessageKeyIgnoreCase(eq("generic"), eq("validate")))
            .thenReturn(Mono.just(genericRespCode));

        // Act & Assert
        StepVerifier.create(mapper.mapValidation(response, request))
            .assertNext(tuple -> {
                ResponseValidateVoucher dto = tuple.getT1();
                assertFalse(dto.getValid());
                assertEquals("COUPON123", dto.getCoupon());
                assertNotNull(dto.getResult());
                assertEquals(999, dto.getResult().getError().getCode());
                assertEquals("Mensaje genérico de error", dto.getResult().getError().getMessage());
                assertEquals(500, tuple.getT2());
            })
            .verifyComplete();
    }
    
    @Test
    void mapValidation_noRedeemables_repoReturnsEmptyThenEmpty_shouldKeepOriginalResponse() {
        // Arrange
        RequestValidateVoucher request = new RequestValidateVoucher();
        request.setCoupon("COUPON123");

        ResponseValidations responseData = new ResponseValidations();
        responseData.setValid(false);
        responseData.setCode(400);
        responseData.setMessage("voucher.invalid");
        responseData.setKey("voucher.invalid");
        responseData.setRedeemables(Collections.emptyList());

        ResponseValidateVoucherify response = new ResponseValidateVoucherify();
        response.setResponse(responseData);

        when(repoMsCode.findByMessageKeyIgnoreCase(eq("voucher.invalid"), eq("validate")))
            .thenReturn(Mono.empty());
        when(repoMsCode.findByMessageKeyIgnoreCase(eq("generic"), eq("validate")))
            .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(mapper.mapValidation(response, request))
            .assertNext(tuple -> {
                ResponseValidateVoucher dto = tuple.getT1();
                assertFalse(dto.getValid());
                assertEquals("COUPON123", dto.getCoupon());
                assertNotNull(dto.getResult());
                // Debe mantener el código y mensaje originales de Voucherify
                assertEquals(400, dto.getResult().getError().getCode());
                assertEquals("voucher.invalid", dto.getResult().getError().getMessage());
                assertEquals(400, tuple.getT2());
            })
            .verifyComplete();
    }
    
    @Test
    void mapValidation_noRedeemables_onErrorResume_shouldReturnOriginalResponse() {
        // Arrange
        RequestValidateVoucher request = new RequestValidateVoucher();
        request.setCoupon("COUPON123");

        ResponseValidations responseData = new ResponseValidations();
        responseData.setValid(false);
        responseData.setCode(400);
        responseData.setMessage("voucher.invalid");
        responseData.setKey("voucher.invalid");
        responseData.setRedeemables(Collections.emptyList());

        ResponseValidateVoucherify response = new ResponseValidateVoucherify();
        response.setResponse(responseData);

        when(repoMsCode.findByMessageKeyIgnoreCase(anyString(), eq("validate")))
            .thenReturn(Mono.error(new RuntimeException("DB error")));

        // Act & Assert
        StepVerifier.create(mapper.mapValidation(response, request))
            .assertNext(tuple -> {
                ResponseValidateVoucher dto = tuple.getT1();
                assertFalse(dto.getValid());
                assertEquals("COUPON123", dto.getCoupon());
                assertNotNull(dto.getResult());
                // Debe mantener el código y mensaje originales porque hubo error en BD
                assertEquals(400, dto.getResult().getError().getCode());
                assertEquals("voucher.invalid", dto.getResult().getError().getMessage());
                assertEquals(400, tuple.getT2());
            })
            .verifyComplete();
    }

}
