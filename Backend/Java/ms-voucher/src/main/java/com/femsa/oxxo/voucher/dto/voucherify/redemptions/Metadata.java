/*
 * @(#)Metadata.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.femsa.oxxo.voucher.dto.voucherify.redemptions;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Clase que implementa el obj Metadata del request de Voucherify
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */
@Data
public class Metadata {

    @JsonProperty("MD_Redemption_ID_Area")
    private String redemptionIdArea;

    @JsonProperty("MD_Redemption_ID_Store")
    private String redemptionIdStore;

    @JsonProperty("MD_Redemption_Cash_Register")
    private Integer redemptionCashRegister;

    @JsonProperty("MD_Redemption_Ticket")
    private Integer redemptionTicket;

    @JsonProperty("MD_Redemption_Cashier")
    private String redemptionCashier;

    @JsonProperty("MD_Redemption_Transaction_ID")
    private String redemptionTransactionId;

    @JsonProperty("MD_Redemption_Member_ID_Reedemed")
    private String redemptionMemberIdRedeemed;
    
    @JsonProperty("MD_Redemption_Application")
    private String redemptionApplication;

}
