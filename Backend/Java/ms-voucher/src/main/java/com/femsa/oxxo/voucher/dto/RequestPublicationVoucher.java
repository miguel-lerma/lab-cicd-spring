/*
 * @(#)RequestPublicationVoucher.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.femsa.oxxo.voucher.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Clase que implementa el request del endpoint publication 
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */
@Data
public class RequestPublicationVoucher {
	
	@NotNull(message = "El elemento operation es obligatorio")
	@Pattern(regexp = "^[A-Z]{3}$", message = "Operación inválida") 
	private String operation;
	
	@Valid
    private ConsumerDataRequestPublication consumerData;
    
	@Pattern(regexp = "^[0-9]{8}$", message = "Fecha inválida, el formato correcto es YYYYMMDD")
	private String date;
	
	@Pattern(regexp = "^[0-9]{6}$", message = "Hora inválida, el formato correcto es HHMMSS")
	private String hour;
	
	@Pattern(regexp = "^[A-Za-z0-9]+$", message = "User inválido")
	private String user;
	
	@NotNull(message = "El elemento MemberId es obligatorio")
	@Pattern(regexp = "^[A-Za-z0-9-]{1,50}$", message = "MemberId inválido")
	private String memberId;
	
	@Pattern(regexp = "^[A-Za-z0-9_]+$", message = "Campaign inválida")
	private String campaign;
	
	private Integer count;
	
	public void validCount() {
        if (this.count == null || this.count <= 0) {
            this.count = 1;
        }
    }
	
}
