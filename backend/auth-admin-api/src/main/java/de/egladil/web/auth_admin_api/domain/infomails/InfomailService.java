// =====================================================
// Project: auth-admin-api
// (c) Heike Winkelvoß
// =====================================================
package de.egladil.web.auth_admin_api.domain.infomails;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.egladil.web.auth_admin_api.domain.exceptions.AuthAdminAPIRuntimeException;
import de.egladil.web.auth_admin_api.infrastructure.persistence.dao.MailsUndVersandDao;
import de.egladil.web.auth_admin_api.infrastructure.persistence.entities.PersistenterInfomailText;
import de.egladil.web.auth_admin_api.infrastructure.persistence.entities.PersistenterInfomailTextReadOnly;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * InfomailService
 */
@ApplicationScoped
public class InfomailService {

	private static final Logger LOGGER = LoggerFactory.getLogger(InfomailService.class);

	@Inject
	MailsUndVersandDao dao;

	public List<InfomailResponseDto> loadInfomailTexte() {

		List<PersistenterInfomailTextReadOnly> trefferliste = dao.loadInfomailTexte();
		return trefferliste.stream().map(this::mapFromDB).toList();
	}

	/**
	 * Legt einen neuen InfomailText an.
	 *
	 * @param  requestPayload
	 * @return                InfomailResponseDto
	 */
	public InfomailResponseDto infomailAnlegen(final InfomailRequestDto requestPayload) {

		PersistenterInfomailText persistenterInfomailText = new PersistenterInfomailText();
		persistenterInfomailText.setBetreff(requestPayload.getBetreff());
		persistenterInfomailText.setMailtext(requestPayload.getMailtext());

		String uuid = doSave(persistenterInfomailText);

		PersistenterInfomailTextReadOnly result = dao.findInfomailtextReadOnlyByID(uuid);

		if (result == null) {

			LOGGER.error("extrem unwahrscheinlich: gerade erst angelegt und schon nicht mehr gefunden: uuid={}", uuid);
			throw new AuthAdminAPIRuntimeException("extrem unwahrscheinlich: gerade erst angelegt und schon nicht mehr gefunden");
		}

		return mapFromDB(result);
	}

	/**
	 * Ändert einen vorhandenen Infomailtext.
	 *
	 * @param  uuid
	 * @param  requestPayload
	 * @return                UpdateInfomailResponseDto
	 */
	public UpdateInfomailResponseDto infomailAendern(final String uuid, final InfomailRequestDto requestPayload) {

		PersistenterInfomailText ausDB = dao.findInfomailtextByID(uuid);

		if (ausDB == null) {

			LOGGER.warn("kein INFOMAIL_TEXT mit uuid={} vorhanden", uuid);
			return new UpdateInfomailResponseDto().withUuid(uuid);
		}

		ausDB.setBetreff(requestPayload.getBetreff());
		ausDB.setMailtext(requestPayload.getMailtext());

		doSave(ausDB);

		PersistenterInfomailTextReadOnly result = dao.findInfomailtextReadOnlyByID(uuid);

		if (result == null) {

			LOGGER.error("extrem unwahrscheinlich: gerade erst geaendert und schon nicht mehr gefunden: uuid={}", uuid);
			throw new AuthAdminAPIRuntimeException("extrem unwahrscheinlich: gerade erst geaendert und schon nicht mehr gefunden");
		}

		return new UpdateInfomailResponseDto().withUuid(uuid).withInfomail(mapFromDB(result));

	}

	@Transactional
	String doSave(final PersistenterInfomailText persistenterInfomailText) {

		return dao.saveInfomailText(persistenterInfomailText);
	}

	InfomailResponseDto mapFromDB(final PersistenterInfomailTextReadOnly fromDB) {

		InfomailResponseDto result = new InfomailResponseDto();
		result.setUuid(fromDB.uuid);
		result.setBetreff(fromDB.betreff);
		result.setMailtext(fromDB.mailtext);

		if (fromDB.uuidsMailversandauftraege != null) {

			result.setUuidsMailversandauftraege(Arrays.asList(StringUtils.split(fromDB.uuidsMailversandauftraege, ",")));
		}

		return result;

	}

}
