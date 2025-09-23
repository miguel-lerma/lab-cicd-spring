/*
 * @(#)MetadataRequest.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.femsa.oxxo.voucher.dto.voucherify.publications;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Clase que representa el obj metadata en el request del endpoint publication
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */
@Data
public class MetadataRequest {
	
	@JsonProperty("MD_Publication_Company")
    private String publicationCompany;

    @JsonProperty("MD_Publication_Application")
    private String publicationApplication;

}
