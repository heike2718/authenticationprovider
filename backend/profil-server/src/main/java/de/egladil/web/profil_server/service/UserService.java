// =====================================================
// Project: profil-server
// (c) Heike Winkelvoß
// =====================================================
package de.egladil.web.profil_server.service;

import java.util.Map;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.egladil.web.commons_validation.payload.MessagePayload;
import de.egladil.web.commons_validation.payload.OAuthClientCredentials;
import de.egladil.web.commons_validation.payload.ResponsePayload;
import de.egladil.web.profil_server.domain.User;
import de.egladil.web.profil_server.error.ProfilserverRuntimeException;
import de.egladil.web.profil_server.payload.SelectProfilePayload;
import de.egladil.web.profil_server.restclient.ProfileRestClient;

/**
 * UserService
 */
@RequestScoped
public class UserService {

	private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

	@ConfigProperty(name = "auth.client-id")
	String clientId;

	@ConfigProperty(name = "auth.client-secret")
	String clientSecret;

	@Inject
	@RestClient
	ProfileRestClient profileRestClient;

	/**
	 * REST-API- Aufruf, um die Userdaten zu holen.
	 *
	 * @param  uuid
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "resource" })
	public User getUser(final String uuid) {

		OAuthClientCredentials credentials = OAuthClientCredentials.create(clientId,
			clientSecret, null);

		Response response = null;

		try {

			SelectProfilePayload selectPayload = SelectProfilePayload.create(credentials, uuid);
			response = profileRestClient.getUserProfile(selectPayload);

			LOG.debug("UserService === (3) ===");

			ResponsePayload payload = response.readEntity(ResponsePayload.class);
			MessagePayload messagePayload = payload.getMessage();

			if (messagePayload.isOk()) {

				Map<String, String> data = (Map<String, String>) payload.getData();

				User user = new User();
				user.setEmail(data.get("email"));
				user.setLoginName(data.get("loginName"));
				user.setNachname(data.get("nachname"));
				user.setVorname(data.get("vorname"));

				return user;
			}

			return null;
		} catch (WebApplicationException e) {

			response = e.getResponse();

			int status = response.getStatus();

			if (status == 401) {

				LOG.error("Authentisierungsfehler für Client {} gegenüber dem authprovider", StringUtils.abbreviate(clientId, 11));
				throw new ProfilserverRuntimeException("Authentisierungsfehler für Client");
			}

			LOG.error("Statuscode {} beim Holen des Users", status);

			throw new ProfilserverRuntimeException("Unerwarteter Response-Status beim Holen des Users.");

		} catch (Exception e) {

			LOG.error(e.getMessage(), e);

			throw new ProfilserverRuntimeException("Fehler bei Anfrage des authproviders: " + e.getMessage(), e);

		} finally {

			credentials.clean();

			if (response != null) {

				response.close();
			}
		}
	}
}
