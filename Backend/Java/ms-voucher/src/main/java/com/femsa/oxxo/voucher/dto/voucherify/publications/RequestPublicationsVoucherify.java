/*
 * @(#)RequestPublicationsVoucherify.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.femsa.oxxo.voucher.dto.voucherify.publications;

import lombok.Data;

/**
 * Clase que implementa el request para el endpoint publications de Voucherify
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */
@Data
public class RequestPublicationsVoucherify {
	
    private CustomerRequest customer;
    private MetadataRequest metadata;
    private Campaign campaign;

}
