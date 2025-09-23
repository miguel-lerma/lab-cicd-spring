/*
 * @(#)Item.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.femsa.oxxo.voucher.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Clase que implementa el obj Item para los request de los endpoints validate, redeem 
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */
@Data
public class Item {
	
	@Pattern(regexp = "^[0-9]+$", message = "SkuId inválido")
	@JsonProperty("sku_id")
    private String skuId;
	
	@Pattern(regexp = "^[A-Za-z]+$", message = "Related Object inválido")
	@JsonProperty("related_object")
    private String relatedObject;
	
	@Min(1)
	private int quantity;
	
	@Min(1)
	private int amount;
	
	@Min(1)
	private Double price;

}
