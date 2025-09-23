/*
 * @(#)ClientVoucherifyImpl.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.femsa.oxxo.voucher.client.voucher.impl;

import java.time.OffsetDateTime;
import java.time.ZoneId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.femsa.oxxo.voucher.client.voucher.IClientVoucher;
import com.femsa.oxxo.voucher.config.VoucherifyProperties;
import com.femsa.oxxo.voucher.dto.RequestPublicationVoucher;
import com.femsa.oxxo.voucher.dto.RequestRedeemVoucher;
import com.femsa.oxxo.voucher.dto.RequestValidateVoucher;
import com.femsa.oxxo.voucher.handler.voucherify.VoucherifyPublicationException;
import com.femsa.oxxo.voucher.handler.voucherify.VoucherifyRedemptionException;
import com.femsa.oxxo.voucher.handler.voucherify.VoucherifyResponseHandler;
import com.femsa.oxxo.voucher.handler.voucherify.VoucherifyValidateException;
import com.femsa.oxxo.voucher.mapper.voucherify.RequestVoucherifyMapper;
import com.femsa.oxxo.voucher.utils.LogSanitizer;
import com.femsa.oxxo.voucher.dto.voucherify.publications.RequestPublicationsVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.publications.ResponsePublications;
import com.femsa.oxxo.voucher.dto.voucherify.publications.ResponsePublicationsVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.redemptions.RequestRedeemVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.redemptions.ResponseRedeem;
import com.femsa.oxxo.voucher.dto.voucherify.redemptions.ResponseRedeemVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.validations.RequestValidationsVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.validations.ResponseValidateVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.validations.ResponseValidations;

import reactor.core.publisher.Mono;

/**
 * Clase que implementa los metodos para el cliente Voucherify.
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */
@Component("ClientVoucherifyImpl")
public class ClientVoucherifyImpl implements IClientVoucher {

	private static final Logger logger = LoggerFactory.getLogger(ClientVoucherifyImpl.class);

	private final WebClient webClient;
	private final VoucherifyResponseHandler responseHandler;
	private final ObjectMapper objectMapper;
	private final RequestVoucherifyMapper voucherifyMapper;
	private final VoucherifyProperties properties;
	
	public ClientVoucherifyImpl(WebClient.Builder webClientBuilder, VoucherifyResponseHandler responseHandler, ObjectMapper objectMapper, RequestVoucherifyMapper voucherifyMapper, VoucherifyProperties properties) {
		this.webClient = webClientBuilder.baseUrl(properties.getUrl()).build();
		this.responseHandler = responseHandler;
		this.objectMapper = objectMapper;
		this.voucherifyMapper = voucherifyMapper;
		this.properties = properties;
	}

