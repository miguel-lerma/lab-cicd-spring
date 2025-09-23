/*
 * @(#)VoucherifyProperties.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.femsa.oxxo.voucher.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;


/**
 * Clase que recupera los valores de los secretos usados en la aplicación 
 * Los secretos se recuperan desde el archivo applications.properties
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */
@Component
@ConfigurationProperties(prefix = "voucherify")
@Data
public class VoucherifyProperties {

	private String url;
	private String channel;
	private String object;
	
	private App app;
	private Session session;
	private Url urlEndpoint;
	
}
