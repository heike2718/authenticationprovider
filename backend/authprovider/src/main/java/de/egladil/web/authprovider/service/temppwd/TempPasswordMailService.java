// =====================================================
// Project: authprovider
// (c) Heike Winkelvoß
// =====================================================
package de.egladil.web.authprovider.service.temppwd;

import java.util.Optional;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.egladil.web.authprovider.config.PasswordConfig;
import de.egladil.web.authprovider.dao.TempPasswordDao;
import de.egladil.web.authprovider.domain.TempPassword;
import de.egladil.web.authprovider.error.AuthRuntimeException;
import de.egladil.web.authprovider.service.AuthMailService;

/**
 * TempPasswordMailService
 */
@RequestScoped
public class TempPasswordMailService {

	private static final Logger LOG = LoggerFactory.getLogger(TempPasswordMailService.class);

	@Inject
	TempPasswordDao tempPasswordDao;

	@Inject
	AuthMailService mailService;

	@Inject
	PasswordConfig passwordConfig;

	@Context
	private UriInfo uriInfo;

	public void versendeTempPasswordMail(final String email, final TempPassword tempPassword) {

		SendTempPasswordTask task = new SendTempPasswordTask(email, tempPassword, mailService, passwordConfig.getTempPwdUrl());

		try {

			Boolean outcome = task.call();

			if (outcome) {

				persistSentStatusQuietly(tempPassword);
				LOG.debug("TempPassword.sent persisted");
			} else {

				LOG.warn("Beim Versenden der Mail ist ein Fehler aufgetreten");
			}
		} catch (Exception e) {

			throw new AuthRuntimeException("Senden der TempPassword-Mail konnte nicht beendet werden: " + e.getMessage(), e);
		}
	}

	private void persistSentStatusQuietly(final TempPassword tempPassword) {

		Optional<TempPassword> opt = tempPasswordDao.findByTokenId(tempPassword.getTokenId());

		if (opt.isPresent()) {

			TempPassword tpwd = opt.get();
			tpwd.setSent(true);

			try {

				tempPasswordDao.save(tpwd);
			} catch (Exception e) {

				LOG.warn("PersistenceException beim Speichern des Versendet-Status des {}. Wird ignoriert", tempPassword);
			}
		}
	}
}