	/**
	 * Método para validar si un cupon es valido o no 
	 *
	 * @param RequestValidateVoucher request
	 * @return Mono<ResponseValidateVoucherify>
	 * @since 1.0.0
	 */
	@Override
	public Mono<ResponseValidateVoucherify> validateVoucher(RequestValidateVoucher request) {
		
		RequestValidationsVoucherify body = voucherifyMapper.mapRequestValidate(request);

		String requestJson = null;
		try {
			requestJson = objectMapper.writeValueAsString(body);
		    logger.info("Body enviado a Voucherify en metodo Validate:{}", LogSanitizer.sanitize(requestJson));
		} catch (Exception e) {
			logger.error("Error serializando body", e);
		} 

		final String finalRequestJson = requestJson;
		
		//Se obtiene el DateTime del inicio del request a Voucherify
		OffsetDateTime startTime = OffsetDateTime.now(ZoneId.of("America/Mexico_City"));
		
		return webClient
				.post()
				.uri(properties.getUrlEndpoint().getValidate())
				.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.header("X-App-Id", properties.getApp().getId())
				.header("X-App-Token", properties.getApp().getToken())
				.header("X-Voucherify-Channel", properties.getChannel())
				.bodyValue(body)
				.retrieve()
				.onStatus( // Se capturan errores 400 y 500 
						status -> status.is4xxClientError() || status.is5xxServerError(),
						response -> responseHandler.responseErrorValidate(response,"Error " + response.statusCode().value() + " en la llamada a Voucherify")
								.flatMap(errorResponse -> Mono.error(new VoucherifyValidateException(errorResponse))))
				.bodyToMono(ResponseValidations.class)
				.flatMap(response -> {
					//Se capturan las respuestas 200
					//En este metodo, Voucherify retorna en estatus 200 algunos errores que deben retornarse en un estatus 4xx
					//Se captura el DateTime de la respuesta de Voucherify
					//Se serialian los request y response de Voucherify
			        OffsetDateTime endTime =  OffsetDateTime.now(ZoneId.of("America/Mexico_City"));

			        String responseJson = null;
					try {
						responseJson = objectMapper.writeValueAsString(response);
						logger.info("Body recibido de Voucherify en metodo Validate:{}", responseJson);
					} catch (Exception e) {
						logger.error("Error serializando body", e);
					}
					return Mono.just(new ResponseValidateVoucherify(response, startTime, endTime, finalRequestJson, responseJson));
			    })
				.onErrorResume(e -> {
					//Se captura el DateTime de la respuesta de Voucherify cuando se recibio un error en el request
					//Se ejecuta cuando:
					//Ocurre cualquier error en el flujo anterior
					//Si en onStatus detectamos un error
					//Falló la deserialización bodyToMono
					//Falló el procesamiento dentro del flatMap
				    OffsetDateTime endTime = OffsetDateTime.now(ZoneId.of("America/Mexico_City"));
				    return responseHandler.responseErrorOrExceptionValidate(e, startTime, endTime, finalRequestJson);
				});
	}

	/**
	 * Método para redimir un cupon 
	 *
	 * @param RequestRedeemVoucher request
	 * @return Mono<ResponseRedeemVoucherify>
	 * @since 1.0.0
	 */
	@Override
	public Mono<ResponseRedeemVoucherify> redeemVoucher(RequestRedeemVoucher request) {
		
		RequestRedeemVoucherify body = voucherifyMapper.mapRequestRedeem(request);

		String requestJson = null;
		
		try {
			requestJson = objectMapper.writeValueAsString(body);
			logger.info("Body enviado a Voucherify en metodo Redeem:{}", LogSanitizer.sanitize(requestJson));
		} catch (Exception e) {
			logger.error("Error serializando body", e);
		} 

		final String finalRequestJson = requestJson;
		
		//Se obtiene el DateTime del inicio del request a Voucherify
		OffsetDateTime startTime = OffsetDateTime.now(ZoneId.of("America/Mexico_City"));
		
		return webClient
				.post()
				.uri(properties.getUrlEndpoint().getRedeem())
				.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.header("X-App-Id", properties.getApp().getId())
				.header("X-App-Token", properties.getApp().getToken())
				.header("X-Voucherify-Channel", properties.getChannel())
				.bodyValue(body)
				.retrieve()
				.onStatus(// Se capturan errores 400 y 500 
						status -> status.is4xxClientError() || status.is5xxServerError(),
						response -> responseHandler.responseErrorRedeem(response,"Error " + response.statusCode().value() + " en la llamada a Voucherify")
								.flatMap(errorResponse -> Mono.error(new VoucherifyRedemptionException(errorResponse))))
				.bodyToMono(ResponseRedeem.class)
				.flatMap(response -> {
					//Se procesan los estatus 200
					//Se captura el DateTime de la respuesta de Voucherify
					//Se serialian los request y response de Voucherify
			        OffsetDateTime endTime =  OffsetDateTime.now(ZoneId.of("America/Mexico_City"));
			        
			        String responseJson = null;
					try {
						responseJson = objectMapper.writeValueAsString(response);
						logger.info("Body recibido de Voucherify en metodo Redeem:{}", responseJson);
					} catch (Exception e) {
						logger.error("Error serializando body", e);
					}
			        return Mono.just(new ResponseRedeemVoucherify(response, startTime, endTime, finalRequestJson, responseJson));
			    })
				.onErrorResume(e -> {
					//Se captura el DateTime de la respuesta de Voucherify cuando se recibio un error en el request
					//Se ejecuta cuando:
					//Ocurre cualquier error en el flujo anterior
					//Si en onStatus detectamos un error
					//Falló la deserialización bodyToMono
					//Falló el procesamiento dentro del flatMap
				    OffsetDateTime endTime = OffsetDateTime.now(ZoneId.of("America/Mexico_City"));
				    return responseHandler.responseErrorOrExceptionRedeem(e, startTime, endTime, finalRequestJson);
				});
	}

