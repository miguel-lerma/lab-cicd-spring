/*
 * @(#)VoucherifyResponseHandler.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.femsa.oxxo.voucher.handler.voucherify;

import java.time.OffsetDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.femsa.oxxo.voucher.dto.voucherify.publications.ResponsePublications;
import com.femsa.oxxo.voucher.dto.voucherify.publications.ResponsePublicationsVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.redemptions.ResponseRedeem;
import com.femsa.oxxo.voucher.dto.voucherify.redemptions.ResponseRedeemVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.validations.ResponseValidateVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.validations.ResponseValidations;

import reactor.core.publisher.Mono;

/**
 * Clase que captura errores y exceptions en los request a Voucherify y retorna un obj personalizado
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */
@Component
public class VoucherifyResponseHandler {

	private static final Logger logger = LoggerFactory.getLogger(VoucherifyResponseHandler.class);

	private final ObjectMapper objectMapper;

	public VoucherifyResponseHandler(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	/**
	 * Metodo que captura los errores en httpsstatus 4xx y 5xx y retorna un msj controlado 
	 *
	 * @param ClientResponse response, String errorMessage
	 * @return Mono<ResponseValidations>
	 * @since 1.0.0
	 */
	public Mono<ResponseValidations> responseErrorValidate(ClientResponse response, String errorMessage) {
		return response.bodyToMono(ResponseValidations.class)
				.flatMap(error -> {
					logger.error("{}: [code: {}, key: {}, message: {}]", errorMessage, error.getCode(), error.getKey(),error.getMessage());
		
					ResponseValidations errorResponse = new ResponseValidations();
		
					errorResponse.setValid(false);
					errorResponse.setCode(error.getCode());
					errorResponse.setKey(error.getKey());
					errorResponse.setMessage(error.getMessage());
					
					return Mono.just(errorResponse);
		});
	}

	/**
	 * Metodo que captura errores devueltos en onStatus o alguna exception del flujo
	 *
	 * @param Throwable e, OffsetDateTime startTime, OffsetDateTime endTime, String requestJson
	 * @return Mono<ResponseValidateVoucherify>
	 * @since 1.0.0
	 */
	public Mono<ResponseValidateVoucherify> responseErrorOrExceptionValidate(Throwable e, OffsetDateTime startTime, OffsetDateTime endTime, String requestJson) {

		ResponseValidations fallbackResponse;
		String responseJson = null;

		if (e instanceof VoucherifyValidateException) {
			fallbackResponse = ((VoucherifyValidateException) e).getResponse();
		} else {
			logger.error("Error inesperado validando el cupón: {}", e.getMessage());
			logger.error("Se asigna code 500");
			logger.error("Se asigna message Internal Server Error");

			fallbackResponse = new ResponseValidations();
			
			fallbackResponse.setValid(false);
			fallbackResponse.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			fallbackResponse.setKey("Internal Server Error");
			fallbackResponse.setMessage("Internal Server error");
		}

		try {
			responseJson = objectMapper.writeValueAsString(fallbackResponse);
			logger.info("Body Recibido de Voucherify"+ responseJson);
		} catch (Exception ex) {
			logger.error("Error serializando fallbackResponse", ex);
		}

		return Mono.just(new ResponseValidateVoucherify(fallbackResponse, startTime, endTime, requestJson, responseJson));
	}

	
	/**
	 * Metodo que captura los errores en httpsstatus 4xx y 5xx y retorna un msj controlado
	 *
	 * @param ClientResponse response, String errorMessage
	 * @return Mono<ResponseRedeem>
	 * @since 1.0.0
	 */
	public Mono<ResponseRedeem> responseErrorRedeem(ClientResponse response, String errorMessage) {
		return response.bodyToMono(ResponseRedeem.class)
				.flatMap(error -> {
					logger.error("{}: [code: {}, key: {}, message: {}]", errorMessage, error.getCode(), error.getKey(),error.getMessage());
		
					ResponseRedeem errorResponse = new ResponseRedeem();
		
					errorResponse.setCode(error.getCode());
					errorResponse.setKey(error.getKey());
					errorResponse.setMessage(error.getMessage());
					
					return Mono.just(errorResponse);
				});
	}

	
	/**
	 * Metodo que captura errores devueltos en onStatus o alguna exception del flujo
	 *
	 * @param Throwable e, OffsetDateTime startTime,OffsetDateTime endTime, String requestJson
	 * @return Mono<ResponseRedeemVoucherify>
	 * @since 1.0.0
	 */
	public Mono<ResponseRedeemVoucherify> responseErrorOrExceptionRedeem(Throwable e, OffsetDateTime startTime,OffsetDateTime endTime, String requestJson) {

		ResponseRedeem fallbackResponse;
		String responseJson = null;

		if (e instanceof VoucherifyRedemptionException) {
			fallbackResponse = ((VoucherifyRedemptionException) e).getResponse();
		} else {
			logger.error("Error inesperado al redimir el cupón: {}", e.getMessage());
			logger.error("Se asigna code 500");
			logger.error("Se asigna message Internal Server Error");

			fallbackResponse = new ResponseRedeem();

			fallbackResponse.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			fallbackResponse.setKey("Internal Server Error");
			fallbackResponse.setMessage("Internal Server Error");
		}
		try {
			responseJson = objectMapper.writeValueAsString(fallbackResponse);
			logger.info("Body Recibido de Voucherify"+ responseJson);
		} catch (Exception ex) {
			logger.error("Error serializando fallbackResponse", ex);
		}

		return Mono.just(new ResponseRedeemVoucherify(fallbackResponse, startTime, endTime, requestJson, responseJson));
	}

	//
	
	/**
	 * Metodo que captura los errores en httpStatus 4xx y 5xx y retorna un msj controlado
	 *
	 * @param ClientResponse response, String errorMessage
	 * @return Mono<ResponsePublications>
	 * @since 1.0.0
	 */
	public Mono<ResponsePublications> responseErrorPublication(ClientResponse response, String errorMessage) {
		return response.bodyToMono(ResponsePublications.class)
				.flatMap(error -> {
			
					logger.error("{}: [code: {}, key: {}, message: {}]", errorMessage, error.getCode(), error.getKey(),error.getMessage());
					
					ResponsePublications errorResponse = new ResponsePublications();
		
					errorResponse.setCode(error.getCode());
					errorResponse.setKey(error.getKey());
					errorResponse.setMessage(error.getMessage());
		
					return Mono.just(errorResponse);
				});
	}
	
	/**
	 * Metodo que captura errores devueltos en onStatus o alguna exception del flujo
	 *
	 * @param Throwable e, OffsetDateTime startTime, OffsetDateTime endTime, String requestJson
	 * @return Mono<ResponsePublicationsVoucherify>
	 * @since 1.0.0
	 */
	public Mono<ResponsePublicationsVoucherify> responseErrorOrExceptionPublication(Throwable e, OffsetDateTime startTime, OffsetDateTime endTime, String requestJson) {

		ResponsePublications fallbackResponse;
		String responseJson = null;

		if (e instanceof VoucherifyPublicationException) {
			fallbackResponse = ((VoucherifyPublicationException) e).getResponse();
		} else {
			logger.error("Error inesperado al publicar el cupón: {}", e.getMessage());
			logger.error("Se asigna code 500");
			logger.error("Se asigna message Internal Server Error");

			fallbackResponse = new ResponsePublications();

			fallbackResponse.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			fallbackResponse.setKey("Internal Server Error");
			fallbackResponse.setMessage("Internal Server Error");
		}

		try {
			responseJson = objectMapper.writeValueAsString(fallbackResponse);
			logger.info("Body Recibido de Voucherify"+ responseJson);
		} catch (Exception ex) {
			logger.error("Error serializando fallbackResponse", ex);
		}

		return Mono.just(new ResponsePublicationsVoucherify(fallbackResponse, startTime, endTime, requestJson, responseJson));
	}
	
}
