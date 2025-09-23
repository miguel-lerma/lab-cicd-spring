/*
 * @(#)App.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.femsa.oxxo.voucher.config;

import lombok.Data;

/**
 * Clase que representa el obj App de los secretos obtenidos desde application.properties
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */
@Data
public class App {
	
	private String id;
	private String token;

}
