/*
 * @(#)GlobalExceptionHandler.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.femsa.oxxo.voucher.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.femsa.oxxo.voucher.dto.ResponseError;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Clase que implementa la respuesta de Excepciones Globales
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */
@RestControllerAdvice
public class GlobalExceptionHandler {


	/**
	 * Método para capturar las exception de argumentos no validos en los request
	 *
	 * @param MethodArgumentNotValidException ex, HttpServletRequest request
	 * @return ResponseEntity
	 * @since 1.0.0
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<?> handleArgumentsvalidExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
		String path = request.getRequestURI();
		
		if (path.contains("/validate")) {
			return new ResponseEntity<>(
					ResponseError.buildErrorValidateResponse(HttpStatus.BAD_REQUEST.value(), "Datos recibidos inválidos"),
					HttpStatus.BAD_REQUEST);
	    } else if (path.contains("/redeem")) {
	    	return new ResponseEntity<>(
					ResponseError.buildErrorRedeemResponse(HttpStatus.BAD_REQUEST.value(), "Datos recibidos inválidos"),
					HttpStatus.BAD_REQUEST);
	    } else if (path.contains("/publication")) {
	    	return new ResponseEntity<>(
					ResponseError.buildErrorPublicationsResponse(HttpStatus.BAD_REQUEST.value(), "Datos recibidos inválidos"),
					HttpStatus.BAD_REQUEST);
	    }
		
		return new ResponseEntity<>(
				ResponseError.buildErrorGenericResponse(HttpStatus.BAD_REQUEST.value(), "Datos recibidos inválidos"),
				HttpStatus.BAD_REQUEST);

	}

	/**
	 * Método para capturar las exception de path incorrecto en los request
	 *
	 * @param NoHandlerFoundException ex, HttpServletRequest request
	 * @return ResponseEntity
	 * @since 1.0.0
	 */
	@ExceptionHandler(NoHandlerFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ResponseEntity<?> handleNotFoundException(NoHandlerFoundException ex, HttpServletRequest request) {
		
		String path = request.getRequestURI();
		
		if (path.contains("/validate")) {
			return new ResponseEntity<>(
					ResponseError.buildErrorValidateResponse(HttpStatus.NOT_FOUND.value(), "Ruta no encontrada"),
					HttpStatus.NOT_FOUND);
	    } else if (path.contains("/redeem")) {
	    	return new ResponseEntity<>(
					ResponseError.buildErrorRedeemResponse(HttpStatus.NOT_FOUND.value(), "Ruta no encontrada"),
					HttpStatus.NOT_FOUND);
	    } else if (path.contains("/publication")) {
	    	return new ResponseEntity<>(
					ResponseError.buildErrorPublicationsResponse(HttpStatus.NOT_FOUND.value(), "Ruta no encontrada"),
					HttpStatus.NOT_FOUND);
	    }

		return new ResponseEntity<>(
				ResponseError.buildErrorGenericResponse(HttpStatus.NOT_FOUND.value(), "Ruta no encontrada"),
				HttpStatus.NOT_FOUND);
	}

	/**
	 * Método para capturar las exception de metodos no permitidos en los request
	 *
	 * @param HttpRequestMethodNotSupportedException ex, HttpServletRequest request
	 * @return ResponseEntity
	 * @since 1.0.0
	 */
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	public ResponseEntity<?> handleMethodNotAllowedException(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
		
		String path = request.getRequestURI();
		
		if (path.contains("/validate")) {
			return new ResponseEntity<>(
					ResponseError.buildErrorValidateResponse(HttpStatus.METHOD_NOT_ALLOWED.value(), "Método no permitido"),
					HttpStatus.METHOD_NOT_ALLOWED);
	    } else if (path.contains("/redeem")) {
	    	return new ResponseEntity<>(
					ResponseError.buildErrorRedeemResponse(HttpStatus.METHOD_NOT_ALLOWED.value(), "Método no permitido"),
					HttpStatus.METHOD_NOT_ALLOWED);
	    } else if (path.contains("/publication")) {
	    	return new ResponseEntity<>(
					ResponseError.buildErrorPublicationsResponse(HttpStatus.METHOD_NOT_ALLOWED.value(), "Método no permitido"),
					HttpStatus.METHOD_NOT_ALLOWED);
	    }

		return new ResponseEntity<>(
				ResponseError.buildErrorGenericResponse(HttpStatus.METHOD_NOT_ALLOWED.value(), "Método no permitido"),
				HttpStatus.METHOD_NOT_ALLOWED);

	}
}