package com.femsa.oxxo.voucher.repository;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.femsa.oxxo.voucher.entity.MsTransaction;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class MsTransactionRepositoryTest {

	@Mock
    private MsTransactionRepository msTransactionRepository;

    @Test
    void findById_shouldReturnTransaction() {
        MsTransaction transaction = MsTransaction.builder()
                .id(1L)
                .place("MX001")
                .store("S001")
                .cash(1)
                .transactionDateChannel(LocalDate.now())
                .transactionTimeChannel(LocalTime.now())
                .code(200)
                .message("Transaction OK")
                .dataRequest("{\"voucher\":\"ABC123\"}")
                .dataResponse("{\"status\":\"VALID\"}")
                .application("POS")
                .entity("Store")
                .source("API")
                .operation("VALIDATE")
                .datetimeRequest(OffsetDateTime.now().minusSeconds(2))
                .datetimeResponse(OffsetDateTime.now())
                .coupon("ABC123")
                .noTicket("TCK123")
                .idTicket("ID123")
                .requestServer("10.0.0.1")
                .operator("OP123")
                .httpCode(200)
                .transactionDateRequest(OffsetDateTime.now().minusSeconds(3))
                .transactionDateResponse(OffsetDateTime.now())
                .dataRequestProvider("{\"provider_req\":\"value\"}")
                .dataResponseProvider("{\"provider_res\":\"value\"}")
                .build();

        when(msTransactionRepository.findById(1L)).thenReturn(Mono.just(transaction));

        StepVerifier.create(msTransactionRepository.findById(1L))
            .expectNextMatches(tx -> tx.getId().equals(1L) && tx.getCoupon().equals("ABC123"))
            .verifyComplete();

        verify(msTransactionRepository, times(1)).findById(1L);
    }

}
