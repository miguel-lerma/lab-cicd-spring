package com.femsa.oxxo.voucher.dto.voucherify.redemptions;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;

class ResponseRedeemVoucherifyTest {

	@Test
    void testConstructorAndGetters() {
        ResponseRedeem response = new ResponseRedeem();
        OffsetDateTime start = OffsetDateTime.now().minusSeconds(3);
        OffsetDateTime end = OffsetDateTime.now();
        String reqJson = "{\"voucher\":\"XYZ456\"}";
        String resJson = "{\"status\":\"REDEEMED\"}";

        ResponseRedeemVoucherify dto = new ResponseRedeemVoucherify(response, start, end, reqJson, resJson);

        assertEquals(response, dto.getResponse());
        assertEquals(start, dto.getStartTime());
        assertEquals(end, dto.getEndTime());
        assertEquals(reqJson, dto.getRequestJson());
        assertEquals(resJson, dto.getResponseJson());
    }

    @Test
    void testSetters() {
        ResponseRedeemVoucherify dto = new ResponseRedeemVoucherify();
        OffsetDateTime now = OffsetDateTime.now();

        dto.setResponse(new ResponseRedeem());
        dto.setStartTime(now);
        dto.setEndTime(now);
        dto.setRequestJson("redeemReq");
        dto.setResponseJson("redeemRes");

        assertNotNull(dto.getResponse());
        assertEquals("redeemReq", dto.getRequestJson());
        assertEquals("redeemRes", dto.getResponseJson());
    }

}
