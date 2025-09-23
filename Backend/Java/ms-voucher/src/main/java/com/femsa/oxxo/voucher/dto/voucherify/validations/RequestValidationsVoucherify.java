/*
 * @(#)RequestValidationsVoucherify.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.femsa.oxxo.voucher.dto.voucherify.validations;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Clase que implementa el request del endpoint validations de Voucherify
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */
@Data
public class RequestValidationsVoucherify {
	
    private List<Redeemable> redeemables;
    private Order order;
    private Session session;
    private Metadata metadata;
    
    @JsonProperty("tracking_id")
    private String trackingId;
    
}
