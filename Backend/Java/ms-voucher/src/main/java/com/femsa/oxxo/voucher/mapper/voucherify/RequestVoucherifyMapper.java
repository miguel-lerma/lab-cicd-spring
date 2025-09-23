/*
 * @(#)RequestVoucherifyMapper.java 1.0.0 12/05/25
 * 
 * Copyright 2025 FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.femsa.oxxo.voucher.mapper.voucherify;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.femsa.oxxo.voucher.config.VoucherifyProperties;
import com.femsa.oxxo.voucher.dto.RequestPublicationVoucher;
import com.femsa.oxxo.voucher.dto.RequestRedeemVoucher;
import com.femsa.oxxo.voucher.dto.RequestValidateVoucher;
import com.femsa.oxxo.voucher.dto.voucherify.publications.Campaign;
import com.femsa.oxxo.voucher.dto.voucherify.publications.CustomerRequest;
import com.femsa.oxxo.voucher.dto.voucherify.publications.MetadataRequest;
import com.femsa.oxxo.voucher.dto.voucherify.publications.RequestPublicationsVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.redemptions.RequestRedeemVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.validations.Metadata;
import com.femsa.oxxo.voucher.dto.voucherify.validations.OrderItem;
import com.femsa.oxxo.voucher.dto.voucherify.validations.Redeemable;
import com.femsa.oxxo.voucher.dto.voucherify.validations.RequestValidationsVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.validations.Session;

/**
 * Clase que mapea objetos request para los endpoints de Voucherify
 *
 * @author GTIM
 * @version 1.0.0, 12/05/25
 * @since 1.0.0
 *
 */
@Component
public class RequestVoucherifyMapper {
	
	
	private final VoucherifyProperties properties;
	
	public RequestVoucherifyMapper(VoucherifyProperties properties) {
        this.properties = properties;
    }

	/**
	 * Método para convertir el request recibido en el endpoint validate en un objeto para realizar un request a Voucherify en el endpoint validations
	 *
	 * @param RequestValidateVoucher request
	 * @return RequestValidationsVoucherify
	 * @since 1.0.0
	 */
	public RequestValidationsVoucherify mapRequestValidate(RequestValidateVoucher request) {

		RequestValidationsVoucherify validate = new RequestValidationsVoucherify();

		List<Redeemable> redeemables = new ArrayList<>();

		Redeemable redeemable = new Redeemable();

		// redemables 
		redeemable.setObject((properties.getObject() == null ) ? "voucher" : properties.getObject());
		redeemable.setId(request.getCoupon());

		redeemables.add(redeemable);

		validate.setRedeemables(redeemables);

		// order
		com.femsa.oxxo.voucher.dto.voucherify.validations.Order order = new com.femsa.oxxo.voucher.dto.voucherify.validations.Order();

		List<OrderItem> orderItems = new ArrayList<>();
		
		for (com.femsa.oxxo.voucher.dto.Item item : request.getOrder().getItems()) {
	        OrderItem orderItem = new OrderItem();
	        orderItem.setSkuId(item.getSkuId());
	        orderItem.setRelatedObject(item.getRelatedObject());
	        orderItem.setQuantity(item.getQuantity());
	        orderItem.setAmount(item.getAmount());
	        orderItem.setPrice(item.getPrice());
	        orderItems.add(orderItem);
	    }
		
		order.setItems(orderItems);
		order.setStatus(request.getOrder().getStatus());
		order.setAmount(request.getOrder().getAmount());
		
		validate.setOrder(order);

		// Sesion
		Session dataSession = new Session();
		dataSession.setType((properties.getSession().getType() == null ) ? "LOCK" : properties.getSession().getType());
		dataSession.setTtl((properties.getSession().getTtl() == null ) ? 2 : properties.getSession().getTtl());
		dataSession.setTtlUnit((properties.getSession().getTtlUnit() == null ) ? "MINUTES" : properties.getSession().getTtlUnit());
		dataSession.setKey(request.getTransactionId());

		validate.setSession(dataSession);

		// Metadata
		Metadata metadata = new Metadata();

		metadata.setRedemptionIdArea(request.getConsumerData().getCrPlace());
		metadata.setRedemptionIdStore(request.getConsumerData().getCrStore());
		metadata.setRedemptionCashRegister(request.getConsumerData().getCashRegister());
		metadata.setRedemptionTicket(request.getTicket());
		metadata.setRedemptionCashier(request.getCashier());
		metadata.setRedemptionTransactionId(request.getTransactionId());
		metadata.setRedemptionMemberIdRedeemed(request.getMemberId());
		metadata.setRedemptionApplication(request.getConsumerData().getApplication());

		validate.setMetadata(metadata);

		validate.setTrackingId(request.getMemberId());

		return validate;
	}

	/**
	 * Método para convertir el request recibido en el endpoint redeem en un objeto para realizar un request a Voucherify en el endpoint redemptions
	 *
	 * @param RequestRedeemVoucher request
	 * @return RequestRedeemVoucherify
	 * @since 1.0.0
	 */
	public RequestRedeemVoucherify mapRequestRedeem(RequestRedeemVoucher request) {

		RequestRedeemVoucherify validate = new RequestRedeemVoucherify();

		List<Redeemable> redeemables = new ArrayList<>();

		Redeemable redeemable = new Redeemable();

		// redemables
		redeemable.setObject((properties.getObject() == null ) ? "voucher" : properties.getObject());
		redeemable.setId(request.getCoupon());

		redeemables.add(redeemable);

		validate.setRedeemables(redeemables);

		// Metadata
		com.femsa.oxxo.voucher.dto.voucherify.redemptions.Metadata metadata = new com.femsa.oxxo.voucher.dto.voucherify.redemptions.Metadata();

		metadata.setRedemptionIdArea(request.getConsumerData().getCrPlace());
		metadata.setRedemptionIdStore(request.getConsumerData().getCrStore());
		metadata.setRedemptionCashRegister(request.getConsumerData().getCashRegister());
		metadata.setRedemptionTicket(request.getTicket());
		metadata.setRedemptionCashier(request.getCashier());
		metadata.setRedemptionTransactionId(request.getTransactionId());
		metadata.setRedemptionMemberIdRedeemed(request.getMemberId());
		metadata.setRedemptionApplication(request.getConsumerData().getApplication());

		validate.setMetadata(metadata);
		
		//tracking_id
		
		validate.setTrackingId(request.getMemberId());

		return validate;
	}

	/**
	 * Método para convertir el request recibido en el endpoint publication en un objeto para realizar un request a Voucherify en el endpoint publications
	 *
	 * @param RequestPublicationVoucher request
	 * @return RequestPublicationsVoucherify
	 * @since 1.0.0
	 */
	public RequestPublicationsVoucherify mapRequestPublication(RequestPublicationVoucher request) {

		RequestPublicationsVoucherify validate = new RequestPublicationsVoucherify();

		CustomerRequest customer = new CustomerRequest();
		MetadataRequest metadata = new MetadataRequest();
		Campaign campaign = new Campaign();

		// customer
		customer.setSourceId(request.getMemberId());

		// metadata
		metadata.setPublicationCompany(request.getConsumerData().getEntity());
		metadata.setPublicationApplication(request.getConsumerData().getApplication());
		
		//campaign
		campaign.setName(request.getCampaign());
		campaign.setCount(request.getCount());

		//return
		validate.setCustomer(customer);
		validate.setMetadata(metadata);
		validate.setCampaign(campaign);
		
		return validate;
	}
}
