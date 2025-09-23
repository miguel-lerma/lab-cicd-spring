/*
 * @(#)ResponsePublicationsVoucherify.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.femsa.oxxo.voucher.dto.voucherify.publications;

import java.time.OffsetDateTime;

import lombok.Data;

/**
 * Clase que implementa la respuesta del cliente Voucherify para el enpoint publication
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */
@Data
public class ResponsePublicationsVoucherify {

	private ResponsePublications response;
	private OffsetDateTime startTime;
	private OffsetDateTime endTime;
	private String requestJson;
	private String responseJson;
	
	public ResponsePublicationsVoucherify() {
		super();
	}
	
	public ResponsePublicationsVoucherify(ResponsePublications response, OffsetDateTime startTime,
			OffsetDateTime endTime, String requestJson, String responseJson) {
		super();
		this.response = response;
		this.startTime = startTime;
		this.endTime = endTime;
		this.requestJson = requestJson;
		this.responseJson = responseJson;
	}

}
