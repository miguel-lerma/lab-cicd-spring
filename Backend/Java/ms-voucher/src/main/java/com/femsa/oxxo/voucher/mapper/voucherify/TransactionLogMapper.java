/*
 * @(#)TransactionLogMapper.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.femsa.oxxo.voucher.mapper.voucherify;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.femsa.oxxo.voucher.dto.RequestPublicationVoucher;
import com.femsa.oxxo.voucher.dto.RequestRedeemVoucher;
import com.femsa.oxxo.voucher.dto.RequestValidateVoucher;
import com.femsa.oxxo.voucher.dto.ResponsePublicationVoucher;
import com.femsa.oxxo.voucher.dto.ResponseRedeemVoucher;
import com.femsa.oxxo.voucher.dto.ResponseValidateVoucher;
import com.femsa.oxxo.voucher.dto.voucherify.publications.ResponsePublicationsVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.redemptions.ResponseRedeemVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.validations.ResponseValidateVoucherify;
import com.femsa.oxxo.voucher.entity.MsLog;
import com.femsa.oxxo.voucher.entity.MsTransaction;
import com.femsa.oxxo.voucher.repository.MsRespCodeRepository;

import reactor.core.publisher.Mono;

/**
 * Clase que mapea objetos para guardar logs
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */
@Component
public class TransactionLogMapper {

	private static final Logger logger = LoggerFactory.getLogger(TransactionLogMapper.class);

	private final static ObjectMapper objectMapper = new ObjectMapper();

	private final MsRespCodeRepository repoMsCode;
	
	private static final String GENERIC = "generic"; 
	
	private static final String VALIDATE = "validate"; 
	
	private static final String REDEMPTION = "redemption";
	
	private static final String PUBLICATION = "publication";

	public TransactionLogMapper(MsRespCodeRepository repoMsCode) {
		this.repoMsCode = repoMsCode;
	}

	/**
	 * Método para mapear la entidad MsTransaction para el endpoint validate 
	 *
	 * @param RequestValidateVoucher request, ResponseValidateVoucher response,
			int httpStatus, OffsetDateTime transactionDateRequest, ResponseValidateVoucherify responseVoucherify
	 * @return MsTransaction
	 * @since 1.0.0
	 */
	public static MsTransaction toTransactionValidate(RequestValidateVoucher request, ResponseValidateVoucher response,
			int httpStatus, OffsetDateTime transactionDateRequest, ResponseValidateVoucherify responseVoucherify) {

		ZoneId zonaCDMX = ZoneId.of("America/Mexico_City");
		OffsetDateTime transactionDateResponse = OffsetDateTime.now(zonaCDMX);
		
		LocalDate transactionDatePos;
		
		if (request.getCashdate() == null) {
			logger.warn("No se recibio la propiedad Cashdate, se asigna fecha actual a transaction_date_channel");
			transactionDatePos = LocalDate.now();
		}else {
			String transactionDatePosStr = request.getCashdate();
			DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("yyyyMMdd");
			transactionDatePos = LocalDate.parse(transactionDatePosStr, formatterDate);
		}
		
		LocalTime transactionTime;
		
		if (request.getCashhour() == null) {
			logger.warn("No se recibio la propiedad Cashhour, se asigna hora actual a transaction_time_channel");
			transactionTime = LocalTime.now();
		}else {
			String horaStr = request.getCashhour();
			DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("HH:mm:ss");
			String formattedHoraStr = horaStr.substring(0, 2) + ":" + horaStr.substring(2, 4) + ":" + horaStr.substring(4);
			transactionTime = LocalTime.parse(formattedHoraStr, formatterTime);
		}
		
		String strRequest = null;
		String strResponse = null;

		try {
			strRequest = objectMapper.writeValueAsString(request);
			strResponse = objectMapper.writeValueAsString(response);
		} catch (Exception e) {
			logger.error("Error serializando body", e);
		}

		return MsTransaction.builder()
				.place(request.getConsumerData().getCrPlace())
				.store(request.getConsumerData().getCrStore())
				.cash(request.getConsumerData().getCashRegister())
				.transactionDateChannel(transactionDatePos)
				.transactionTimeChannel(transactionTime)
				.code(response.getResult() != null ? response.getResult().getError().getCode() : null)
				.message(response.getResult() != null ? response.getResult().getError().getMessage() : null)
				.dataRequest(strRequest)
				.dataResponse(strResponse)
				.application(request.getConsumerData().getApplication())
				.entity(request.getConsumerData().getEntity())
				.source(request.getConsumerData().getOrigin())
				.operation(request.getOperation())
				.datetimeRequest(responseVoucherify.getStartTime())
				.datetimeResponse(responseVoucherify.getEndTime())
				.coupon(request.getCoupon())
				.noTicket(String.valueOf(request.getTicket()))
				.idTicket(request.getTransactionId())
				.requestServer(request.getServer())
				.operator(request.getCashier())
				.httpCode(httpStatus)
				.transactionDateRequest(transactionDateRequest)
				.transactionDateResponse(transactionDateResponse)
				.dataRequestProvider(responseVoucherify.getRequestJson())
				.dataResponseProvider(responseVoucherify.getResponseJson())
				.build();
	}

