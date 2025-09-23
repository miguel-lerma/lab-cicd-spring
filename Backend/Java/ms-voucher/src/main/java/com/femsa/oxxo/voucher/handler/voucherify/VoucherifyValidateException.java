/*
 * @(#)VoucherifyValidateException.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.femsa.oxxo.voucher.handler.voucherify;

import com.femsa.oxxo.voucher.dto.voucherify.validations.ResponseValidations;

/**
 * Clase que implementa una Exception personalizada para el endpoint Validate
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */
public class VoucherifyValidateException extends RuntimeException{
	
	private static final long serialVersionUID = 1L;
	
	private ResponseValidations response;

    public VoucherifyValidateException(ResponseValidations response) {
        this.response = response;
    }

    public ResponseValidations getResponse() {
        return response;
    }

}
