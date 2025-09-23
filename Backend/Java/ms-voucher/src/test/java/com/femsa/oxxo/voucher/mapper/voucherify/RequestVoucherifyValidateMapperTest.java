package com.femsa.oxxo.voucher.mapper.voucherify;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.femsa.oxxo.voucher.config.Session;
import com.femsa.oxxo.voucher.config.VoucherifyProperties;
import com.femsa.oxxo.voucher.dto.ConsumerData;
import com.femsa.oxxo.voucher.dto.ConsumerDataRequestPublication;
import com.femsa.oxxo.voucher.dto.Item;
import com.femsa.oxxo.voucher.dto.Order;
import com.femsa.oxxo.voucher.dto.RequestPublicationVoucher;
import com.femsa.oxxo.voucher.dto.RequestRedeemVoucher;
import com.femsa.oxxo.voucher.dto.RequestValidateVoucher;
import com.femsa.oxxo.voucher.dto.voucherify.publications.RequestPublicationsVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.redemptions.RequestRedeemVoucherify;
import com.femsa.oxxo.voucher.dto.voucherify.validations.Redeemable;
import com.femsa.oxxo.voucher.dto.voucherify.validations.RequestValidationsVoucherify;

class RequestVoucherifyValidateMapperTest {

	private VoucherifyProperties properties;
	private RequestVoucherifyMapper mapper;

	@BeforeEach
	void setUp() {
		properties = new VoucherifyProperties();

		Session session = new Session();
		session.setType("LOCK");
		session.setTtl(2);
		session.setTtlUnit("MINUTES");

		properties.setObject("voucher");
		properties.setSession(session);

		mapper = new RequestVoucherifyMapper(properties);
	}

	@Test
	void testMapRequestValidate() {
		// Preparar datos de entrada
		ConsumerData consumerData = new ConsumerData();
		consumerData.setCrPlace("12345");
		consumerData.setCrStore("67890");
		consumerData.setCashRegister(1);

		Item item = new Item();
		item.setSkuId("1111");
		item.setRelatedObject("SKU");
		item.setQuantity(1);
		item.setAmount(100);
		item.setPrice(10.0);

		Order order = new Order();
		order.setItems(List.of(item));
		order.setStatus("PAID");
		order.setAmount(100);

		RequestValidateVoucher request = new RequestValidateVoucher();
		request.setCoupon("ABC123");
		request.setConsumerData(consumerData);
		request.setOrder(order);
		request.setCashier("CA01");
		request.setTicket(123);
		request.setTransactionId("TX123");
		request.setMemberId("MID123");

		// Ejecutar método
		RequestValidationsVoucherify result = mapper.mapRequestValidate(request);

		// Validar resultado
		assertNotNull(result);
		assertEquals("voucher", result.getRedeemables().get(0).getObject());
		assertEquals("ABC123", result.getRedeemables().get(0).getId());
		assertEquals("LOCK", result.getSession().getType());
		assertEquals(2, result.getSession().getTtl());
		assertEquals("MINUTES", result.getSession().getTtlUnit());
		assertEquals("TX123", result.getSession().getKey());

		assertEquals("12345", result.getMetadata().getRedemptionIdArea());
		assertEquals("67890", result.getMetadata().getRedemptionIdStore());
		assertEquals(1, result.getMetadata().getRedemptionCashRegister());
		assertEquals(123, result.getMetadata().getRedemptionTicket());
		assertEquals("CA01", result.getMetadata().getRedemptionCashier());
		assertEquals("TX123", result.getMetadata().getRedemptionTransactionId());
		assertEquals("MID123", result.getMetadata().getRedemptionMemberIdRedeemed());
	}

	@Test
	void testMapRequestRedeem() {
		// Configurar VoucherifyProperties
		VoucherifyProperties properties = new VoucherifyProperties();
		properties.setObject("voucher");

		RequestVoucherifyMapper mapper = new RequestVoucherifyMapper(properties);

		// Armar RequestRedeemVoucher
		ConsumerData consumerData = new ConsumerData();
		consumerData.setCrPlace("PL123");
		consumerData.setCrStore("ST456");
		consumerData.setCashRegister(3);

		Order order = new Order();
		order.setAmount(100);
		order.setStatus("VALIDO");

		Item item = new Item();
		item.setAmount(50);
		item.setPrice(25.0);
		item.setQuantity(2);
		item.setRelatedObject("product");
		item.setSkuId("123456");

		order.setItems(List.of(item));

		RequestRedeemVoucher request = new RequestRedeemVoucher();
		request.setConsumerData(consumerData);
		request.setOrder(order);
		request.setCoupon("ABC123");
		request.setTicket(456);
		request.setCashier("CASHIER01");
		request.setTransactionId("TXID9876");
		request.setMemberId("MEM001");

		// Ejecutar
		RequestRedeemVoucherify result = mapper.mapRequestRedeem(request);

		// Validar resultado
		assertNotNull(result);
		assertNotNull(result.getRedeemables());
		assertEquals(1, result.getRedeemables().size());

		Redeemable redeemable = result.getRedeemables().get(0);
		assertEquals("voucher", redeemable.getObject());
		assertEquals("ABC123", redeemable.getId());

		com.femsa.oxxo.voucher.dto.voucherify.redemptions.Metadata metadata = result.getMetadata();
		assertNotNull(metadata);
		assertEquals("PL123", metadata.getRedemptionIdArea());
		assertEquals("ST456", metadata.getRedemptionIdStore());
		assertEquals(3, metadata.getRedemptionCashRegister());
		assertEquals(456, metadata.getRedemptionTicket());
		assertEquals("CASHIER01", metadata.getRedemptionCashier());
		assertEquals("TXID9876", metadata.getRedemptionTransactionId());
		assertEquals("MEM001", metadata.getRedemptionMemberIdRedeemed());
	}

	@Test
	void testMapRequestPublication_success() {
		// Preparar datos de entrada
		ConsumerDataRequestPublication consumerData = new ConsumerDataRequestPublication();
		consumerData.setApplication("APP123");
		consumerData.setEntity("ENT");
		consumerData.setOrigin("ORIGIN");
		consumerData.setCrPlace("12345");
		consumerData.setCrStore("54321");
		consumerData.setCashRegister(1);

		RequestPublicationVoucher request = new RequestPublicationVoucher();
		request.setOperation("PUB");
		request.setConsumerData(consumerData);
		request.setDate("20250101");
		request.setHour("120000");
		request.setUser("tester");
		request.setMemberId("member123");
		request.setCampaign("CAMPAIGN2025");
		request.setCount(2);

		// Ejecutar método a probar
		RequestPublicationsVoucherify result = mapper.mapRequestPublication(request);

		// Validaciones
		assertNotNull(result);

		// Customer
		assertNotNull(result.getCustomer());
		assertEquals("member123", result.getCustomer().getSourceId());

		// Metadata
		assertNotNull(result.getMetadata());
		assertEquals("ENT", result.getMetadata().getPublicationCompany());
		assertEquals("APP123", result.getMetadata().getPublicationApplication());

		// Campaign
		assertNotNull(result.getCampaign());
		assertEquals("CAMPAIGN2025", result.getCampaign().getName());
		assertEquals(2, result.getCampaign().getCount());
	}

}
