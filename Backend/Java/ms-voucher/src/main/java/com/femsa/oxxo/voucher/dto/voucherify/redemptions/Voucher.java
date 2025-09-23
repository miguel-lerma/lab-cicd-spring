package com.femsa.oxxo.voucher.dto.voucherify.redemptions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * Clase que implementa el obj voucher del response de Voucherify para el endpoint redemptions
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Voucher {
	
	private String code;
	private RedemptionDetails redemption;

}