	/**
	 * Método para mapear la entidad MsTransaction para el endpoint redeem 
	 *
	 * @param RequestValidateVoucher request, ResponseValidateVoucher response,
			int httpStatus, OffsetDateTime transactionDateRequest, ResponseValidateVoucherify responseVoucherify
	 * @return MsTransaction
	 * @since 1.0.0
	 */
	public static MsTransaction toTransactionRedeem(RequestRedeemVoucher request, ResponseRedeemVoucher response,
			int httpStatus, OffsetDateTime transactionDateRequest, ResponseRedeemVoucherify responseVoucherify) {
		
		LocalDate transactionDatePos;
		
		if (request.getCashdate() == null) {
			logger.warn("No se recibio la propiedad Cashdate, se asigna fecha actual a transaction_date_channel");
			transactionDatePos = LocalDate.now();
		}else {
			String transactionDatePosStr = request.getCashdate();
			DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("yyyyMMdd");
			transactionDatePos = LocalDate.parse(transactionDatePosStr, formatterDate);
		}
		
		LocalTime transactionTime;
		
		if (request.getCashhour() == null) {
			logger.warn("No se recibio la propiedad Cashhour, se asigna hora actual a transaction_time_channel");
			transactionTime = LocalTime.now();
		}else {
			String horaStr = request.getCashhour();
			DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("HH:mm:ss");
			String formattedHoraStr = horaStr.substring(0, 2) + ":" + horaStr.substring(2, 4) + ":" + horaStr.substring(4);
			transactionTime = LocalTime.parse(formattedHoraStr, formatterTime);
		}

		String strRequest = null;
		String strResponse = null;

		ZoneId zonaCDMX = ZoneId.of("America/Mexico_City");
		OffsetDateTime transactionDateResponse = OffsetDateTime.now(zonaCDMX);

		try {
			strRequest = objectMapper.writeValueAsString(request);
			strResponse = objectMapper.writeValueAsString(response);
		} catch (Exception e) {
			logger.error("Error serializando body", e);
		}

		return MsTransaction.builder()
				.place(request.getConsumerData().getCrPlace())
				.store(request.getConsumerData().getCrStore())
				.cash(request.getConsumerData().getCashRegister())
				.transactionDateChannel(transactionDatePos)
				.transactionTimeChannel(transactionTime)
				.code(response.getResult() != null ? response.getResult().getError().getCode() : null)
				.message(response.getResult() != null ? response.getResult().getError().getMessage() : null)
				.dataRequest(strRequest)
				.dataResponse(strResponse)
				.application(request.getConsumerData().getApplication())
				.entity(request.getConsumerData().getEntity())
				.source(request.getConsumerData().getOrigin())
				.operation(request.getOperation())
				.datetimeRequest(responseVoucherify.getStartTime())
				.datetimeResponse(responseVoucherify.getEndTime())
				.coupon(request.getCoupon())
				.noTicket(String.valueOf(request.getTicket()))
				.idTicket(request.getTransactionId())
				.requestServer(request.getServer())
				.operator(request.getCashier())
				.httpCode(httpStatus)
				.transactionDateRequest(transactionDateRequest)
				.transactionDateResponse(transactionDateResponse)
				.dataRequestProvider(responseVoucherify.getRequestJson())
				.dataResponseProvider(responseVoucherify.getResponseJson())
				.build();
	}

