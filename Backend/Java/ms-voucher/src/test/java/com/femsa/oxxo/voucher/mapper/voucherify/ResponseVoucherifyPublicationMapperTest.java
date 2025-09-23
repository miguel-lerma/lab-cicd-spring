package com.femsa.oxxo.voucher.mapper.voucherify;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.femsa.oxxo.voucher.dto.RequestPublicationVoucher;
import com.femsa.oxxo.voucher.dto.ResponsePublicationVoucher;
import com.femsa.oxxo.voucher.dto.voucherify.publications.Customer;
import com.femsa.oxxo.voucher.dto.voucherify.publications.Metadata;
import com.femsa.oxxo.voucher.dto.voucherify.publications.ResponsePublications;
import com.femsa.oxxo.voucher.dto.voucherify.publications.ResponsePublicationsVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.publications.Voucher;
import com.femsa.oxxo.voucher.entity.MsRespCode;
import com.femsa.oxxo.voucher.repository.MsRespCodeRepository;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class ResponseVoucherifyPublicationMapperTest {
	
	@Mock
    private MsRespCodeRepository repoMsCode;

    private ResponseVoucherifyMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ResponseVoucherifyMapper(repoMsCode);
    }

    @Test
    void mapPublications_whenVoucherIsPresent() {

        Voucher voucher = new Voucher();
        voucher.setCode("VOUCHER123");
        voucher.setCampaignId("CAMP123");

        Metadata metadata = new Metadata();
        metadata.setMemberIdAssigned("MEM123");

        Customer customer = new Customer();
        customer.setMetadata(metadata);

        ResponsePublications responseBody = new ResponsePublications();
        responseBody.setResult("SUCCESS");
        responseBody.setCustomer(customer);
        responseBody.setVoucher(voucher);

        ResponsePublicationsVoucherify response = new ResponsePublicationsVoucherify();
        response.setResponse(responseBody);

        RequestPublicationVoucher request = new RequestPublicationVoucher();
        request.setCampaign("REQ-CAMP");
        request.setCount(1);
        request.setMemberId("MEM123");

        StepVerifier.create(mapper.mapPublications(response, request))
            .assertNext(tuple -> {
                ResponsePublicationVoucher result = tuple.getT1();
                assertTrue(result.getValid(), "La respuesta debe ser válida");
                assertEquals("MEM123", result.getMemberId());
                assertEquals("CAMP123", result.getCampaign());
                assertEquals(1, result.getCount());
                assertEquals(List.of("VOUCHER123"), result.getVouchers());
                assertNull(result.getResult(), "No debe haber errores");
                assertEquals(HttpStatus.OK.value(), tuple.getT2());
            })
            .verifyComplete();
    }
    
    @Test
    void mapPublications_whenVouchersIsPresent() {

        String memberId = "MEM123";
        String campaign = "SUMMER24";
        int count = 2;

        RequestPublicationVoucher request = new RequestPublicationVoucher();
        request.setMemberId(memberId);
        request.setCampaign(campaign);
        request.setCount(count);

        Metadata metadata = new Metadata();
        metadata.setMemberIdAssigned("ASSIGNED123");

        Customer customer = new Customer();
        customer.setMetadata(metadata);

        ResponsePublications publications = new ResponsePublications();
        publications.setResult("SUCCESS");
        publications.setCustomer(customer);
        publications.setVouchers(List.of("VOUCHER1", "VOUCHER2"));
        publications.setCode(200);
        publications.setMessage("OK");

        ResponsePublicationsVoucherify response = new ResponsePublicationsVoucherify();
        response.setResponse(publications);

        StepVerifier.create(mapper.mapPublications(response, request))
            .assertNext(tuple -> {
                ResponsePublicationVoucher result = tuple.getT1();
                assertTrue(result.getValid());
                assertEquals("ASSIGNED123", result.getMemberId());
                assertEquals(campaign, result.getCampaign());
                assertEquals(count, result.getCount());
                assertEquals(List.of("VOUCHER1", "VOUCHER2"), result.getVouchers());
                assertEquals(HttpStatus.OK.value(), tuple.getT2());
            })
            .verifyComplete();
    }
    
    @Test
    void mapPublications_ErrorResponseWithRespCodeFound() {

        ResponsePublications responseBody = new ResponsePublications();
        responseBody.setCode(400);
        responseBody.setKey("unauthorized");
        responseBody.setMessage("unauthorized");

        ResponsePublicationsVoucherify response = new ResponsePublicationsVoucherify();
        response.setResponse(responseBody);

        RequestPublicationVoucher request = new RequestPublicationVoucher();
        request.setOperation("PUB");
        request.setCount(2);
        
        MsRespCode mockRespCode = new MsRespCode();
        mockRespCode.setCodeMs(400L);
        mockRespCode.setCodeHttpMs(400L);
        mockRespCode.setMessageKey("unauthorized");
        mockRespCode.setMessagePos("NO autorizado");

        when(repoMsCode.findByMessageKeyIgnoreCase(anyString(), eq("publication")))
        .thenReturn(Mono.just(mockRespCode));

        StepVerifier.create(mapper.mapPublications(response, request))
            .assertNext(tuple -> {
                ResponsePublicationVoucher result = tuple.getT1();
                assertFalse(result.getValid());
                assertNotNull(result.getResult());
                assertEquals(400, result.getResult().getError().getCode());
                assertEquals("NO autorizado", result.getResult().getError().getMessage());
                assertEquals(HttpStatus.BAD_REQUEST.value(), tuple.getT2());
            })
            .verifyComplete();
    }
    
    @Test
    void mapPublications_ErrorResponseWithRespCodeNotFound() {

        ResponsePublications responseBody = new ResponsePublications();
        responseBody.setCode(401);
        responseBody.setKey("ERR_KEY");
        responseBody.setMessage("Unauthorized access");

        ResponsePublicationsVoucherify response = new ResponsePublicationsVoucherify();
        response.setResponse(responseBody);

        RequestPublicationVoucher request = new RequestPublicationVoucher();
        request.setOperation("PUB");
        request.setCount(2);

        Mockito.when(repoMsCode.findByMessageKeyIgnoreCase(anyString(), anyString()))
               .thenReturn(Mono.empty());

        StepVerifier.create(mapper.mapPublications(response, request))
            .assertNext(tuple -> {
                ResponsePublicationVoucher result = tuple.getT1();
                assertFalse(result.getValid());
                assertNotNull(result.getResult());
                assertEquals(401, result.getResult().getError().getCode());
                assertEquals("Unauthorized access", result.getResult().getError().getMessage());
            })
            .verifyComplete();
    }
    
    @Test
    void mapPublications_whenVouchersListIsPresentAndResultIsFailed() {

        String memberId = "MEM456";
        String campaign = "WINTER25";
        int count = 3;

        RequestPublicationVoucher request = new RequestPublicationVoucher();
        request.setMemberId(memberId);
        request.setCampaign(campaign);
        request.setCount(count);

        Metadata metadata = new Metadata();

        Customer customer = new Customer();
        customer.setMetadata(metadata);

        ResponsePublications publications = new ResponsePublications();
        publications.setResult("FAILED");
        publications.setCustomer(customer);
        publications.setVouchers(List.of("VOUCHER10", "VOUCHER20"));
        publications.setCode(200);
        publications.setMessage("OK");
        

        ResponsePublicationsVoucherify response = new ResponsePublicationsVoucherify();
        response.setResponse(publications);

        StepVerifier.create(mapper.mapPublications(response, request))
            .assertNext(tuple -> {
                ResponsePublicationVoucher result = tuple.getT1();
                assertFalse(result.getValid());
                assertEquals(campaign, result.getCampaign());
                assertEquals(count, result.getCount());
                assertEquals(List.of("VOUCHER10", "VOUCHER20"), result.getVouchers());
                assertEquals(HttpStatus.OK.value(), tuple.getT2());
            })
            .verifyComplete();
    }
    
    @Test
    void mapPublications_shouldReturnVoucherifyError_whenRepositoryFails() {

        RequestPublicationVoucher request = new RequestPublicationVoucher();
        request.setMemberId("MEM001");
        request.setCampaign("CAMPAIGN01");
        request.setCount(1);

        ResponsePublications publications = new ResponsePublications();
        publications.setCode(500);
        publications.setKey("internal.error");
        publications.setMessage("Internal server error from voucherify");

        ResponsePublicationsVoucherify response = new ResponsePublicationsVoucherify();
        response.setResponse(publications);

        when(repoMsCode.findByMessageKeyIgnoreCase(anyString(), eq("publication")))
            .thenReturn(Mono.error(new RuntimeException("Simulated DB failure")));

        StepVerifier.create(mapper.mapPublications(response, request))
            .assertNext(tuple -> {
                ResponsePublicationVoucher result = tuple.getT1();
                assertFalse(result.getValid());
                assertNotNull(result.getResult());
                assertEquals(500, result.getResult().getError().getCode());
                assertEquals("Internal server error from voucherify", result.getResult().getError().getMessage());
            })
            .verifyComplete();
    }
    
    @Test
    void mapPublications_shouldUseGenericMessage_whenMessageKeyNotFound() {

        RequestPublicationVoucher request = new RequestPublicationVoucher();
        request.setMemberId("MEM001");
        request.setCampaign("GENERIC_CAMPAIGN");
        request.setCount(1);

        ResponsePublications publications = new ResponsePublications();
        publications.setCode(404);
        publications.setKey("voucher.unknown");
        publications.setMessage("Mensaje original de Voucherify");

        ResponsePublicationsVoucherify response = new ResponsePublicationsVoucherify();
        response.setResponse(publications);

        when(repoMsCode.findByMessageKeyIgnoreCase(eq("voucher.unknown"), eq("publication")))
            .thenReturn(Mono.empty());

        MsRespCode genericCode = new MsRespCode();
        genericCode.setCodeMs(999L);
        genericCode.setCodeHttpMs(400L);
        genericCode.setMessagePos("Mensaje genérico desde BD");

        when(repoMsCode.findByMessageKeyIgnoreCase(eq("generic"), eq("publication")))
            .thenReturn(Mono.just(genericCode));

        StepVerifier.create(mapper.mapPublications(response, request))
            .assertNext(tuple -> {
                ResponsePublicationVoucher result = tuple.getT1();
                assertFalse(result.getValid());
                assertNotNull(result.getResult());
                assertEquals(999, result.getResult().getError().getCode());
                assertEquals("Mensaje genérico desde BD", result.getResult().getError().getMessage());
                assertEquals(400, tuple.getT2());
            })
            .verifyComplete();
    }
    
    

}
