/*
 * @(#)ResponseRedeemVoucher.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.femsa.oxxo.voucher.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

/**
 * Clase que implementa el response del endpoint redeem 
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "valid", "coupon", "memberId", "redemption", "result" })
@Data
public class ResponseRedeemVoucher {
	
	private Boolean valid;
	private String coupon;
	private String memberId;
	private Redemption redemption;
	private Result result;

}
