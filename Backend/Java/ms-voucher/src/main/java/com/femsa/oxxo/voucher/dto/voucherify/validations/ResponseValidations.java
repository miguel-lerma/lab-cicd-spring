/*
 * @(#)ResponseValidations.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.femsa.oxxo.voucher.dto.voucherify.validations;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Clase que implementa response del endpoint validations de Voucherify
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ResponseValidations {
	
    private boolean valid;
    private List<RedeemableResponse> redeemables;
    
    @JsonProperty("stacking_rules")
    private StackingRules stackingRules;
    
	private Integer code;
    private String key;
    private String message;

}
