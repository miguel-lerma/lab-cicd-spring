/*
 * @(#)VoucherController.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.femsa.oxxo.voucher.controller;

import java.time.OffsetDateTime;
import java.time.ZoneId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.femsa.oxxo.voucher.entity.MsLog;
import com.femsa.oxxo.voucher.entity.MsTransaction;
import com.femsa.oxxo.voucher.mapper.voucherify.TransactionLogMapper;
import com.femsa.oxxo.voucher.service.ITransactionLogService;
import com.femsa.oxxo.voucher.service.IVoucherService;
import com.femsa.oxxo.voucher.utils.LogSanitizer;

import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

/**
 * Controlador REST para operaciones relacionadas con cupones.
 *
 * Este controlador expone los endpoints para:
 *   Validar un cupón ({@code /validate})
 *   Redimir un cupón ({@code /redeem})
 *   Publicar cupones para una campaña ({@code /publication})
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 */
@RestController
@RequestMapping("/voucher")
public class VoucherController {

	private static final Logger logger = LoggerFactory.getLogger(VoucherController.class);

	private final IVoucherService voucherService;

	private final ITransactionLogService transactionService;

	private final TransactionLogMapper transactionLogMapper;
	
	private final ObjectMapper objectMapper;

	@Autowired
	public VoucherController(IVoucherService voucherService, ITransactionLogService transactionService,
			TransactionLogMapper transactionLogMapper, ObjectMapper objectMapper) {
		this.voucherService = voucherService;
		this.transactionService = transactionService;
		this.transactionLogMapper = transactionLogMapper;
		this.objectMapper = objectMapper;
	}