	/**
	 * Método para mapear la entidad MsTransaction para el endpoint publication 
	 *
	 * @param RequestPublicationVoucher request,
			ResponsePublicationVoucher response, int httpStatus, OffsetDateTime transactionDateRequest,
			ResponsePublicationsVoucherify responseVoucherify
	 * @return MsTransaction
	 * @since 1.0.0
	 */
	public static MsTransaction toTransactionPublication(RequestPublicationVoucher request,
			ResponsePublicationVoucher response, int httpStatus, OffsetDateTime transactionDateRequest,
			ResponsePublicationsVoucherify responseVoucherify) {
		
		LocalDate transactionDatePos;
		
		if (request.getDate() == null) {
			logger.warn("No se recibio la propiedad date, se asigna fecha actual a transaction_date_channel");
			transactionDatePos = LocalDate.now();
		}else {
			String transactionDatePosStr = request.getDate();
			DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("yyyyMMdd");
			transactionDatePos = LocalDate.parse(transactionDatePosStr, formatterDate);
		}
		
		LocalTime transactionTime;
		
		if (request.getHour() == null) {
			logger.warn("No se recibio la propiedad hour, se asigna hora actual a transaction_time_channel");
			transactionTime = LocalTime.now();
		}else {
			String horaStr = request.getHour();
			DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("HH:mm:ss");
			String formattedHoraStr = horaStr.substring(0, 2) + ":" + horaStr.substring(2, 4) + ":" + horaStr.substring(4);
			transactionTime = LocalTime.parse(formattedHoraStr, formatterTime);
		}

		String strRequest = null;
		String strResponse = null;

		ZoneId zonaCDMX = ZoneId.of("America/Mexico_City");
		OffsetDateTime transactionDateResponse = OffsetDateTime.now(zonaCDMX);

		try {
			strRequest = objectMapper.writeValueAsString(request);
			strResponse = objectMapper.writeValueAsString(response);
		} catch (Exception e) {
			logger.error("Error serializando body", e);
		}

		return MsTransaction.builder()
				.place(request.getConsumerData() != null ? request.getConsumerData().getCrPlace() : null)
				.store(request.getConsumerData() != null ? request.getConsumerData().getCrStore() : null)
				.cash(request.getConsumerData() != null ? request.getConsumerData().getCashRegister(): null)
				.transactionDateChannel(transactionDatePos)
				.transactionTimeChannel(transactionTime)
				.code(response.getResult() != null ? response.getResult().getError().getCode() : null)
				.message(response.getResult() != null ? response.getResult().getError().getMessage() : null)
				.dataRequest(strRequest)
				.dataResponse(strResponse)
				.application(request.getConsumerData().getApplication())
				.entity(request.getConsumerData().getEntity())
				.source(request.getConsumerData().getOrigin())
				.operation(request.getOperation())
				.datetimeRequest(responseVoucherify.getStartTime())
				.datetimeResponse(responseVoucherify.getEndTime())
				.operator(request.getUser()).httpCode(httpStatus)
				.transactionDateRequest(transactionDateRequest)
				.transactionDateResponse(transactionDateResponse)
				.dataRequestProvider(responseVoucherify.getRequestJson())
				.dataResponseProvider(responseVoucherify.getResponseJson())
				.build();
	}

