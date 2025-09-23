package com.femsa.oxxo.voucher.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.femsa.oxxo.voucher.entity.MsLog;
import com.femsa.oxxo.voucher.entity.MsTransaction;
import com.femsa.oxxo.voucher.repository.MsLogRepository;
import com.femsa.oxxo.voucher.repository.MsTransactionRepository;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class TransactionLogServiceImplTest {

    @Mock
    private MsTransactionRepository transactionRepository;

    @Mock
    private MsLogRepository logRepository;

    @InjectMocks
    private TransactionLogServiceImpl transactionLogService;

    @Test
    void testSaveTransaction_success() {
        MsTransaction transaction = MsTransaction.builder().id(1L).operation("VAL").build();

        Mockito.when(transactionRepository.save(transaction)).thenReturn(Mono.just(transaction));

        StepVerifier.create(transactionLogService.saveTransaction(transaction))
            .expectNext(transaction)
            .verifyComplete();

        Mockito.verify(transactionRepository).save(transaction);
    }

    @Test
    void testSaveTransaction_error() {
        MsTransaction transaction = MsTransaction.builder().operation("VAL").build();
        RuntimeException ex = new RuntimeException("DB error");

        Mockito.when(transactionRepository.save(transaction)).thenReturn(Mono.error(ex));

        StepVerifier.create(transactionLogService.saveTransaction(transaction))
            .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                                              throwable.getMessage().equals("DB error"))
            .verify();

        Mockito.verify(transactionRepository).save(transaction);
    }

    @Test
    void testSaveTransactionAndLog_success() {
        MsTransaction transaction = MsTransaction.builder().id(1L).operation("VAL").build();
        MsLog log = MsLog.builder().errorId(10L).message("Error").build();
        MsLog logWithTransactionId = MsLog.builder().errorId(10L).message("Error").idTransaction(1L).build();

        Mockito.when(transactionRepository.save(transaction)).thenReturn(Mono.just(transaction));
        Mockito.when(logRepository.save(Mockito.any(MsLog.class))).thenReturn(Mono.just(logWithTransactionId));

        StepVerifier.create(transactionLogService.saveTransactionAndLog(transaction, Mono.just(log)))
            .verifyComplete();

        Mockito.verify(transactionRepository).save(transaction);
        Mockito.verify(logRepository).save(Mockito.argThat(l -> l.getIdTransaction().equals(1L)));
    }

    @Test
    void testSaveTransactionAndLog_transactionFails() {
        MsTransaction transaction = MsTransaction.builder().operation("VAL").build();
        RuntimeException ex = new RuntimeException("Transaction failed");

        Mockito.when(transactionRepository.save(transaction)).thenReturn(Mono.error(ex));

        StepVerifier.create(transactionLogService.saveTransactionAndLog(transaction, Mono.just(MsLog.builder().build())))
            .expectError(RuntimeException.class)
            .verify();

        Mockito.verify(transactionRepository).save(transaction);
        Mockito.verify(logRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void testSaveTransactionAndLog_logFails() {
        MsTransaction transaction = MsTransaction.builder().id(1L).operation("VAL").build();
        MsLog log = MsLog.builder().message("fail log").build();
        RuntimeException ex = new RuntimeException("Log save failed");

        Mockito.when(transactionRepository.save(transaction)).thenReturn(Mono.just(transaction));
        Mockito.when(logRepository.save(Mockito.any(MsLog.class))).thenReturn(Mono.error(ex));

        StepVerifier.create(transactionLogService.saveTransactionAndLog(transaction, Mono.just(log)))
            .expectError(RuntimeException.class)
            .verify();

        Mockito.verify(transactionRepository).save(transaction);
        Mockito.verify(logRepository).save(Mockito.any(MsLog.class));
    }
}