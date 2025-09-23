/*
 * @(#)IClientVoucher.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.femsa.oxxo.voucher.client.voucher;

import com.femsa.oxxo.voucher.dto.RequestPublicationVoucher;
import com.femsa.oxxo.voucher.dto.RequestRedeemVoucher;
import com.femsa.oxxo.voucher.dto.RequestValidateVoucher;
import com.femsa.oxxo.voucher.dto.voucherify.publications.ResponsePublicationsVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.redemptions.ResponseRedeemVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.validations.ResponseValidateVoucherify;

import reactor.core.publisher.Mono;

/**
 * Interface que define los contratos de los metodos para el cliente Voucherify.
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */
public interface IClientVoucher {
	
	/**
	 * Método para validar si un cupon es valido o no 
	 *
	 * @param RequestValidateVoucher request
	 * @return Mono<ResponseValidateVoucherify>
	 * @since 1.0.0
	 */
	public Mono<ResponseValidateVoucherify> validateVoucher(RequestValidateVoucher request);
	
	/**
	 * Método para redimir un cupon 
	 *
	 * @param RequestRedeemVoucher request
	 * @return Mono<ResponseRedeemVoucherify>
	 * @since 1.0.0
	 */
	public Mono<ResponseRedeemVoucherify> redeemVoucher(RequestRedeemVoucher request);
	
	/**
	 * Método para activar (publicar) un cupon asociado a un memberId
	 *
	 * @param RequestRedeemVoucher request
	 * @return Mono<ResponseRedeemVoucherify>
	 * @since 1.0.0
	 */
	public Mono<ResponsePublicationsVoucherify> publicationVoucher(RequestPublicationVoucher request);

}
