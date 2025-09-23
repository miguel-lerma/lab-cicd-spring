/*
 * @(#)MsRespCode.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.femsa.oxxo.voucher.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Data;

/**
 * Entity de la tabla ms_resp_code
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */
@Table(name = "ms_resp_code", schema = "backend_schema")
@Data
public class MsRespCode {
	
	@Id
	@Column("id")
	private Long id;
	
    @Column("code_vou")
    private Long codeVou;

    @Column("code_http_ms")
    private Long codeHttpMs;

    @Column("code_ms")
    private Long codeMs;

    @Column("message_key")
    private String messageKey;

    @Column("message_pos")
    private String messagePos;

    @Column("operation")
    private String operation;

    @Column("severity")
    private String severity;

    @Column("action")
    private String action;

}