	/**
	 * Método para activar (publicar) un cupon asociado a un memberId
	 *
	 * @param RequestRedeemVoucher request
	 * @return Mono<ResponseRedeemVoucherify>
	 * @since 1.0.0
	 */
	@Override
	public Mono<ResponsePublicationsVoucherify> publicationVoucher(RequestPublicationVoucher request) {
		
		RequestPublicationsVoucherify body = voucherifyMapper.mapRequestPublication(request);

		String requestJson = null;
		
		try {
			requestJson = objectMapper.writeValueAsString(body);
			logger.info("Body enviado a Voucherify en metodo Publications:{}", LogSanitizer.sanitize(requestJson));
		} catch (Exception e) {
			logger.error("Error serializando body", e);
		} 

		final String finalRequestJson = requestJson;
		
		//Se obtiene el DateTime del inicio del request a Voucherify
		OffsetDateTime startTime = OffsetDateTime.now(ZoneId.of("America/Mexico_City"));
		
		return webClient
				.post()
				.uri(properties.getUrlEndpoint().getPublication())
				.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.header("X-App-Id", properties.getApp().getId())
				.header("X-App-Token", properties.getApp().getToken())
				.header("X-Voucherify-Channel", properties.getChannel())
				.bodyValue(body)
				.retrieve()
				.onStatus(//Se capturan errores 400 y 500
						status -> status.is4xxClientError() || status.is5xxServerError(),
						response -> responseHandler
								.responseErrorPublication(response,"Error " + response.statusCode().value() + " en la llamada a Voucherify")
								.flatMap(errorResponse -> Mono.error(new VoucherifyPublicationException(errorResponse))))
				.bodyToMono(ResponsePublications.class)
				.flatMap(response -> {
					//Se procesan los estatus 200
					//Se captura el DateTime de la respuesta de Voucherify
					//Se serialian los request y response de Voucherify
			        OffsetDateTime endTime =  OffsetDateTime.now(ZoneId.of("America/Mexico_City"));
			        
			        String responseJson = null;
					try {
						responseJson = objectMapper.writeValueAsString(response);
						logger.info("Body recibido de Voucherify en metodo Publications:{}", responseJson);
					} catch (Exception e) {
						logger.error("Error serializando body", e);
					}
			        return Mono.just(new ResponsePublicationsVoucherify(response, startTime, endTime, finalRequestJson, responseJson));
			    })
				.onErrorResume(e -> {
					//Se captura el DateTime de la respuesta de Voucherify cuando se recibio un error en el request
					//Se ejecuta cuando:
					//Ocurre cualquier error en el flujo anterior
					//Si en onStatus detectamos un error
					//Falló la deserialización bodyToMono
					//Falló el procesamiento dentro del flatMap
				    OffsetDateTime endTime = OffsetDateTime.now(ZoneId.of("America/Mexico_City"));
				    return responseHandler.responseErrorOrExceptionPublication(e, startTime, endTime, finalRequestJson);
				});
	}
	
}
