/*
 * @(#)MsTransaction.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.femsa.oxxo.voucher.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Builder;
import lombok.Data;

/**
 * Entity de la tabla ms_transaction
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */

@Data
@Builder
@Table(name = "ms_transaction", schema = "backend_schema")
public class MsTransaction {
	
    @Id
    @Column("id")
    private Long id;

    @Column("place")
    private String place;

    @Column("store")
    private String store;

    @Column("cash")
    private Integer cash;

    @Column("transaction_date_channel")
    private LocalDate transactionDateChannel;

    @Column("transaction_time_channel")
    private LocalTime transactionTimeChannel;

    @Column("code")
    private Integer code;

    @Column("message")
    private String message;

    @Column("data_request")
    private String dataRequest;

    @Column("data_response")
    private String dataResponse;

    @Column("application")
    private String application;

    @Column("entity")
    private String entity;

    @Column("source")
    private String source;

    @Column("operation")
    private String operation;

    @Column("datetime_request")
    private OffsetDateTime datetimeRequest;

    @Column("datetime_response")
    private OffsetDateTime datetimeResponse;

    @Column("coupon")
    private String coupon;

    @Column("no_ticket")
    private String noTicket;

    @Column("id_ticket")
    private String idTicket;

    @Column("request_server")
    private String requestServer;

    @Column("operator")
    private String operator;

    @Column("http_code")
    private Integer httpCode;

    @Column("transaction_date_request")
    private OffsetDateTime transactionDateRequest;
    
    @Column("transaction_date_response")
    private OffsetDateTime transactionDateResponse;
    
    @Column("data_request_provider")
    private String dataRequestProvider;
    
    @Column("data_response_provider")
    private String dataResponseProvider;
	    
}
