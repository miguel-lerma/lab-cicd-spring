/*
 * @(#)MetadataResponse.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.femsa.oxxo.voucher.dto.voucherify.validations;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Clase que implementa el obj metadata en el response del endpoint validations de Voucherify
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class MetadataResponse {
	
    @JsonProperty("MD_Voucher_Type_Coupon")
    private Integer mdVoucherTypeCoupon;

    @JsonProperty("MD_Voucher_RMS_Transaction_ID")
    private Long mdVoucherRmsTransactionId;
    
    @JsonProperty("MD_Voucher_Member_ID_Assigned")
    private String mdVoucherMemberIdAssigned;

}
