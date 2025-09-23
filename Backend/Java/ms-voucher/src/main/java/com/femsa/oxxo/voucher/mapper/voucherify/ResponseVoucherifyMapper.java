/*
 * @(#)ResponseVoucherifyMapper.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.femsa.oxxo.voucher.mapper.voucherify;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.femsa.oxxo.voucher.dto.Error;
import com.femsa.oxxo.voucher.dto.Redemption;
import com.femsa.oxxo.voucher.dto.RequestPublicationVoucher;
import com.femsa.oxxo.voucher.dto.RequestRedeemVoucher;
import com.femsa.oxxo.voucher.dto.RequestValidateVoucher;
import com.femsa.oxxo.voucher.dto.ResponsePublicationVoucher;
import com.femsa.oxxo.voucher.dto.ResponseRedeemVoucher;
import com.femsa.oxxo.voucher.dto.ResponseValidateVoucher;
import com.femsa.oxxo.voucher.dto.Result;
import com.femsa.oxxo.voucher.dto.voucherify.publications.ResponsePublicationsVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.redemptions.ResponseRedeemVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.validations.RedeemableResponse;
import com.femsa.oxxo.voucher.dto.voucherify.validations.ResponseValidateVoucherify;
import com.femsa.oxxo.voucher.repository.MsRespCodeRepository;

import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * Clase que mapea objetos response para los endpoints de Voucherify
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */
@Component
public class ResponseVoucherifyMapper {
	
	private static final Logger logger = LoggerFactory.getLogger(ResponseVoucherifyMapper.class);
	
	private final MsRespCodeRepository repoMsCode;
	
	private static final String GENERIC = "generic"; 
	
	private static final String VALIDATE = "validate"; 
	
	private static final String REDEMPTION = "redemption";
	
	private static final String PUBLICATION = "publication";
	
	public ResponseVoucherifyMapper(MsRespCodeRepository repoMsCode) {
		this.repoMsCode = repoMsCode;
	}

