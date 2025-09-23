/*
 * @(#)MsLog.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.femsa.oxxo.voucher.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Builder;
import lombok.Data;

/**
 * Entity de la tabla ms_log
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */
@Builder
@Data
@Table(name = "ms_log", schema = "backend_schema")
public class MsLog {
	
    @Id
    @Column("error_id")
    private Long errorId;

    @Column("error_date")
    private LocalDateTime errorDate;

    @Column("severity")
    private String severity;

    @Column("error_type")
    private String errorType;

    @Column("error_code")
    private Integer errorCode;

    @Column("description")
    private String description;

    @Column("message")
    private String message;

    @Column("action")
    private String action;

    @Column("place")
    private String place;

    @Column("store")
    private String store;

    @Column("cash")
    private Integer cash;

    @Column("operation")
    private String operation;

    @Column("id_transaction")
    private Long idTransaction; // Referencia a MsTransaction

}
