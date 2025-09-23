/*
 * @(#)RequestRedeemVoucherify.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.femsa.oxxo.voucher.dto.voucherify.redemptions;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.femsa.oxxo.voucher.dto.voucherify.validations.Redeemable;

import lombok.Data;

/**
 * Clase que implementa el request del endpoint redemptions de Voucherify
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */
@Data
public class RequestRedeemVoucherify {
	
	private List<Redeemable> redeemables;
	
	private Metadata metadata;
	
	@JsonProperty("tracking_id")
    private String trackingId;

}