	/**
	 * Valida un cupón.
	 *
	 * Este endpoint recibe una solicitud de validación de cupón, invoca el servicio de validación
	 * y construye la respuesta correspondiente. Además, registra la transacción en base de datos y,
	 * en caso de error de validación, registra también un log de error.
	 *
	 * @param request Objeto con los datos necesarios para validar el cupón.
	 * @return Mono con ResponseEntity que contiene la respuesta del proceso de validación
	 */
	@PostMapping("/validate")
	public Mono<ResponseEntity<ResponseValidateVoucher>> validateVoucher(@RequestBody @Valid RequestValidateVoucher request) {

		logger.info("Inicia VoucherController.validateVoucher() --> Coupon: [{}] TransactionId: [{}]",LogSanitizer.sanitize(request.getCoupon()), LogSanitizer.sanitize(request.getTransactionId()));
		
		try {
			String rawJson = objectMapper.writeValueAsString(request);
		    String sanitizedJson = LogSanitizer.sanitize(rawJson);
		    logger.info("Request completo recibido: {}", sanitizedJson);
		} catch (Exception e) {
			logger.warn("No se pudo serializar el request a JSON", e);
		}
		
		ZoneId zonaCDMX = ZoneId.of("America/Mexico_City");
    	OffsetDateTime transactionDateRequest = OffsetDateTime.now(zonaCDMX);

		return voucherService.validateVoucher(request)
				.flatMap(tuple -> {

					ResponseValidateVoucherify responseVoucherify = tuple.getT1();
					ResponseValidateVoucher responseBody = tuple.getT2();
					
					int httpStatus = tuple.getT3();
					
					logger.info("Código HTTP a devolver: {}", httpStatus);

					// Convertir el request en transacción
					MsTransaction transaction = TransactionLogMapper.toTransactionValidate(request, responseBody, httpStatus, transactionDateRequest, responseVoucherify);
					

					if (responseBody.getValid()) {
						transactionService.saveTransaction(transaction).subscribe();
					}else {
						Mono<MsLog> errorLog = transactionLogMapper.toLogError(request, responseVoucherify);
						transactionService.saveTransactionAndLog(transaction, errorLog).subscribe();
					}
					logger.info("Finaliza VoucherController.validateVoucher() --> Coupon: [{}] TransactionId: [{}]", LogSanitizer.sanitize(request.getCoupon()), LogSanitizer.sanitize(request.getTransactionId()));
					return Mono.just(ResponseEntity.status(httpStatus).body(responseBody));
				}).switchIfEmpty(Mono.defer(() -> {
			        logger.info("Finaliza VoucherController.validateVoucher() sin resultado --> Coupon: [{}] TransactionId: [{}]", LogSanitizer.sanitize(request.getCoupon()), LogSanitizer.sanitize(request.getTransactionId()));
			        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND.value()).build());
			    }))
			    .onErrorResume(e -> {
					logger.error("Error en VoucherController.validateVoucher ", e);
					logger.info("Finaliza VoucherController.validateVoucher() --> Coupon: [{}] TransactionId: [{}]", LogSanitizer.sanitize(request.getCoupon()), LogSanitizer.sanitize(request.getTransactionId()));
					ResponseValidateVoucher response = ResponseError
							.buildErrorValidateResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error");
					return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).body(response));
			    });
	}
	
	/**
	 * Redime un cupón.
	 *
	 * Este endpoint recibe una solicitud para redimir un cupón, llama al servicio de redención
	 * y genera la respuesta con el resultado. También persiste la transacción, y si la redención falla,
	 * guarda un log de error en base de datos.
	 *
	 * @param request Objeto con los datos necesarios para redimir el cupón.
	 * @return Mono con ResponseEntity que contiene la respuesta del proceso de redención,
	 *         incluyendo el código HTTP correspondiente.
	 */
	@PostMapping("/redeem")
	public Mono<ResponseEntity<ResponseRedeemVoucher>> redeemVoucher(@RequestBody @Valid RequestRedeemVoucher request) {

		logger.info("Inicia VoucherController.redeemVoucher() --> Coupon: [{}] TransactionId: [{}]", LogSanitizer.sanitize(request.getCoupon()), LogSanitizer.sanitize(request.getTransactionId()));

		try {
			String rawJson = objectMapper.writeValueAsString(request);
		    String sanitizedJson = LogSanitizer.sanitize(rawJson);
		    logger.info("Request completo recibido: {}", sanitizedJson);
		} catch (Exception e) {
			logger.warn("No se pudo serializar el request a JSON", e);
		}
		
		ZoneId zonaCDMX = ZoneId.of("America/Mexico_City");
    	OffsetDateTime transactionDateRequest = OffsetDateTime.now(zonaCDMX);

		return voucherService.redeemVoucher(request)
				.flatMap(tuple -> {

					ResponseRedeemVoucherify responseVoucherify = tuple.getT1();
					ResponseRedeemVoucher responseBody = tuple.getT2();
		
					int httpStatus = tuple.getT3();
		            
		        	logger.info("Código HTTP a devolver: {}", httpStatus);
		
					// Convertir el request en transacción
					MsTransaction transaction = TransactionLogMapper.toTransactionRedeem(request, responseBody, httpStatus, transactionDateRequest, responseVoucherify);
					
					if (responseBody.getValid()) {
						transactionService.saveTransaction(transaction).subscribe();
					}else {
						Mono<MsLog> errorLog = transactionLogMapper.toLogErrorRedeem(request, responseVoucherify);
						transactionService.saveTransactionAndLog(transaction, errorLog).subscribe();
					}
					logger.info("Finaliza VoucherController.redeemVoucher() --> Coupon: [{}] TransactionId: [{}]", LogSanitizer.sanitize(request.getCoupon()), LogSanitizer.sanitize(request.getTransactionId()));
					return Mono.just(ResponseEntity.status(httpStatus).body(responseBody));
				})
				.switchIfEmpty(Mono.defer(() -> {
					logger.info("Finaliza VoucherController.redeemVoucher() sin resultados --> Coupon: [{}] TransactionId: [{}]", LogSanitizer.sanitize(request.getCoupon()), LogSanitizer.sanitize(request.getTransactionId()));
			        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
			    }))
				.onErrorResume(e -> {
					logger.error("Error en VoucherController.redeemVoucher ", e);
					logger.info("Finaliza VoucherController.redeemVoucher() --> Coupon: [{}] TransactionId: [{}]", LogSanitizer.sanitize(request.getCoupon()), LogSanitizer.sanitize(request.getTransactionId()));
					ResponseRedeemVoucher response = ResponseError
							.buildErrorRedeemResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error");
					return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
				});

	}
	
	/**
	 * Genera uno o más cupones asociados a un memberId.
	 *
	 * Este endpoint recibe una solicitud para publicar (asignar) cupones a un miembro en una campaña.
	 * Invoca el servicio correspondiente, construye y retorna la respuesta.
	 * También persiste la transacción y, si ocurre un error, registra un log de error en base de datos.
	 *
	 * @param request Objeto con los datos necesarios para publicar los cupones.
	 * @return Mono con ResponseEntity que contiene la respuesta del proceso de publicación,
	 *         incluyendo el código HTTP correspondiente.
	 */
	@PostMapping("/publication")
	public Mono<ResponseEntity<ResponsePublicationVoucher>> publicationVoucher(@RequestBody @Valid RequestPublicationVoucher request) {

		logger.info("Inicia VoucherController.publicationVoucher() --> campaign: [{}] memberId: [{}]", LogSanitizer.sanitize(request.getCampaign()), LogSanitizer.sanitize(request.getMemberId())) ;

		try {
			String rawJson = objectMapper.writeValueAsString(request);
		    String sanitizedJson = LogSanitizer.sanitize(rawJson);
		    logger.info("Request completo recibido: {}", sanitizedJson);
		} catch (Exception e) {
			logger.warn("No se pudo serializar el request a JSON", e);
		}
		
		request.validCount();
		
		ZoneId zonaCDMX = ZoneId.of("America/Mexico_City");
    	OffsetDateTime transactionDateRequest = OffsetDateTime.now(zonaCDMX);

		return voucherService.publicationVoucher(request)
				.flatMap(tuple -> {
			
					ResponsePublicationsVoucherify responseVoucherify = tuple.getT1();
					ResponsePublicationVoucher responseBody = tuple.getT2();
		
					int httpStatus = tuple.getT3();
			          
		        	logger.info("Código HTTP a devolver: {}", httpStatus);
		
					// Convertir el request en transacción
					MsTransaction transaction = TransactionLogMapper.toTransactionPublication(request, responseBody, httpStatus, transactionDateRequest, responseVoucherify);
		
					if (responseBody.getValid()) {
						transactionService.saveTransaction(transaction).subscribe();
					}else {
						Mono<MsLog> errorLog = transactionLogMapper.toLogErrorPublications(request, responseVoucherify);
						transactionService.saveTransactionAndLog(transaction, errorLog).subscribe();
					}
					//
					logger.info("Finaliza VoucherController.publicationVoucher() --> campaign: [{}] memberId: [{}]", LogSanitizer.sanitize(request.getCampaign()), LogSanitizer.sanitize(request.getMemberId()));
					return Mono.just(ResponseEntity.status(httpStatus).body(responseBody));
				})
				.switchIfEmpty(Mono.defer(() -> {
					logger.info("Finaliza VoucherController.publicationVoucher() --> campaign: [{}] memberId: [{}]", LogSanitizer.sanitize(request.getCampaign()), LogSanitizer.sanitize(request.getMemberId()));
			        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
			    }))
				.onErrorResume(e -> {
					logger.info("Finaliza VoucherController.publicationVoucher() --> campaign: [{}] memberId: [{}]", LogSanitizer.sanitize(request.getCampaign()), LogSanitizer.sanitize(request.getMemberId()));
					logger.error("Error en VoucherController.publicationVoucher ", e);
		
					ResponsePublicationVoucher response = ResponseError
							.buildErrorPublicationsResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error");
					return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).body(response));
				});
	}

}
