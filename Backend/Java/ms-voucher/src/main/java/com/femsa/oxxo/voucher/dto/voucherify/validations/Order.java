/*
 * @(#)Orders.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.femsa.oxxo.voucher.dto.voucherify.validations;

import java.util.List;

import lombok.Data;

/**
 * Clase que implementa el obj order en el request del endpoint validations de Voucherify
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */
@Data
public class Order {
	
	private List<OrderItem> items;
    private String status;
    private int amount;
    
}
