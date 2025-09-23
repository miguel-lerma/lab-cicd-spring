package com.femsa.oxxo.voucher.mapper.voucherify;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.femsa.oxxo.voucher.dto.RequestPublicationVoucher;
import com.femsa.oxxo.voucher.dto.RequestRedeemVoucher;
import com.femsa.oxxo.voucher.dto.ResponsePublicationVoucher;
import com.femsa.oxxo.voucher.dto.ResponseRedeemVoucher;
import com.femsa.oxxo.voucher.dto.voucherify.redemptions.RedemptionDetails;
import com.femsa.oxxo.voucher.dto.voucherify.redemptions.ResponseRedeem;
import com.femsa.oxxo.voucher.dto.voucherify.redemptions.ResponseRedeemVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.redemptions.Voucher;
import com.femsa.oxxo.voucher.entity.MsRespCode;
import com.femsa.oxxo.voucher.repository.MsRespCodeRepository;
import com.femsa.oxxo.voucher.dto.voucherify.publications.ResponsePublications;
import com.femsa.oxxo.voucher.dto.voucherify.publications.ResponsePublicationsVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.redemptions.*;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;

@ExtendWith(MockitoExtension.class)
class ResponseVoucherifyRedeemMapperTest {

	@Mock
	private MsRespCodeRepository repoMsCode;

	@InjectMocks
	private ResponseVoucherifyMapper mapper;

	@BeforeEach
	void setUp() {
		mapper = new ResponseVoucherifyMapper(repoMsCode);
	}

	@Test
	void testMapRedeem_whenRedemptionsIsNull_andDbReturnsSpecificMessage() {
		// Arrange
		RequestRedeemVoucher request = new RequestRedeemVoucher();
		request.setCoupon("TEST_COUPON");
		request.setMemberId("MEMBER_123");

		ResponseRedeemVoucherify response = new ResponseRedeemVoucherify();
		ResponseRedeem responseInner = new ResponseRedeem();
		responseInner.setRedemptions(null);
		responseInner.setKey("some.error.key");
		response.setResponse(responseInner);

		MsRespCode entity = new MsRespCode();
		entity.setCodeMs(1001L);
		entity.setMessagePos("Mensaje de error específico");
		entity.setCodeHttpMs(400L);

		when(repoMsCode.findByMessageKeyIgnoreCase(anyString(), eq("redemption")))
		.thenReturn(Mono.just(entity));

		// Assert
		StepVerifier.create(mapper.mapRedeem(response, request)).assertNext(tuple -> {
			ResponseRedeemVoucher dto = tuple.getT1();
			Integer httpCode = tuple.getT2();

			assertFalse(dto.getValid());
			assertEquals("TEST_COUPON", dto.getCoupon());
			assertEquals("MEMBER_123", dto.getMemberId());
			assertNotNull(dto.getResult());
			assertEquals(1001, dto.getResult().getError().getCode());
			assertEquals("Mensaje de error específico", dto.getResult().getError().getMessage());
			assertEquals(400, httpCode);
		}).verifyComplete();
	}
	
