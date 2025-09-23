/*
 * @(#)RedemptionDetails.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.femsa.oxxo.voucher.dto.voucherify.redemptions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Clase que representa el obj redemption
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class RedemptionDetails {

	private Integer quantity;
	
    @JsonProperty("redeemed_quantity")
    private Integer redeemedQuantity;
    
}