	/**
	 * Método para convertir el response del endpoint validations de Voucherify en el response del endpoint validate 
	 *
	 * @param ResponsePublicationsVoucherify response, RequestPublicationVoucher request
	 * @return ResponsePublicationVoucher, Integer
	 * @since 1.0.0
	 */
	public Mono<Tuple2<ResponsePublicationVoucher, Integer>> mapPublications(ResponsePublicationsVoucherify response, RequestPublicationVoucher request) {

		ResponsePublicationVoucher responseDto = new ResponsePublicationVoucher();
		
		Integer httpCode = HttpStatus.OK.value();

		if (response.getResponse().getVoucher() != null) {

			String resultVou = response.getResponse().getResult();

			if (resultVou.equalsIgnoreCase("SUCCESS")) {
				responseDto.setValid(true);
			} else {
				responseDto.setValid(false);
			}

			responseDto.setMemberId((response.getResponse().getCustomer().getMetadata().getMemberIdAssigned() == null)
					? ""
					: response.getResponse().getCustomer().getMetadata().getMemberIdAssigned());
			
			responseDto.setCampaign((response.getResponse().getVoucher().getCampaignId() == null) 
					? request.getCampaign() 
					: response.getResponse().getVoucher().getCampaignId());
			
			responseDto.setCount(request.getCount());

			List<String> voucher = new ArrayList<String>();
			voucher.add(response.getResponse().getVoucher().getCode());

			responseDto.setVouchers(voucher);
			
			return Mono.just(Tuples.of(responseDto, httpCode));

		} else if (response.getResponse().getVouchers() != null) {

			String resultVou = response.getResponse().getResult();

			if (resultVou.equalsIgnoreCase("SUCCESS")) {
				responseDto.setValid(true);
			} else {
				responseDto.setValid(false);
			}

			responseDto.setMemberId((response.getResponse().getCustomer().getMetadata().getMemberIdAssigned() == null)
					? request.getMemberId()
					: response.getResponse().getCustomer().getMetadata().getMemberIdAssigned());
			responseDto.setCampaign(request.getCampaign());
			responseDto.setCount(request.getCount());
			responseDto.setVouchers(response.getResponse().getVouchers());
			
			return Mono.just(Tuples.of(responseDto, httpCode));

		} else {

			Result result = new Result();
			Error error = new Error();

			responseDto.setValid(false);
			responseDto.setMemberId(request.getMemberId());
			responseDto.setCampaign(request.getCampaign());
			responseDto.setCount(request.getCount());

			try {
				
				logger.info("Key recibido desde Voucherify: " + response.getResponse().getKey());
			
				return repoMsCode.findByMessageKeyIgnoreCase(response.getResponse().getKey(), PUBLICATION)
						.timeout(Duration.ofSeconds(3))
						.flatMap(restBd -> {
							logger.info("Message equivalente obtenido desde BD: " + restBd.getMessagePos());
			
							error.setCode(restBd.getCodeMs().intValue());
							error.setMessage(restBd.getMessagePos());
							result.setError(error);
		                    responseDto.setResult(result);
		                    return Mono.just(Tuples.of(responseDto, restBd.getCodeHttpMs().intValue()));
						})
						.switchIfEmpty(
								repoMsCode.findByMessageKeyIgnoreCase(GENERIC, PUBLICATION)
								.timeout(Duration.ofSeconds(3))
								.flatMap(restBd -> {
									logger.info("Obteniendo message generico");
									logger.info("Message equivalente obtenido desde BD: " + restBd.getMessagePos());
	
									error.setCode(restBd.getCodeMs().intValue());
									error.setMessage(restBd.getMessagePos());
									result.setError(error);
				                    responseDto.setResult(result);
				                    return Mono.just(Tuples.of(responseDto, restBd.getCodeHttpMs().intValue()));
								})
						)
						.switchIfEmpty(Mono.defer(() -> {
							logger.warn("No se encontró message en BD, se mantiene respuesta de Voucherify");
							error.setCode(response.getResponse().getCode());
		                    error.setMessage(response.getResponse().getMessage());
		                    result.setError(error);
		                    responseDto.setResult(result);
		                    return Mono.just(Tuples.of(responseDto, response.getResponse().getCode()));
						}))
						.onErrorResume(e -> {
							logger.error("Error al consultar el message en BD: {}", e.getMessage(), e);
							logger.info("Se mantiene respuesta de Voucherify");
							error.setCode(response.getResponse().getCode());
		                    error.setMessage(response.getResponse().getMessage());
		                    result.setError(error);
		                    responseDto.setResult(result);
		                    return Mono.just(Tuples.of(responseDto, response.getResponse().getCode()));
						});
			}catch(Exception e) {
				logger.error("Error al consultar el message en BD desde try catch: {}", e.getMessage(), e);
				logger.info("Se mantiene respuesta de Voucherify");
				error.setCode(response.getResponse().getCode());
                error.setMessage(response.getResponse().getMessage());
                result.setError(error);
                responseDto.setResult(result);
                return Mono.just(Tuples.of(responseDto, response.getResponse().getCode()));
			}
		}
	}
	
