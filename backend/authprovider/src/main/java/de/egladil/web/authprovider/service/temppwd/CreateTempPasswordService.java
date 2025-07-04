// =====================================================
// Project: authprovider
// (c) Heike Winkelvoß
// =====================================================
package de.egladil.web.authprovider.service.temppwd;

import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.egladil.web.authprovider.config.PasswordConfig;
import de.egladil.web.authprovider.crypto.impl.CryptoService;
import de.egladil.web.authprovider.dao.ResourceOwnerDao;
import de.egladil.web.authprovider.dao.TempPasswordDao;
import de.egladil.web.authprovider.domain.TimeInterval;
import de.egladil.web.authprovider.entities.ResourceOwner;
import de.egladil.web.authprovider.entities.TempPassword;
import de.egladil.web.authprovider.utils.AuthTimeUtils;
import de.egladil.web.authprovider.utils.AuthUtils;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;

/**
 * TempPasswordService
 */
@RequestScoped
public class CreateTempPasswordService {

	private static final Logger LOG = LoggerFactory.getLogger(CreateTempPasswordService.class);

	@ConfigProperty(name = "tempPasswordExpireMinutes", defaultValue = "30")
	int tempPasswordExpireMinutes;

	@Inject
	TempPasswordDao tempPasswordDao;

	@Inject
	ResourceOwnerDao ressourceOwnerDao;

	@Inject
	TempPasswordMailService mailService;

	@Inject
	CryptoService cryptoService;

	@Inject
	PasswordConfig passwordConfig;

	/**
	 * Erzeugt eine TempPassword-Entity mit einem expireDate und versendet asynchron eine Mail an die gegebene
	 * Mailadresse, sofern diese gültig ist.
	 *
	 * @param payload OrderTempPasswordPayload
	 * @throws NotFoundException
	 */
	public void orderTempPassword(final String email) throws NotFoundException {

		if (email == null) {

			throw new IllegalArgumentException("email null");
		}

		Optional<ResourceOwner> optUser = ressourceOwnerDao.findByEmail(email);

		if (optUser.isEmpty()) {

			LOG.warn("Anforderung temporäres Passwort ungekannte Mailadresse '{}'", email);

			mailService.versendePasswortUnbekanntMail(email);

			return;
		}

		ResourceOwner resourceOwner = optUser.get();

		if (!resourceOwner.isAktiviert()) {

			LOG.warn("Anforderung temporäres Passwort nicht aktivierte Mailadresse '{}'", email);

			mailService.versendePasswortUnbekanntMail(email);

			return;
		}

		String password = cryptoService.generateRandomString(passwordConfig.getRandomAlgorithm(), passwordConfig.getTempPwdLength(),
			passwordConfig.getTempPwdCharPool().toCharArray());

		String tokenId = AuthUtils.newTokenId();

		int expirationMinutes = Integer.valueOf(tempPasswordExpireMinutes);
		TimeInterval timeInterval = AuthTimeUtils.getInterval(AuthTimeUtils.now(), expirationMinutes, ChronoUnit.MINUTES);

		TempPassword tempPassword = new TempPassword();
		tempPassword.setExpiresAt(timeInterval.getEndTime());
		tempPassword.setPassword(password);
		tempPassword.setTokenId(tokenId);
		tempPassword.setResourceOwner(optUser.get());

		TempPassword persisted = tempPasswordDao.save(tempPassword);
		mailService.versendeTempPasswordMail(email, persisted);

		LOG.debug("temp password ordered for {}", optUser.get());
	}
}