	@Test
	void testMapRedeem_whenRedemptionsIsNull_andDbReturnsGenericMessage() {
	    // Arrange
	    RequestRedeemVoucher request = new RequestRedeemVoucher();
	    request.setCoupon("GENERIC_COUPON");
	    request.setMemberId("GENERIC_MEMBER");

	    ResponseRedeemVoucherify response = new ResponseRedeemVoucherify();
	    ResponseRedeem responseInner = new ResponseRedeem();
	    responseInner.setRedemptions(null);
	    responseInner.setKey("unknown.key");
	    response.setResponse(responseInner);

	    MsRespCode genericEntity = new MsRespCode();
	    genericEntity.setCodeMs(9999L);
	    genericEntity.setMessagePos("Mensaje genérico de error");
	    genericEntity.setCodeHttpMs(422L);

	    when(repoMsCode.findByMessageKeyIgnoreCase("unknown.key", "redemption"))
	        .thenReturn(Mono.empty());
	    when(repoMsCode.findByMessageKeyIgnoreCase("generic", "redemption"))
	        .thenReturn(Mono.just(genericEntity));

	    // Act
	    Mono<Tuple2<ResponseRedeemVoucher, Integer>> result = mapper.mapRedeem(response, request);

	    // Assert
	    StepVerifier.create(result)
	        .assertNext(tuple -> {
	            ResponseRedeemVoucher dto = tuple.getT1();
	            Integer httpCode = tuple.getT2();

	            assertFalse(dto.getValid());
	            assertEquals("GENERIC_COUPON", dto.getCoupon());
	            assertEquals("GENERIC_MEMBER", dto.getMemberId());
	            assertNotNull(dto.getResult());
	            assertEquals(9999, dto.getResult().getError().getCode());
	            assertEquals("Mensaje genérico de error", dto.getResult().getError().getMessage());
	            assertEquals(422, httpCode);
	        })
	        .verifyComplete();
	}
	
	@Test
	void testMapRedeem_whenRedemptionsIsNull_andDbReturnsEmpty_thenReturnOriginalMessage() {
	    // Arrange
	    RequestRedeemVoucher request = new RequestRedeemVoucher();
	    request.setCoupon("ORIGINAL_COUPON");
	    request.setMemberId("ORIGINAL_MEMBER");

	    ResponseRedeemVoucherify response = new ResponseRedeemVoucherify();
	    ResponseRedeem responseInner = new ResponseRedeem();
	    responseInner.setRedemptions(null);
	    responseInner.setKey("not.found.key");
	    responseInner.setCode(1234);
	    responseInner.setMessage("Mensaje original de Voucherify");
	    response.setResponse(responseInner);

	    when(repoMsCode.findByMessageKeyIgnoreCase("not.found.key", "redemption"))
	        .thenReturn(Mono.empty());
	    when(repoMsCode.findByMessageKeyIgnoreCase("generic", "redemption"))
	        .thenReturn(Mono.empty());

	    // Act
	    Mono<Tuple2<ResponseRedeemVoucher, Integer>> result = mapper.mapRedeem(response, request);

	    // Assert
	    StepVerifier.create(result)
	        .assertNext(tuple -> {
	            ResponseRedeemVoucher dto = tuple.getT1();
	            Integer httpCode = tuple.getT2();

	            assertFalse(dto.getValid());
	            assertEquals("ORIGINAL_COUPON", dto.getCoupon());
	            assertEquals("ORIGINAL_MEMBER", dto.getMemberId());
	            assertNotNull(dto.getResult());
	            assertEquals(1234, dto.getResult().getError().getCode());
	            assertEquals("Mensaje original de Voucherify", dto.getResult().getError().getMessage());
	            assertEquals(1234, httpCode);
	        })
	        .verifyComplete();
	}
	
	@Test
	void testMapRedeem_whenRedemptionsIsNull_andDbThrowsException_thenReturnOriginalMessage() {
	    // Arrange
	    RequestRedeemVoucher request = new RequestRedeemVoucher();
	    request.setCoupon("ERROR_COUPON");
	    request.setMemberId("ERROR_MEMBER");

	    ResponseRedeemVoucherify response = new ResponseRedeemVoucherify();
	    ResponseRedeem responseInner = new ResponseRedeem();
	    responseInner.setRedemptions(null);
	    responseInner.setKey("exception.key");
	    responseInner.setCode(9999);
	    responseInner.setMessage("Error al consultar la base de datos");
	    response.setResponse(responseInner);

	    when(repoMsCode.findByMessageKeyIgnoreCase(anyString(), eq("redemption")))
	        .thenReturn(Mono.error(new RuntimeException("Simulated DB failure")));

	    // Act
	    Mono<Tuple2<ResponseRedeemVoucher, Integer>> result = mapper.mapRedeem(response, request);

	    // Assert
	    StepVerifier.create(result)
	        .assertNext(tuple -> {
	            ResponseRedeemVoucher dto = tuple.getT1();
	            Integer httpCode = tuple.getT2();

	            assertFalse(dto.getValid());
	            assertEquals("ERROR_COUPON", dto.getCoupon());
	            assertEquals("ERROR_MEMBER", dto.getMemberId());
	            assertNotNull(dto.getResult());
	            assertEquals(9999, dto.getResult().getError().getCode());
	            assertEquals("Error al consultar la base de datos", dto.getResult().getError().getMessage());
	            assertEquals(9999, httpCode);
	        })
	        .verifyComplete();
	}
	