	/**
	 * Método para convertir el response del endpoint validations de Voucherify en el response del endpoint validate 
	 *
	 * @param ResponseValidateVoucherify response, RequestValidateVoucher request
	 * @return ResponseValidateVoucher, Integer
	 * @since 1.0.0
	 */
	public Mono<Tuple2<ResponseValidateVoucher, Integer>> mapValidation(ResponseValidateVoucherify response, RequestValidateVoucher request) {

		ResponseValidateVoucher responseDto = new ResponseValidateVoucher();
		
		Integer httpCode = HttpStatus.OK.value();
		
		responseDto.setValid(response.getResponse().isValid());
		responseDto.setCoupon(request.getCoupon());
		
		if (response.getResponse().getStackingRules() != null) {
			responseDto.setRedeemablesLimit(response.getResponse().getStackingRules().getRedeemablesLimit());
			responseDto.setApplicableRedeemablesLimit(response.getResponse().getStackingRules().getApplicableRedeemablesLimit());
			responseDto.setApplicableExclusiveRedeemablesLimit(response.getResponse().getStackingRules().getApplicableExclusiveRedeemablesLimit());
		} else {
			responseDto.setRedeemablesLimit(0);
			responseDto.setApplicableRedeemablesLimit(0);
			responseDto.setApplicableExclusiveRedeemablesLimit(0);
		}
		
		if (response.getResponse().getRedeemables() != null && !response.getResponse().getRedeemables().isEmpty()) {
			RedeemableResponse redeemable = response.getResponse().getRedeemables().get(0);

			responseDto.setTypeCoupon(
					(redeemable.getMetadata() != null && redeemable.getMetadata().getMdVoucherTypeCoupon() !=null)
							? redeemable.getMetadata().getMdVoucherTypeCoupon()
							: 0);

			responseDto.setVoucherRmsId(
					(redeemable.getMetadata() != null && redeemable.getMetadata().getMdVoucherRmsTransactionId() !=null)
							? redeemable.getMetadata().getMdVoucherRmsTransactionId()
							: 0);
			
			responseDto.setMemberId(
					(redeemable.getMetadata() != null && redeemable.getMetadata().getMdVoucherMemberIdAssigned() !=null)
							? redeemable.getMetadata().getMdVoucherMemberIdAssigned()
							: request.getMemberId());

			//Aqui se capturan los errores recibidos en httpStatus 200
			if (redeemable.getResult() != null && redeemable.getResult().getError() != null) {

				Result result = new Result();
				Error error = new Error();
				
				try {
				
					return repoMsCode.findByMessageKeyIgnoreCase(response.getResponse().getRedeemables().get(0).getResult().getError().getKey(), VALIDATE)
							.timeout(Duration.ofSeconds(3))
							.flatMap(restBd -> {
								logger.info("Message equivalente obtenido desde BD: " + restBd.getMessagePos());
				
								error.setCode(restBd.getCodeMs().intValue());
								error.setMessage(restBd.getMessagePos());
								result.setError(error);
			                    responseDto.setResult(result);
			                    return Mono.just(Tuples.of(responseDto, restBd.getCodeHttpMs().intValue()));
								
							})
							.switchIfEmpty(repoMsCode.findByMessageKeyIgnoreCase(GENERIC, VALIDATE)
									.timeout(Duration.ofSeconds(3))
									.flatMap(restBd -> {
										logger.info("Obteniendo message generico");
										logger.info("Message equivalente obtenido desde BD: " + restBd.getMessagePos());
	
										error.setCode(restBd.getCodeMs().intValue());
										error.setMessage(restBd.getMessagePos());
										result.setError(error);
					                    responseDto.setResult(result);
					                    return Mono.just(Tuples.of(responseDto, restBd.getCodeHttpMs().intValue()));
							}))
							.switchIfEmpty(Mono.defer(() -> {
								logger.warn("No se encontró message en BD, se mantiene respuesta de Voucherify");
								error.setCode(response.getResponse().getRedeemables().get(0).getResult().getError().getCode());
			                    error.setMessage(response.getResponse().getRedeemables().get(0).getResult().getError().getMessage());
			                    result.setError(error);
			                    responseDto.setResult(result);
			                    return Mono.just(Tuples.of(responseDto, response.getResponse().getRedeemables().get(0).getResult().getError().getCode()));
							}))
							.onErrorResume(e -> {
								logger.error("Error al consultar el message en BD: {}", e.getMessage(), e);
								logger.info("Se mantiene respuesta de Voucherify");
								error.setCode(response.getResponse().getRedeemables().get(0).getResult().getError().getCode());
			                    error.setMessage(response.getResponse().getRedeemables().get(0).getResult().getError().getMessage());
			                    result.setError(error);
			                    responseDto.setResult(result);
			                    return Mono.just(Tuples.of(responseDto, response.getResponse().getRedeemables().get(0).getResult().getError().getCode()));
							});
				}catch(Exception e) {
					logger.error("Error al consultar el message en BD desde try catch: {}", e.getMessage(), e);
					logger.info("Se mantiene respuesta de Voucherify");
					error.setCode(response.getResponse().getRedeemables().get(0).getResult().getError().getCode());
                    error.setMessage(response.getResponse().getRedeemables().get(0).getResult().getError().getMessage());
                    result.setError(error);
                    responseDto.setResult(result);
                    return Mono.just(Tuples.of(responseDto, response.getResponse().getRedeemables().get(0).getResult().getError().getCode()));
				}
			}

		} else {
			
			//Aqui se capturan los errores recibidos en httpsStatus diferente a 200
			responseDto.setTypeCoupon(0);
			responseDto.setVoucherRmsId(0L);
			responseDto.setMemberId(request.getMemberId());
			Result result = new Result();
			Error error = new Error();

			logger.info("Key recibido desde Voucherify: " + response.getResponse().getKey());
			
			try {

				return repoMsCode.findByMessageKeyIgnoreCase(response.getResponse().getKey(), VALIDATE)
						.timeout(Duration.ofSeconds(3))
						.flatMap(restBd -> {
							logger.info("Message equivalente obtenido desde BD: " + restBd.getMessagePos());
			
							error.setCode(restBd.getCodeMs().intValue());
							error.setMessage(restBd.getMessagePos());
							result.setError(error);
		                    responseDto.setResult(result);
		                    return Mono.just(Tuples.of(responseDto, restBd.getCodeHttpMs().intValue()));
							
						})
						.switchIfEmpty(repoMsCode.findByMessageKeyIgnoreCase(GENERIC, VALIDATE)
								.timeout(Duration.ofSeconds(3))
								.flatMap(restBd -> {
									logger.info("Obteniendo message generico");
									logger.info("Message equivalente obtenido desde BD: " + restBd.getMessagePos());
	
									error.setCode(restBd.getCodeMs().intValue());
									error.setMessage(restBd.getMessagePos());
									result.setError(error);
				                    responseDto.setResult(result);
				                    return Mono.just(Tuples.of(responseDto, restBd.getCodeHttpMs().intValue()));
						}))
						.switchIfEmpty(Mono.defer(() -> {
							logger.warn("No se encontró message en BD, se mantiene respuesta de Voucherify");
							error.setCode(response.getResponse().getCode());
		                    error.setMessage(response.getResponse().getMessage());
		                    result.setError(error);
		                    responseDto.setResult(result);
		                    return Mono.just(Tuples.of(responseDto, response.getResponse().getCode()));
						}))
						.onErrorResume(e -> {
							logger.error("Error al consultar el message en BD: {}", e.getMessage(), e);
							logger.info("Se mantiene respuesta de Voucherify");
							error.setCode(response.getResponse().getCode());
		                    error.setMessage(response.getResponse().getMessage());
		                    result.setError(error);
		                    responseDto.setResult(result);
		                    return Mono.just(Tuples.of(responseDto, response.getResponse().getCode()));
						});
				
			}catch(Exception e) {
				logger.error("Error al consultar el message en BD desde try catch: {}", e.getMessage(), e);
				logger.info("Se mantiene respuesta de Voucherify");
				error.setCode(response.getResponse().getCode());
                error.setMessage(response.getResponse().getMessage());
                result.setError(error);
                responseDto.setResult(result);
                return Mono.just(Tuples.of(responseDto, response.getResponse().getCode()));
			}
		}
		
		return Mono.just(Tuples.of(responseDto, httpCode));
	}
	