	/**
	 * Método para mapear la entidad MsLog para el endpoint validate 
	 *
	 * @param RequestValidateVoucher request, ResponseValidateVoucherify responseVoucherify
	 * @return MsLog
	 * @since 1.0.0
	 */
	public Mono<MsLog> toLogError(RequestValidateVoucher request, ResponseValidateVoucherify responseVoucherify) {

		// identificar cuando es error que viene en http 200 y cuando viene en 4xx, 5xx
		String key = null; 

		
		//Aqui se capturan los 2xx	
		if (responseVoucherify.getResponse().getRedeemables() != null){
			key = responseVoucherify.getResponse().getRedeemables().get(0).getResult().getError().getKey();  
		}
		//Aqui se capturan los 4xx y 5xx con elemento key
		else if (responseVoucherify.getResponse().getKey() != null) {
			key = responseVoucherify.getResponse().getKey();
		}  
		
		String KeyGeneric = key;
		
		if (KeyGeneric == null) {
			KeyGeneric = (responseVoucherify.getResponse().getMessage() != null) ? responseVoucherify.getResponse().getMessage() : "" ;
		}
		
		final String finalKey = KeyGeneric;
		
		return this.repoMsCode.findByMessageKeyIgnoreCase(key, VALIDATE)
				.timeout(Duration.ofSeconds(3))
				.flatMap(msCode -> {
					return Mono.just(MsLog.builder()
					.errorDate(LocalDateTime.now())
					.severity(msCode.getSeverity())
					.errorType(String.valueOf(msCode.getCodeHttpMs()))
					.errorCode(msCode.getCodeMs().intValue())
					.description(msCode.getMessageKey())
					.message(msCode.getMessagePos())
					.action(msCode.getAction())
					.place(request.getConsumerData().getCrPlace())
					.store(request.getConsumerData().getCrStore())
					.cash(request.getConsumerData().getCashRegister())
					.operation(request.getOperation())
					.idTransaction(1L).build());
				}).switchIfEmpty(
						// Si no se encuentra el registro, hacer una consulta alternativa y
						// transformarlo a MsLog
						repoMsCode.findByMessageKeyIgnoreCase(GENERIC, VALIDATE)
							.timeout(Duration.ofSeconds(3))
							.map(msCodeG -> MsLog.builder()
								.errorDate(LocalDateTime.now())
								.severity(msCodeG.getSeverity())
								.errorType(String.valueOf(msCodeG.getCodeHttpMs()))
								.errorCode(msCodeG.getCodeMs().intValue())
								.description(finalKey)
								.message(msCodeG.getMessagePos())
								.action(msCodeG.getAction())
								.place(request.getConsumerData().getCrPlace())
								.store(request.getConsumerData().getCrStore())
								.cash(request.getConsumerData().getCashRegister())
								.operation(request.getOperation())
								.idTransaction(1L)
								.build())
				)
				.switchIfEmpty(Mono.defer(() -> {
				    logger.warn("MsRespCode no encontrado con key={} ni con clave GENÉRICA. Se usará log con datos de Voucherify.", finalKey);
				    return Mono.just(MsLog.builder()
							.errorDate(LocalDateTime.now())
							.severity("high")
							.errorType(String.valueOf(responseVoucherify.getResponse().getCode()))
							.errorCode(responseVoucherify.getResponse().getCode())
							.description(finalKey)
							.message(responseVoucherify.getResponse().getMessage())
							.place(request.getConsumerData().getCrPlace())
							.store(request.getConsumerData().getCrStore())
							.cash(request.getConsumerData().getCashRegister())
							.operation(request.getOperation())
							.idTransaction(1L).build());
				}))
				.onErrorResume(ex -> {
	                logger.error("Error consultando MsRespCode en BD: {}", ex.getMessage(), ex);
	                return Mono.just(buildDefaultLog(
	                		request.getConsumerData().getCrPlace(),
				    		request.getConsumerData().getCrStore(),
				    		request.getConsumerData().getCashRegister(), 
				    		request.getOperation(),
	                        "Excepción al consultar MsRespCode",
	                        "500",
	                        "Error interno del sistema"));
	            });
	}

