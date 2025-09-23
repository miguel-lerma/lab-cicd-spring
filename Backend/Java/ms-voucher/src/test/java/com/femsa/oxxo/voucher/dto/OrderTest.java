package com.femsa.oxxo.voucher.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.femsa.oxxo.voucher.utils.ValidatorUtil;

import jakarta.validation.ConstraintViolation;

class OrderTest {

	@Test
    void testOrderValid() {
        Item item = new Item();
        item.setSkuId("111");
        item.setRelatedObject("SERVICE");
        item.setQuantity(1);
        item.setAmount(10);
        item.setPrice(5.0);

        Order order = new Order();
        order.setItems(List.of(item));
        order.setStatus("PAID");
        order.setAmount(10);

        Set<ConstraintViolation<Order>> violations = ValidatorUtil.getValidator().validate(order);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testOrderInvalid() {
        Item item = new Item(); // objeto inválido por campos nulos/defaults

        Order order = new Order();
        order.setItems(List.of(item)); // debe fallar por los errores en el item
        order.setStatus("@@@");        // inválido
        order.setAmount(0);            // inválido

        Set<ConstraintViolation<Order>> violations = ValidatorUtil.getValidator().validate(order);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getRootBeanClass().equals(Order.class)));
    }

}