	/**
	 * Método para convertir el response del endpoint redemptions de Voucherify en el response del endpoint redeem 
	 *
	 * @param ResponseRedeemVoucherify response, RequestRedeemVoucher request
	 * @return ResponseRedeemVoucher, Integer
	 * @since 1.0.0
	 */
	public Mono<Tuple2<ResponseRedeemVoucher, Integer>> mapRedeem(ResponseRedeemVoucherify response, RequestRedeemVoucher request) {

		ResponseRedeemVoucher responseDto = new ResponseRedeemVoucher();
		
		Integer httpCode = HttpStatus.OK.value();

		Redemption redemption = new Redemption();

		if (response.getResponse().getRedemptions() == null) {
			Result result = new Result();
			Error error = new Error();

			responseDto.setValid(false);
			responseDto.setCoupon(request.getCoupon());
			responseDto.setMemberId(request.getMemberId());
			
			try {
				
				logger.info("Key recibido desde Voucherify: " + response.getResponse().getKey());
				
				return repoMsCode.findByMessageKeyIgnoreCase(response.getResponse().getKey(), REDEMPTION)
						.timeout(Duration.ofSeconds(3))
						.flatMap(restBd -> {
							logger.info("Message equivalente obtenido desde BD: " + restBd.getMessagePos());
			
							error.setCode(restBd.getCodeMs().intValue());
							error.setMessage(restBd.getMessagePos());
							result.setError(error);
		                    responseDto.setResult(result);
		                    return Mono.just(Tuples.of(responseDto, restBd.getCodeHttpMs().intValue()));
							
						})
						.switchIfEmpty(repoMsCode.findByMessageKeyIgnoreCase(GENERIC, REDEMPTION)
								.timeout(Duration.ofSeconds(3))
								.flatMap(restBd -> {
									logger.info("Obteniendo message generico");
									logger.info("Message equivalente obtenido desde BD: " + restBd.getMessagePos());

									error.setCode(restBd.getCodeMs().intValue());
									error.setMessage(restBd.getMessagePos());
									result.setError(error);
				                    responseDto.setResult(result);
				                    return Mono.just(Tuples.of(responseDto, restBd.getCodeHttpMs().intValue()));
								})
						)
						.switchIfEmpty(Mono.defer(() -> {
							logger.warn("No se encontró message en BD, se mantiene respuesta de Voucherify");
							error.setCode(response.getResponse().getCode());
		                    error.setMessage(response.getResponse().getMessage());
		                    result.setError(error);
		                    responseDto.setResult(result);
		                    return Mono.just(Tuples.of(responseDto, response.getResponse().getCode()));
						})).onErrorResume(e -> {
							logger.error("Error al consultar el message en BD: {}", e.getMessage(), e);
							logger.info("Se mantiene respuesta de Voucherify");
							error.setCode(response.getResponse().getCode());
		                    error.setMessage(response.getResponse().getMessage());
		                    result.setError(error);
		                    responseDto.setResult(result);
		                    return Mono.just(Tuples.of(responseDto, response.getResponse().getCode()));
						});
				
			}catch(Exception e) {
				logger.error("Error al consultar el message en BD desde try catch: {}", e.getMessage(), e);
				logger.info("Se mantiene respuesta de Voucherify");
				error.setCode(response.getResponse().getCode());
                error.setMessage(response.getResponse().getMessage());
                result.setError(error);
                responseDto.setResult(result);
                return Mono.just(Tuples.of(responseDto, response.getResponse().getCode()));
			}
			
		} else {

			String resultVou = response.getResponse().getRedemptions().get(0).getResult();

			if (resultVou.equalsIgnoreCase("SUCCESS")) {
				responseDto.setValid(true);
			} else {
				responseDto.setValid(false);
			}

			responseDto.setCoupon((response.getResponse().getRedemptions().get(0).getVoucher().getCode() == null) ? request.getCoupon() : response.getResponse().getRedemptions().get(0).getVoucher().getCode());
			responseDto.setMemberId((response.getResponse().getRedemptions().get(0).getMetadata().getRedemptionMemberIdRedeemed() == null) ? request.getMemberId() : response.getResponse().getRedemptions().get(0).getMetadata().getRedemptionMemberIdRedeemed());

			redemption.setQuantity(response.getResponse().getRedemptions().get(0).getVoucher().getRedemption().getQuantity());
			redemption.setRedeemedQuantity(response.getResponse().getRedemptions().get(0).getVoucher().getRedemption().getRedeemedQuantity());
			responseDto.setRedemption(redemption);
			
			return Mono.just(Tuples.of(responseDto, httpCode));

		}
	}
}
