package com.femsa.oxxo.voucher.repository;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.femsa.oxxo.voucher.entity.MsLog;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class MsLogRepositoryTest {

	@Mock
    private MsLogRepository msLogRepository;

    @Test
    void saveLog_ReturnSavedLog() {
        MsLog log = MsLog.builder()
                .errorId(1L)
                .errorDate(LocalDateTime.now())
                .severity("HIGH")
                .errorType("Validation")
                .errorCode(400)
                .description("Missing field")
                .message("Invalid input")
                .action("Check input values")
                .place("MX001")
                .store("Store123")
                .cash(1)
                .operation("VALIDATE")
                .idTransaction(10L)
                .build();

        when(msLogRepository.save(log)).thenReturn(Mono.just(log));

        StepVerifier.create(msLogRepository.save(log))
            .expectNextMatches(savedLog -> savedLog.getErrorId().equals(1L) &&
                                           savedLog.getMessage().equals("Invalid input"))
            .verifyComplete();

        verify(msLogRepository, times(1)).save(log);
    }

}
