/*
 * @(#)StackingRules.java 1.0.0 12/05/25
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
 * Clase que implementa el StackingRules order en el response del endpoint validations de Voucherify
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class StackingRules {
	
    @JsonProperty("redeemables_limit")
    private Integer redeemablesLimit;
    
    @JsonProperty("applicable_redeemables_limit")
    private Integer applicableRedeemablesLimit;

    @JsonProperty("applicable_exclusive_redeemables_limit")
    private Integer applicableExclusiveRedeemablesLimit;

}
