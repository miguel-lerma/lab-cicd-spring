/*
 * @(#)ConsumerData.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.femsa.oxxo.voucher.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Clase que representa el obj consumerData para los request de validate y redeeem
 * 
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 * 
 */
@Data
public class ConsumerData {

	@Pattern(regexp = "^[A-Za-z0-9]+$", message = "Application inválida")
	private String application;
	
    @Pattern(regexp = "^[A-Za-z0-9]{3}$", message = "Entity inválida") 
	private String entity;
	
    @Pattern(regexp = "^[A-Za-z0-9]{3}$", message = "Origin inválido")
	private String origin;
	
    @Pattern(regexp = "^[A-Za-z0-9]{5}$", message = "CrPlaza inválida")
	private String crPlace;
	
    @Pattern(regexp = "^[A-Za-z0-9]{5}$", message = "CrTienda inválida")
	private String crStore;
	
	@Min(1)
	private int cashRegister;

}
