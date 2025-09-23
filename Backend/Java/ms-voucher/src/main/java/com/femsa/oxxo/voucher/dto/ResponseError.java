package com.femsa.oxxo.voucher.dto;

/**
 * Clase que implementa el obj de respuesta para las excepciones capturadas en ExceptionHandler 
 * para los endpoints validate, redeem y publication
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */
public class ResponseError {
	
	/**
	 * Construye una respuesta genérica en el obj Result con el código y mensaje recibidos.
	 *
	 * @param code Código HTTP.
	 * @param message Mensaje descriptivo del error.
	 * @return Result que contiene los detalles del error.
	 */
	public static Result buildErrorGenericResponse(int code, String message) {

		Result result = new Result();
		Error er = new Error();
		
		er.setCode(code);
		er.setMessage(message);

		result.setError(er);

		return result;
	}
	

	/**
	 * Construye una respuesta de error para el flujo de validación de cupones.
	 *
	 * @param code Código del error.
	 * @param message Mensaje del error.
	 * @return ResponseValidateVoucher con campos por defecto e información del error.
	 */
	public static ResponseValidateVoucher buildErrorValidateResponse(int code, String message) {

		ResponseValidateVoucher resp = new ResponseValidateVoucher();
		Result result = new Result();
		Error er = new Error();
		
		er.setCode(code);
		er.setMessage(message);

		result.setError(er);

		resp.setValid(false);
		resp.setTypeCoupon(0);
		resp.setCoupon("0");
		resp.setVoucherRmsId(0L);
		resp.setMemberId("0");
		resp.setRedeemablesLimit(0);
		resp.setApplicableRedeemablesLimit(0);
		resp.setApplicableExclusiveRedeemablesLimit(0);
		resp.setResult(result);

		return resp;
	}

	/**
	 * Construye una respuesta de error para el flujo de redención de cupones.
	 *
	 * @param code Código del error.
	 * @param message Mensaje de error.
	 * @return ResponseRedeemVoucher con campos por defecto e información del error.
	 */
	public static ResponseRedeemVoucher buildErrorRedeemResponse(int code, String message) {

		ResponseRedeemVoucher resp = new ResponseRedeemVoucher();
		Result result = new Result();
		Error er = new Error();
		
		er.setCode(code);
		er.setMessage(message);

		result.setError(er);

		resp.setValid(false);
		resp.setCoupon("0");
		resp.setMemberId("0");
		
		resp.setResult(result);

		return resp;
	}

	/**
	 * Construye una respuesta de error para el flujo de publicación de cupones.
	 *
	 * @param code Código de error.
	 * @param message Mensaje del error.
	 * @return ResponsePublicationVoucher con valores por defecto e información del error.
	 */
	public static ResponsePublicationVoucher buildErrorPublicationsResponse(int code, String message) {

		ResponsePublicationVoucher resp = new ResponsePublicationVoucher();
		
		Result result = new Result();
		Error er = new Error();
		
		er.setCode(code);
		er.setMessage(message);

		result.setError(er);
		
		resp.setValid(false);
		resp.setMemberId("0");
		resp.setCampaign("0");
		resp.setCount(0);
		resp.setResult(result);

		return resp; 
	}
}
