/*
 * @(#)RequestRedeemVoucher.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.femsa.oxxo.voucher.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Clase que implementa el request del endpoint redeem 
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */
@Data
public class RequestRedeemVoucher {
	
	@NotNull(message = "Elemento obligatorio")
	@Pattern(regexp = "^[A-Z]{3}$", message = "Operación inválida") 
	private String operation;
	
	@Valid
	private ConsumerData consumerData;
	
	@Valid
	private Order order;
	
	@NotNull(message = "Elemento obligatorio")
	@Pattern(regexp = "^[A-Za-z0-9]+$", message = "Cupón inválido")
	private String coupon;
	
	@Pattern(regexp = "^[0-9]{8}$", message = "Fecha inválida, el formato correcto es YYYYMMDD")
	private String cashdate;
	
	@Pattern(regexp = "^[0-9]{6}$", message = "Hora inválida, el formato correcto es HHMMSS")
	private String cashhour;
	
	@Pattern(regexp = "^[A-Za-z0-9]+$", message = "Cashier inválido")
	private String cashier;
	
	@Min(1)
	private int ticket;
	
	@Pattern(regexp = "^[A-Za-z0-9]+$", message = "TransactionId inválido")
	private String transactionId;
	
	@Pattern(regexp = "^[A-Za-z0-9]+$", message = "Server inválido")
	private String server;
	
	@Pattern(regexp = "^[A-Za-z0-9-]{1,50}$", message = "MemberId inválido")
	private String memberId;

}
