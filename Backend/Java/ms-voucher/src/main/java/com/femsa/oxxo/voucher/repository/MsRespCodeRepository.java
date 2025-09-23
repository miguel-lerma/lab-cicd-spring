/*
 * @(#)MsRespCodeRepository.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.femsa.oxxo.voucher.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.femsa.oxxo.voucher.entity.MsRespCode;

import reactor.core.publisher.Mono;

/**
 * Repository para la entity MsRespCode
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */
@Repository
public interface MsRespCodeRepository extends ReactiveCrudRepository<MsRespCode, Long>{
	
	@Query("SELECT * FROM backend_schema.ms_resp_code WHERE LOWER(message_key) = LOWER(:messageKey) and operation = :operation")
	Mono<MsRespCode> findByMessageKeyIgnoreCase(String messageKey, String operation);
	
}
