package com.femsa.oxxo.voucher.dto.voucherify.publications;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;

class ResponsePublicationsVoucherifyTest {


    @Test
    void testConstructorAndGetters() {
        ResponsePublications response = new ResponsePublications();
        OffsetDateTime start = OffsetDateTime.now().minusSeconds(2);
        OffsetDateTime end = OffsetDateTime.now();
        String request = "{\"type\":\"PUBLISH\"}";
        String responseStr = "{\"status\":\"PUBLISHED\"}";

        ResponsePublicationsVoucherify dto = new ResponsePublicationsVoucherify(
                response, start, end, request, responseStr);

        assertEquals(response, dto.getResponse());
        assertEquals(start, dto.getStartTime());
        assertEquals(end, dto.getEndTime());
        assertEquals(request, dto.getRequestJson());
        assertEquals(responseStr, dto.getResponseJson());
    }

    @Test
    void testSetters() {
        ResponsePublicationsVoucherify dto = new ResponsePublicationsVoucherify();
        OffsetDateTime now = OffsetDateTime.now();

        dto.setResponse(new ResponsePublications());
        dto.setStartTime(now);
        dto.setEndTime(now);
        dto.setRequestJson("jsonReq");
        dto.setResponseJson("jsonRes");

        assertNotNull(dto.getResponse());
        assertEquals("jsonReq", dto.getRequestJson());
        assertEquals("jsonRes", dto.getResponseJson());
    }
}
