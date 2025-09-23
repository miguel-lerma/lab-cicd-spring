/*
 * @(#)Redeemable.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.femsa.oxxo.voucher.dto.voucherify.validations;

import lombok.Data;

/**
 * Clase que implementa el obj redeemable en el request del endpoint validations de Voucherify
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */
@Data
public class Redeemable {
	
    private String object;
    private String id;
  
}
