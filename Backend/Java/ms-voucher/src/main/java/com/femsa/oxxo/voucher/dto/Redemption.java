/*
 * @(#)Redemption.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.femsa.oxxo.voucher.dto;

import lombok.Data;
/**
 * Clase que implementa el obj Redemption en el response del endpoint redeem
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */
@Data
public class Redemption {
	
	private Integer quantity;
	private Integer redeemedQuantity;

}