	/**
	 * Método para mapear la entidad MsLog para el endpoint redeem 
	 *
	 * @param RequestRedeemVoucher request, ResponseRedeemVoucherify responseVoucherify
	 * @return MsLog
	 * @since 1.0.0
	 */
	public Mono<MsLog> toLogErrorRedeem(RequestRedeemVoucher request, ResponseRedeemVoucherify responseVoucherify) {
		
		return this.repoMsCode.findByMessageKeyIgnoreCase(responseVoucherify.getResponse().getKey(), REDEMPTION)
				.timeout(Duration.ofSeconds(3))
				.flatMap(msCode -> {
					return Mono.just(MsLog.builder()
							.errorDate(LocalDateTime.now())
							.severity(msCode.getSeverity())
							.errorType(String.valueOf(msCode.getCodeHttpMs()))
							.errorCode(msCode.getCodeMs().intValue())
							.description(msCode.getMessageKey())
							.message(msCode.getMessagePos())
							.action(msCode.getAction())
							.place(request.getConsumerData().getCrPlace())
							.store(request.getConsumerData().getCrStore())
							.cash(request.getConsumerData().getCashRegister())
							.operation(request.getOperation())
							.idTransaction(1L).build());
				}).switchIfEmpty(
						// Si no se encuentra el registro, hacer una consulta alternativa y
						// transformarlo a MsLog
						repoMsCode.findByMessageKeyIgnoreCase(GENERIC, REDEMPTION)
								.timeout(Duration.ofSeconds(3))
								.map(msCodeG -> MsLog.builder()
										.errorDate(LocalDateTime.now())
										.severity(msCodeG.getSeverity())
										.errorType(String.valueOf(msCodeG.getCodeHttpMs()))
										.errorCode(msCodeG.getCodeMs().intValue())
										.description(msCodeG.getMessageKey())
										.message(msCodeG.getMessagePos())
										.action(msCodeG.getAction())
										.place(request.getConsumerData().getCrPlace())
										.store(request.getConsumerData().getCrStore())
										.cash(request.getConsumerData().getCashRegister())
										.operation(request.getOperation())
										.idTransaction(1L)
										.build()))
				.switchIfEmpty(Mono.defer(() -> {
				    logger.warn("MsRespCode no encontrado con key={} ni con clave GENÉRICA. Se usará log con datos de Voucherify.", responseVoucherify.getResponse().getKey());
				    return Mono.just(MsLog.builder()
							.errorDate(LocalDateTime.now())
							.severity("high")
							.errorType(String.valueOf(responseVoucherify.getResponse().getCode()))
							.errorCode(responseVoucherify.getResponse().getCode())
							.description(responseVoucherify.getResponse().getKey())
							.message(responseVoucherify.getResponse().getKey())
							.place(request.getConsumerData().getCrPlace())
							.store(request.getConsumerData().getCrStore())
							.cash(request.getConsumerData().getCashRegister())
							.operation(request.getOperation())
							.idTransaction(1L).build());
				}))
				.onErrorResume(ex -> {
	                logger.error("Error consultando MsRespCode en BD: {}", ex.getMessage(), ex);
	                return Mono.just(buildDefaultLog(
	                		request.getConsumerData().getCrPlace(),
				    		request.getConsumerData().getCrStore(),
				    		request.getConsumerData().getCashRegister(), 
				    		request.getOperation(),
	                        "Excepción al consultar MsRespCode",
	                        "500",
	                        "Error interno del sistema"));
	            });
	}

