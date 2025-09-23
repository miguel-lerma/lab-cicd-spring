/*
 * @(#)VoucherifyRedemptionException.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.femsa.oxxo.voucher.handler.voucherify;

import com.femsa.oxxo.voucher.dto.voucherify.redemptions.ResponseRedeem;

/**
 * Clase que implementa una Exception personalizada para el endpoint redeem
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */
public class VoucherifyRedemptionException extends RuntimeException{

	private static final long serialVersionUID = 1L;
	
	private ResponseRedeem response;
	
    public VoucherifyRedemptionException(ResponseRedeem response) {
        this.response = response;
    }

    public ResponseRedeem getResponse() {
        return response;
    }

}
