/*
 * @(#)LogSanitizer.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.femsa.oxxo.voucher.utils;

import java.util.regex.Pattern;

/**
 * Clase que sanitiza los parametros enviados en los logs
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */
public class LogSanitizer {

	private static final Pattern CONTROL_CHARS = Pattern.compile("[\\r\\n\\t\\f\\u0000-\\u001F]");
    private static final Pattern DANGEROUS_CHARS = Pattern.compile("[\'|;\\\\]");
        
    private LogSanitizer() {
    }

	/**
	 * Sanitiza un String para uso seguro en logs. Elimina caracteres de control y
	 * caracteres potencialmente peligrosos.
	 */
	public static String sanitize(String input) {
		
		if (input == null) {
			return null;
		}

		// Elimina caracteres de control (\n, \r, \t, etc.)
		String sanitized = CONTROL_CHARS.matcher(input).replaceAll("");

		// Reemplaza caracteres potencialmente peligrosos con guiones bajos
		sanitized = DANGEROUS_CHARS.matcher(sanitized).replaceAll("_");

		return sanitized;
	}

}
