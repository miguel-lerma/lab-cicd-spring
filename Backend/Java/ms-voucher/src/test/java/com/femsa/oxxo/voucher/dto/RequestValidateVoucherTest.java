package com.femsa.oxxo.voucher.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.femsa.oxxo.voucher.utils.ValidatorUtil;

import jakarta.validation.ConstraintViolation;

class RequestValidateVoucherTest {

	@Test
	void validRequestShouldPassValidation() {
		RequestValidateVoucher request = new RequestValidateVoucher();
		request.setOperation("VAL");
		request.setCoupon("COUPON123");
		request.setCashdate("20240514");
		request.setCashhour("120000");
		request.setCashier("EMP001");
		request.setTicket(100);
		request.setTransactionId("TX123");
		request.setServer("SRV1");
		request.setMemberId("MEMBER1");

	    ConsumerData consumerData = new ConsumerData();
	    consumerData.setApplication("APP001");
	    consumerData.setEntity("ENT");
	    consumerData.setOrigin("ORG");
	    consumerData.setCrPlace("PLAZ1");
	    consumerData.setCrStore("STR01");
	    consumerData.setCashRegister(1);

	    request.setConsumerData(consumerData);

	    Item item = new Item();
	    item.setSkuId("12345");
	    item.setRelatedObject("PRODUCT");
	    item.setQuantity(1);
	    item.setAmount(100);
	    item.setPrice(100.0);

	    Order order = new Order();
	    order.setItems(List.of(item));
	    order.setStatus("OK");
	    order.setAmount(100);

	    request.setOrder(order);

	    Set<ConstraintViolation<RequestValidateVoucher>> violations = ValidatorUtil.getValidator().validate(request);

	    assertTrue(violations.isEmpty());
	}

	@Test
	void invalidRequestShouldReturnViolations() {
		RequestValidateVoucher request = new RequestValidateVoucher(); // vacío
		Set<ConstraintViolation<RequestValidateVoucher>> violations = ValidatorUtil.getValidator().validate(request);
		assertFalse(violations.isEmpty());
		assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("obligatorio")));
	}

}
