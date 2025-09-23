package com.femsa.oxxo.voucher.repository;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.femsa.oxxo.voucher.entity.MsRespCode;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class MsRespCodeRepositoryTest {

	@Mock
	private MsRespCodeRepository msRespCodeRepository;

	@Test
	void findByMessageKeyIgnoreCase_shouldReturnResponseCode() {
		String messageKey = "INVALID_COUPON";
		String operation = "REDEEM";

		MsRespCode responseCode = new MsRespCode();
		responseCode.setId(100L);
		responseCode.setCodeVou(1001L);
		responseCode.setCodeHttpMs(400L);
		responseCode.setCodeMs(9001L);
		responseCode.setMessageKey(messageKey);
		responseCode.setMessagePos("Cupón inválido");
		responseCode.setOperation(operation);
		responseCode.setSeverity("HIGH");
		responseCode.setAction("Revisar cupón");

		when(msRespCodeRepository.findByMessageKeyIgnoreCase(messageKey, operation))
				.thenReturn(Mono.just(responseCode));

		StepVerifier.create(msRespCodeRepository.findByMessageKeyIgnoreCase(messageKey, operation)).expectNextMatches(
				resp -> resp.getMessageKey().equalsIgnoreCase(messageKey) && resp.getOperation().equals(operation))
				.verifyComplete();

		verify(msRespCodeRepository, times(1)).findByMessageKeyIgnoreCase(messageKey, operation);
	}
}