	@Test
	void testMapRedeem_whenRedemptionSuccess_thenReturnValidTrue() {
	    // Arrange
	    RequestRedeemVoucher request = new RequestRedeemVoucher();
	    request.setCoupon("SUCCESS_COUPON");
	    request.setMemberId("MEMBER123");

	    Voucher voucher = new Voucher();
	    voucher.setCode("SUCCESS_COUPON");
	    
	    RedemptionDetails redemptionDetails = new RedemptionDetails();
	    redemptionDetails.setQuantity(10);
	    redemptionDetails.setRedeemedQuantity(5);
	    voucher.setRedemption(redemptionDetails);

	    Metadata metadata = new Metadata();
	    metadata.setRedemptionMemberIdRedeemed("MEMBER123");

	    Redemption redemption = new Redemption();
	    redemption.setResult("SUCCESS");
	    redemption.setVoucher(voucher);
	    redemption.setMetadata(metadata);

	    ResponseRedeem responseInner = new ResponseRedeem();
	    responseInner.setRedemptions(List.of(redemption));

	    ResponseRedeemVoucherify response = new ResponseRedeemVoucherify();
	    response.setResponse(responseInner);

	    // Act
	    Mono<Tuple2<ResponseRedeemVoucher, Integer>> result = mapper.mapRedeem(response, request);

	    // Assert
	    StepVerifier.create(result)
	        .assertNext(tuple -> {
	            ResponseRedeemVoucher dto = tuple.getT1();
	            Integer httpCode = tuple.getT2();

	            assertTrue(dto.getValid());
	            assertEquals("SUCCESS_COUPON", dto.getCoupon());
	            assertEquals("MEMBER123", dto.getMemberId());

	            assertNotNull(dto.getRedemption());
	            assertEquals(10, dto.getRedemption().getQuantity());
	            assertEquals(5, dto.getRedemption().getRedeemedQuantity());

	            assertEquals(HttpStatus.OK.value(), httpCode);
	        })
	        .verifyComplete();
	}
	
	@Test
	void testMapRedeem_whenRedemptionFailed_thenReturnValidFalse() {
	    // Arrange
	    RequestRedeemVoucher request = new RequestRedeemVoucher();
	    request.setCoupon("FAIL_COUPON");
	    request.setMemberId("MEMBER999");

	    Voucher voucher = new Voucher();
	    voucher.setCode("FAIL_COUPON");
	    
	    RedemptionDetails redemptionDetails = new RedemptionDetails();
	    redemptionDetails.setQuantity(5);
	    redemptionDetails.setRedeemedQuantity(5);
	    voucher.setRedemption(redemptionDetails);

	    Metadata metadata = new Metadata();
	    metadata.setRedemptionMemberIdRedeemed(null); // Simulando falta de memberId

	    Redemption redemption = new Redemption();
	    redemption.setResult("FAILED"); // Aquí está el cambio
	    redemption.setVoucher(voucher);
	    redemption.setMetadata(metadata);

	    ResponseRedeem responseInner = new ResponseRedeem();
	    responseInner.setRedemptions(List.of(redemption));

	    ResponseRedeemVoucherify response = new ResponseRedeemVoucherify();
	    response.setResponse(responseInner);

	    // Act
	    Mono<Tuple2<ResponseRedeemVoucher, Integer>> result = mapper.mapRedeem(response, request);

	    // Assert
	    StepVerifier.create(result)
	        .assertNext(tuple -> {
	            ResponseRedeemVoucher dto = tuple.getT1();
	            Integer httpCode = tuple.getT2();

	            assertFalse(dto.getValid());
	            assertEquals("FAIL_COUPON", dto.getCoupon());
	            assertEquals("MEMBER999", dto.getMemberId()); // fallback al valor de request

	            assertNotNull(dto.getRedemption());
	            assertEquals(5, dto.getRedemption().getQuantity());
	            assertEquals(5, dto.getRedemption().getRedeemedQuantity());

	            assertEquals(HttpStatus.OK.value(), httpCode);
	        })
	        .verifyComplete();
	}
	
