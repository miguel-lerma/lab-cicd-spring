/*
 * @(#)ResponseRedeemVoucherify.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.femsa.oxxo.voucher.dto.voucherify.redemptions;

import java.time.OffsetDateTime;

import lombok.Data;

/**
 * Clase que representa la respuesta del cliente Voucherify para el endpoint redeem
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */
@Data
public class ResponseRedeemVoucherify {
	
	private ResponseRedeem response;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private String requestJson;  
    private String responseJson;
    
	public ResponseRedeemVoucherify(ResponseRedeem response, OffsetDateTime startTime, OffsetDateTime endTime,String requestJson, String responseJson) {
		super();
		this.response = response;
		this.startTime = startTime;
		this.endTime = endTime;
		this.requestJson = requestJson;
		this.responseJson = responseJson;
	}

	public ResponseRedeemVoucherify() {
		super();
	} 

}
