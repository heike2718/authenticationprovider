// =====================================================
// Project: authprovider
// (c) Heike Winkelvoß
// =====================================================
package de.egladil.web.authprovider.service.confirm;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import de.egladil.web.authprovider.crypto.impl.CryptoService;
import de.egladil.web.authprovider.dao.ActivationCodeDao;
import de.egladil.web.authprovider.dao.ResourceOwnerDao;
import de.egladil.web.authprovider.entities.ActivationCode;
import de.egladil.web.authprovider.entities.ResourceOwner;
import de.egladil.web.authprovider.event.AuthproviderEvent;
import de.egladil.web.authprovider.event.AuthproviderEventType;
import de.egladil.web.authprovider.service.ResourceOwnerService;
import de.egladil.web.authprovider.utils.AuthTimeUtils;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

/**
 * ConfirmationServiceImplTest
 */
@QuarkusTest
public class ConfirmationServiceImplTest {

	@InjectMock
	private ActivationCodeDao activationCodeDao;

	@InjectMock
	private ResourceOwnerDao resourceOwnerDao;

	@InjectMock
	private ResourceOwnerService resourceOwnerService;

	@InjectMock
	private CryptoService cryptoService;

	@Inject
	private ConfirmationServiceImpl service;

	@Test
	void should_writeToEventLog_when_activationExpired() {

		// Arrange
		String confirmCode = "jkasgdk";
		String uuid = "ahkgd-eufwhgo";

		ActivationCode activationCode = new ActivationCode();
		activationCode.setConfirmationCode(confirmCode);
		activationCode.setConfirmed(false);
		activationCode.setExpirationTime(AuthTimeUtils.transformFromLocalDateTime(LocalDateTime.now().plusHours(-1)));

		ResourceOwner resourceOwner = new ResourceOwner();
		resourceOwner.setAktiviert(false);
		resourceOwner.setAnonym(false);
		resourceOwner.setEmail("mail@web.de");
		resourceOwner.setLoginName("mail@web.de");
		resourceOwner.setNachname("Log");
		resourceOwner.setRoles("STANDARD");
		resourceOwner.setUuid(uuid);
		resourceOwner.setVorname("Anna");

		activationCode.setResourceOwner(resourceOwner);

		Mockito.when(activationCodeDao.findByConfirmationCode(confirmCode)).thenReturn(Optional.of(activationCode));

		// Act
		ConfirmationStatus status = service.confirmCode(confirmCode);

		// Assert
		assertEquals(ConfirmationStatus.expiredActivation, status);
		AuthproviderEvent eventPayload = service.eventPayload();
		assertEquals(AuthproviderEventType.REGISTRATION_CONFIRMATION_EXPIRED, eventPayload.eventType());

	}

}
