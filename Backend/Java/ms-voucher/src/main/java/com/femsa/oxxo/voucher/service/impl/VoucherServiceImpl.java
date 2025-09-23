/*
 * @(#)VoucherServiceImpl.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.femsa.oxxo.voucher.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.femsa.oxxo.voucher.client.voucher.IClientVoucher;
import com.femsa.oxxo.voucher.dto.RequestPublicationVoucher;
import com.femsa.oxxo.voucher.dto.RequestRedeemVoucher;
import com.femsa.oxxo.voucher.dto.RequestValidateVoucher;
import com.femsa.oxxo.voucher.dto.ResponseError;
import com.femsa.oxxo.voucher.dto.ResponsePublicationVoucher;
import com.femsa.oxxo.voucher.dto.ResponseRedeemVoucher;
import com.femsa.oxxo.voucher.dto.ResponseValidateVoucher;
import com.femsa.oxxo.voucher.dto.voucherify.publications.ResponsePublicationsVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.redemptions.ResponseRedeemVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.validations.ResponseValidateVoucherify;
import com.femsa.oxxo.voucher.mapper.voucherify.ResponseVoucherifyMapper;
import com.femsa.oxxo.voucher.service.IVoucherService;

import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

/**
 * Clase que implementa los metodos para los llamadas a los endpoints validate, redeem y publications
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */
@Service
public class VoucherServiceImpl implements IVoucherService {
	
	private static final Logger logger = LoggerFactory.getLogger(VoucherServiceImpl.class);

	private final IClientVoucher clientVoucherify;
	private final ResponseVoucherifyMapper responseVoucherifyMapper;

	public VoucherServiceImpl(
			@Qualifier("ClientVoucherifyImpl") IClientVoucher clientVoucherify, ResponseVoucherifyMapper responseVoucherifyMapper) {
		this.clientVoucherify = clientVoucherify;
		this.responseVoucherifyMapper = responseVoucherifyMapper;
	}

	/**
	 * Método que gestiona la llamada al cliente de cupones para el metodo validate 
	 *
	 * @param RequestValidateVoucher request
	 * @return ResponseValidateVoucherify, ResponseValidateVoucher, Integer
	 * @since 1.0.0
	 */
	@Override
	public Mono<Tuple3<ResponseValidateVoucherify, ResponseValidateVoucher, Integer>> validateVoucher(RequestValidateVoucher request) {
		
	    return clientVoucherify.validateVoucher(request)
	    		.flatMap(response -> 
	            responseVoucherifyMapper.mapValidation(response, request)
	                .map(tuple -> Tuples.of(response, tuple.getT1(), tuple.getT2()))
			)
	            .onErrorResume(e -> {
	                logger.error("Error en validación de VoucherServiceImpl.validateVoucher ", e);
	                ResponseValidateVoucherify response = new ResponseValidateVoucherify();
	                ResponseValidateVoucher errorResponse = ResponseError.buildErrorValidateResponse(
	                        HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal error");
	                return Mono.just(Tuples.of(response, errorResponse, HttpStatus.INTERNAL_SERVER_ERROR.value()));
	            });
                
    }

	/**
	 * Método que gestiona la llamada al cliente de cupones para el metodo redeem 
	 *
	 * @param RequestRedeemVoucher request
	 * @return ResponseRedeemVoucherify, ResponseRedeemVoucher, Integer
	 * @since 1.0.0
	 */
	@Override
	public Mono<Tuple3<ResponseRedeemVoucherify, ResponseRedeemVoucher, Integer>> redeemVoucher(RequestRedeemVoucher request) {
		
		return clientVoucherify.redeemVoucher(request)
				.flatMap(response -> 
		            responseVoucherifyMapper.mapRedeem(response, request)
		                .map(tuple -> Tuples.of(response, tuple.getT1(), tuple.getT2()))
				)
	            .onErrorResume(e -> {
	                logger.error("Error en validación de VoucherServiceImpl.redeemVoucher ", e);
	                ResponseRedeemVoucherify response = new ResponseRedeemVoucherify();
	                ResponseRedeemVoucher errorResponse = ResponseError.buildErrorRedeemResponse(
	                        HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal error");
	                return Mono.just(Tuples.of(response, errorResponse,HttpStatus.INTERNAL_SERVER_ERROR.value()));
	            });
	}

	/**
	 * Método que gestiona la llamada al cliente de cupones para el metodo publication 
	 *
	 * @param RequestPublicationVoucher request
	 * @return ResponsePublicationsVoucherify, ResponsePublicationVoucher, Integer
	 * @since 1.0.0
	 */
	@Override
	public Mono<Tuple3<ResponsePublicationsVoucherify, ResponsePublicationVoucher, Integer>> publicationVoucher(RequestPublicationVoucher request) {
		
		 return clientVoucherify.publicationVoucher(request)
					.flatMap(response -> 
			            responseVoucherifyMapper.mapPublications(response, request)
			                .map(tuple -> Tuples.of(response, tuple.getT1(), tuple.getT2()))
			        )
			        .onErrorResume(e -> {
			            logger.error("Error en validación de VoucherServiceImpl.publicationVoucher ", e);
			            ResponsePublicationsVoucherify response = new ResponsePublicationsVoucherify();
			            ResponsePublicationVoucher errorResponse = ResponseError.buildErrorPublicationsResponse(
			                    HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error");
			            return Mono.just(Tuples.of(response, errorResponse,HttpStatus.INTERNAL_SERVER_ERROR.value()));
			        });
	} 
	

}
