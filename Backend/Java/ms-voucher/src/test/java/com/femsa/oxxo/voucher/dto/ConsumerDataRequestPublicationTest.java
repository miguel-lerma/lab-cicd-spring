package com.femsa.oxxo.voucher.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.femsa.oxxo.voucher.utils.ValidatorUtil;

import jakarta.validation.ConstraintViolation;

class ConsumerDataRequestPublicationTest {

	 @Test
	    void testValidConsumerDataRequestPublication() {
	        ConsumerDataRequestPublication data = new ConsumerDataRequestPublication();
	        data.setApplication("App01");
	        data.setEntity("ENT");
	        data.setOrigin("OR123");
	        data.setCrPlace("PL123");
	        data.setCrStore("ST456");
	        data.setCashRegister(2);

	        Set<ConstraintViolation<ConsumerDataRequestPublication>> violations = ValidatorUtil.getValidator().validate(data);
	        assertTrue(violations.isEmpty());
	    }

	    @Test
	    void testInvalidConsumerDataRequestPublication() {
	        ConsumerDataRequestPublication data = new ConsumerDataRequestPublication();
	        data.setApplication("!!!");         // inválido
	        data.setEntity("ENTITY");           // demasiado largo
	        data.setOrigin(null);               // válido porque no tiene @NotNull
	        data.setCrPlace("1");               // demasiado corto
	        data.setCrStore("");                // vacío
	        data.setCashRegister(0);            // menor a 1

	        Set<ConstraintViolation<ConsumerDataRequestPublication>> violations = ValidatorUtil.getValidator().validate(data);
	        assertFalse(violations.isEmpty());
	        assertEquals(5, violations.size());
	    }

}
