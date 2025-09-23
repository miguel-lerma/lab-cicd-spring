/*
 * @(#)ResponseValidateVoucher.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.femsa.oxxo.voucher.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

/**
 * Clase que implementa el response del endpoint validate 
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "valid", "typeCoupon", "coupon", "voucherRmsId", "memberId", "redeemablesLimit", "applicableRedeemablesLimit",
		"applicableExclusiveRedeemablesLimit", "result" })
@Data
public class ResponseValidateVoucher {

	private Boolean valid;
	private Integer typeCoupon;
	private String coupon;
	private Long voucherRmsId;
	private String memberId;
	private Integer redeemablesLimit;
	private Integer applicableRedeemablesLimit;
	private Integer applicableExclusiveRedeemablesLimit;
	private Result result;

}