	@Test
    void testMapRedeem_catchBlock_whenGetKeyThrowsException() {
        // Arrange: simular ResponseRedeem y que getKey() lanza excepción
        ResponseRedeemVoucherify response = new ResponseRedeemVoucherify();

        // Creamos un mock para response.getResponse()
        ResponseRedeem mockResponse = mock(ResponseRedeem.class);
        when(mockResponse.getRedemptions()).thenReturn(null); // Para entrar al bloque del try
        when(mockResponse.getKey()).thenThrow(new RuntimeException("Simulated exception"));

        response.setResponse(mockResponse);

        RequestRedeemVoucher request = new RequestRedeemVoucher();
        request.setCoupon("ABC123");
        request.setMemberId("MEM001");

        // Act
        Mono<Tuple2<ResponseRedeemVoucher, Integer>> resultMono = mapper.mapRedeem(response, request);

        // Assert
        StepVerifier.create(resultMono)
            .assertNext(tuple -> {
                ResponseRedeemVoucher dto = tuple.getT1();
                Integer code = tuple.getT2();

                assertFalse(dto.getValid());
                assertEquals("ABC123", dto.getCoupon());
                assertEquals("MEM001", dto.getMemberId());

                assertNotNull(dto.getResult());
                assertNotNull(dto.getResult().getError());
                assertEquals(code, dto.getResult().getError().getCode());
            })
            .verifyComplete();
    }
	
	@Test
    void testMapPublications_catchBlock_whenRepoThrowsException() {
        // Arrange
        ResponsePublicationsVoucherify response = new ResponsePublicationsVoucherify();

        ResponsePublications mockResponse = new ResponsePublications();
        mockResponse.setVoucher(null);     // Forzar flujo del else
        mockResponse.setVouchers(null);    // También null para que entre a try/catch
        mockResponse.setKey("ERROR_KEY");
        mockResponse.setCode(500);
        mockResponse.setMessage("Simulated message");

        response.setResponse(mockResponse);

        RequestPublicationVoucher request = new RequestPublicationVoucher();
        request.setMemberId("MEM001");
        request.setCampaign("CAMPAIGN123");
        request.setCount(2);

        // Simular excepción al consultar el repositorio
        when(repoMsCode.findByMessageKeyIgnoreCase(anyString(), anyString()))
                .thenThrow(new RuntimeException("Simulated DB failure"));

        // Act
        Mono<Tuple2<ResponsePublicationVoucher, Integer>> resultMono = mapper.mapPublications(response, request);

        // Assert
        StepVerifier.create(resultMono)
                .assertNext(tuple -> {
                    ResponsePublicationVoucher dto = tuple.getT1();
                    Integer code = tuple.getT2();

                    assertFalse(dto.getValid());
                    assertEquals("MEM001", dto.getMemberId());
                    assertEquals("CAMPAIGN123", dto.getCampaign());
                    assertEquals(2, dto.getCount());

                    assertNotNull(dto.getResult());
                    assertNotNull(dto.getResult().getError());
                    assertEquals(500, dto.getResult().getError().getCode()); // viene del response original
                    assertEquals("Simulated message", dto.getResult().getError().getMessage());

                    assertEquals(500, code); // también lo usamos como código HTTP
                })
                .verifyComplete();
    }

}
