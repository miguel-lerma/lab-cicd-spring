package com.femsa.oxxo.voucher.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.femsa.oxxo.voucher.utils.ValidatorUtil;

import jakarta.validation.ConstraintViolation;

class ConsumerDataTest {

	@Test
    void testConsumerDataValid() {
        ConsumerData data = new ConsumerData();
        data.setApplication("App123");
        data.setEntity("ABC");
        data.setOrigin("DEF");
        data.setCrPlace("X1234");
        data.setCrStore("Y5678");
        data.setCashRegister(5);

        Set<ConstraintViolation<ConsumerData>> violations = ValidatorUtil.getValidator().validate(data);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testConsumerDataInvalid() {
        ConsumerData data = new ConsumerData();
        data.setApplication("$$");  // inválido
        data.setEntity("ABCD");     // muy largo
        data.setOrigin("");         // vacío
        data.setCrPlace("X1");      // corto
        data.setCrStore(null);      // null pero sin @NotNull, válido
        data.setCashRegister(0);    // menor a 1

        Set<ConstraintViolation<ConsumerData>> violations = ValidatorUtil.getValidator().validate(data);
        assertFalse(violations.isEmpty());
        assertEquals(5, violations.size());
    }

}
