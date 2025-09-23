package com.femsa.oxxo.voucher.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.mock.env.MockEnvironment;

class VoucherifyPropertiesTest {
	
	@Test
    void testPropertiesBinding() {
        // Crear entorno simulado con propiedades
        MockEnvironment environment = new MockEnvironment()
            .withProperty("voucherify.url", "https://api.voucherify.io")
            .withProperty("voucherify.channel", "web")
            .withProperty("voucherify.object", "voucher")
            .withProperty("voucherify.app.id", "test-app-id")
            .withProperty("voucherify.app.token", "test-app-token")
            .withProperty("voucherify.session.type", "standard")
            .withProperty("voucherify.session.ttl", "30")
            .withProperty("voucherify.session.ttlUnit", "minutes")
            .withProperty("voucherify.url-endpoint.validate", "/validate")
            .withProperty("voucherify.url-endpoint.redeem", "/redeem")
            .withProperty("voucherify.url-endpoint.publication", "/publication");

        // Usar Binder para bindear manualmente las propiedades
        VoucherifyProperties properties = Binder.get(environment)
            .bind("voucherify", Bindable.of(VoucherifyProperties.class))
            .orElseThrow(() -> new IllegalStateException("No se pudieron bindear las propiedades voucherify"));

        // Verificaciones
        assertEquals("https://api.voucherify.io", properties.getUrl());
        assertEquals("web", properties.getChannel());
        assertEquals("voucher", properties.getObject());

        assertNotNull(properties.getApp());
        assertEquals("test-app-id", properties.getApp().getId());
        assertEquals("test-app-token", properties.getApp().getToken());

        assertNotNull(properties.getSession());
        assertEquals("standard", properties.getSession().getType());
        assertEquals(30, properties.getSession().getTtl());
        assertEquals("minutes", properties.getSession().getTtlUnit());

        assertNotNull(properties.getUrlEndpoint());
        assertEquals("/validate", properties.getUrlEndpoint().getValidate());
        assertEquals("/redeem", properties.getUrlEndpoint().getRedeem());
        assertEquals("/publication", properties.getUrlEndpoint().getPublication());
    }
	

}
