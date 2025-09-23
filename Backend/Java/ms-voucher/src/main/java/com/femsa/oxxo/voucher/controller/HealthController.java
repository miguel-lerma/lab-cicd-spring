/*
 * @(#)HealthController.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.femsa.oxxo.voucher.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para validar estatus del proyecto
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 */
@RestController
@RequestMapping("/voucher")
public class HealthController {

	@GetMapping("/status")
	public ResponseEntity<String> status() {
		return ResponseEntity.ok("OK");
	}

}