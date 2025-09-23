/*
 * @(#)ITransactionLogService.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.femsa.oxxo.voucher.service;

import com.femsa.oxxo.voucher.entity.MsLog;
import com.femsa.oxxo.voucher.entity.MsTransaction;

import reactor.core.publisher.Mono;

/**
 * Interface que define los contratos de los metodos para guardar logs y transactions
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */
public interface ITransactionLogService {

	public Mono<MsTransaction> saveTransaction(MsTransaction transaction);
	
	public Mono<Void> saveTransactionAndLog(MsTransaction transaction, Mono<MsLog> log);
}
