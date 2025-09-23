/*
 * @(#)Result.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.femsa.oxxo.voucher.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * Clase que implementa el obj Result para los response de los metodos validate, redeem y publication
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Result {

	private Error error;
}
