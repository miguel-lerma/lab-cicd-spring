package com.femsa.oxxo.voucher.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.Set;

import com.femsa.oxxo.voucher.utils.ValidatorUtil;

import jakarta.validation.ConstraintViolation;

class ItemTest {

	@Test
	void testItemValid() {
		Item item = new Item();
		item.setSkuId("123456");
		item.setRelatedObject("PRODUCT");
		item.setQuantity(2);
		item.setAmount(100);
		item.setPrice(49.99);

		Set<ConstraintViolation<Item>> violations = ValidatorUtil.getValidator().validate(item);
		assertTrue(violations.isEmpty());
	}

	@Test
	void testItemInvalid() {
		Item item = new Item();
		item.setSkuId("sku#"); // inválido
		item.setRelatedObject("1234"); // no solo letras
		item.setQuantity(0); // inválido
		item.setAmount(-10); // inválido
		item.setPrice(0.0); // inválido

		Set<ConstraintViolation<Item>> violations = ValidatorUtil.getValidator().validate(item);
		assertFalse(violations.isEmpty());
		assertEquals(5, violations.size());
	}

}
