/*
 * @(#)TransactionLogServiceImpl.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.femsa.oxxo.voucher.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.femsa.oxxo.voucher.entity.MsLog;
import com.femsa.oxxo.voucher.entity.MsTransaction;
import com.femsa.oxxo.voucher.repository.MsLogRepository;
import com.femsa.oxxo.voucher.repository.MsTransactionRepository;
import com.femsa.oxxo.voucher.service.ITransactionLogService;
import com.femsa.oxxo.voucher.utils.LogSanitizer;

import reactor.core.publisher.Mono;

/**
 * Clase que implementa los metodos para guardar logs y transactions
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */
@Service
public class TransactionLogServiceImpl implements ITransactionLogService {

	private static final Logger logger = LoggerFactory.getLogger(TransactionLogServiceImpl.class);

	private final MsTransactionRepository transactionRepository;
	private final MsLogRepository logRepository;

	public TransactionLogServiceImpl(MsTransactionRepository transactionRepository, MsLogRepository logRepository) {
		this.transactionRepository = transactionRepository;
		this.logRepository = logRepository;
	}

	/**
	 * Método para guardar transactions
	 *
	 * @param MsTransaction transaction
	 * @return Mono<MsTransaction>
	 * @since 1.0.0
	 */
	@Override
	public Mono<MsTransaction> saveTransaction(MsTransaction transaction) {
		return transactionRepository.save(transaction)
				.doOnSuccess(savedTransaction -> logger.info("Transacción guardada con ID {}, TransactionId {} ", LogSanitizer.sanitize(savedTransaction.getId().toString()), LogSanitizer.sanitize(savedTransaction.getIdTicket())))
				.doOnError(error -> logger.info("Error al guardar la transacción {}, {} : ", LogSanitizer.sanitize(transaction.getIdTicket()), LogSanitizer.sanitize(error.getMessage())));
	}

	
	/**
	 * Método para guardar transactions y logs
	 *
	 * @param (MsTransaction transaction, Mono<MsLog> log
	 * @return Mono<Void>
	 * @since 1.0.0
	 */
	@Override
	public Mono<Void> saveTransactionAndLog(MsTransaction transaction, Mono<MsLog> log) {
		return transactionRepository.save(transaction)
				.doOnSuccess(savedTransaction -> logger.info("Transacción guardada con ID {}, TransactionId {} ", LogSanitizer.sanitize(savedTransaction.getId().toString()), LogSanitizer.sanitize(savedTransaction.getIdTicket())))
		        .flatMap(savedTransaction -> 
		            log.flatMap(msLog -> {
		                msLog.setIdTransaction(savedTransaction.getId()); // Asocia la transacción al log
		                return logRepository.save(msLog)//
		                .doOnSuccess(savedLog -> logger.info("Log guardado con ID {}, TransactionId {}", LogSanitizer.sanitize(savedLog.getErrorId().toString()), LogSanitizer.sanitize(savedLog.getIdTransaction().toString())))
		        		.doOnError(error -> logger.info("Error al guardar el log {}, {} ", LogSanitizer.sanitize(msLog.getIdTransaction().toString()), LogSanitizer.sanitize(error.getMessage())));
		            })
		        )
		        .doOnError(error -> logger.info("Error al guardar la transacción, {}, {} : ", LogSanitizer.sanitize(transaction.getIdTicket()), LogSanitizer.sanitize(error.getMessage())))
		        .then();
	}

}
