/*
 * @(#)IVoucherService.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.femsa.oxxo.voucher.service;

import com.femsa.oxxo.voucher.dto.RequestPublicationVoucher;
import com.femsa.oxxo.voucher.dto.RequestRedeemVoucher;
import com.femsa.oxxo.voucher.dto.RequestValidateVoucher;
import com.femsa.oxxo.voucher.dto.ResponsePublicationVoucher;
import com.femsa.oxxo.voucher.dto.ResponseRedeemVoucher;
import com.femsa.oxxo.voucher.dto.ResponseValidateVoucher;
import com.femsa.oxxo.voucher.dto.voucherify.publications.ResponsePublicationsVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.redemptions.ResponseRedeemVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.validations.ResponseValidateVoucherify;

import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;

/**
 * Interface que define los contratos de los metodos para los endpoints validate, redeem y publications
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */
public interface IVoucherService {
	
	public Mono<Tuple3<ResponseValidateVoucherify, ResponseValidateVoucher, Integer>> validateVoucher(RequestValidateVoucher request);
	
	public Mono<Tuple3<ResponseRedeemVoucherify, ResponseRedeemVoucher, Integer>> redeemVoucher(RequestRedeemVoucher request);
		
	public Mono<Tuple3<ResponsePublicationsVoucherify, ResponsePublicationVoucher, Integer>> publicationVoucher(RequestPublicationVoucher request);
	
}