	/**
	 * Método para mapear la entidad MsLog para el endpoint publications 
	 *
	 * @param RequestPublicationVoucher request,
			ResponsePublicationsVoucherify responseVoucherify
	 * @return MsLog
	 * @since 1.0.0
	 */
	public Mono<MsLog> toLogErrorPublications(RequestPublicationVoucher request,
			ResponsePublicationsVoucherify responseVoucherify) {

		return this.repoMsCode.findByMessageKeyIgnoreCase(responseVoucherify.getResponse().getKey(), PUBLICATION)
				.timeout(Duration.ofSeconds(3))
				.flatMap(msCode -> {
					return Mono.just(MsLog.builder()
							.errorDate(LocalDateTime.now())
							.severity(msCode.getSeverity())
							.errorType(String.valueOf(msCode.getCodeHttpMs()))
							.errorCode(msCode.getCodeMs().intValue())
							.description(msCode.getMessageKey())
							.message(msCode.getMessagePos())
							.action(msCode.getAction())
							.place(request.getConsumerData() != null ? request.getConsumerData().getCrPlace() : null)
							.store(request.getConsumerData() != null ? request.getConsumerData().getCrStore() : null)
							.cash(request.getConsumerData() != null ? request.getConsumerData().getCashRegister() : null)
							.operation(request.getOperation())
							.idTransaction(1L)
							.build());
				}).switchIfEmpty(
						// Si no se encuentra el registro, hacer una consulta alternativa y
						// transformarlo a MsLog
						repoMsCode.findByMessageKeyIgnoreCase(GENERIC, PUBLICATION)
								.timeout(Duration.ofSeconds(3))
								.map(msCodeG -> MsLog.builder()
										.errorDate(LocalDateTime.now())
										.severity(msCodeG.getSeverity())
										.errorType(String.valueOf(msCodeG.getCodeHttpMs()))
										.errorCode(msCodeG.getCodeMs().intValue())
										.description(msCodeG.getMessageKey())
										.message(msCodeG.getMessagePos())
										.action(msCodeG.getAction())
										.operation(request.getOperation())
										.idTransaction(1L)
										.build()))
				.switchIfEmpty(Mono.defer(() -> {
				    logger.warn("MsRespCode no encontrado con key={} ni con clave GENÉRICA. Se usará log con datos de Voucherify.", responseVoucherify.getResponse().getKey());
				    return Mono.just(MsLog.builder()
							.errorDate(LocalDateTime.now())
							.severity("high")
							.errorType(String.valueOf(responseVoucherify.getResponse().getCode()))
							.errorCode(responseVoucherify.getResponse().getCode())
							.description(responseVoucherify.getResponse().getKey())
							.message(responseVoucherify.getResponse().getKey())
							.place(request.getConsumerData().getCrPlace())
							.store(request.getConsumerData().getCrStore())
							.cash(request.getConsumerData().getCashRegister())
							.operation(request.getOperation())
							.idTransaction(1L).build());
				}))
				.onErrorResume(ex -> {
	                logger.error("Error consultando MsRespCode en BD: {}", ex.getMessage(), ex);
	                return Mono.just(buildDefaultLog(
	                		request.getConsumerData().getCrPlace(),
				    		request.getConsumerData().getCrStore(),
				    		request.getConsumerData().getCashRegister(), 
				    		request.getOperation(),
	                        "Excepción al consultar MsRespCode",
	                        "500",
	                        "Error interno del sistema"));
	            });
	}
	
	/**
	 * Método que construye una entidad mslog generica 
	 *
	 * @param RequestValidateVoucher request, String description, String errorCode, String message
	 * @return MsLog
	 * @since 1.0.0
	 */
	private MsLog buildDefaultLog(String crPlaza, String crTienda, Integer cash, String operation, String description, String errorCode, String message) {
	    return MsLog.builder()
	        .errorDate(LocalDateTime.now())
	        .severity("high")
	        .errorType("500")
	        .errorCode(Integer.parseInt(errorCode))
	        .description(description)
	        .message(message)
	        .action("Contacte al administrador")
	        .place(crPlaza)
	        .store(crTienda)
	        .cash(cash)
	        .operation(operation)
	        .idTransaction(1L)
	        .build();
	}
}
