/*
 * @(#)ResponseValidateVoucherify.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.femsa.oxxo.voucher.dto.voucherify.validations;

import java.time.OffsetDateTime;

import lombok.Data;

/**
 * Clase que implementa el response del cliente Voucherify para el endpoint validate
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */
@Data
public class ResponseValidateVoucherify {

	private ResponseValidations response;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private String requestJson;  
    private String responseJson; 
    
	public ResponseValidateVoucherify() {
		super();
	}

	public ResponseValidateVoucherify(ResponseValidations response, OffsetDateTime startTime, OffsetDateTime endTime,
			String requestJson, String responseJson) {
		super();
		this.response = response;
		this.startTime = startTime;
		this.endTime = endTime;
		this.requestJson = requestJson;
		this.responseJson = responseJson;
	}
    
}
