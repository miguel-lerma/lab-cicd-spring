/*
 * @(#)Order.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.femsa.oxxo.voucher.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Clase que implementa el obj Order para los request de los endpoints validate, redeem 
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */
@Data
public class Order {

	@Valid
	private List<Item> items;
	
	@Pattern(regexp = "^[A-Za-z0-9]+$", message = "Status inválido")
	private String status;
	
	@Min(1)
	private int amount;

}
