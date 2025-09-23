/*
 * @(#)OrderItem.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.femsa.oxxo.voucher.dto.voucherify.validations;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Clase que implementa el obj item del request del endpoint validations de Voucherify
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */
@Data
public class OrderItem {
	
    @JsonProperty("sku_id")
    private String skuId;
    
    @JsonProperty("related_object")
    private String relatedObject;
    
    private int quantity;
    private int amount;
    private Double price;

}
