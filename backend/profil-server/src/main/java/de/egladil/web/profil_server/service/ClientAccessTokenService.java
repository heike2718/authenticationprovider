// =====================================================
// Project: profil-server
// (c) Heike Winkelvoß
// =====================================================
package de.egladil.web.profil_server.service;

import java.util.Map;
import java.util.UUID;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientDefinitionException;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.egladil.web.commons_validation.payload.MessagePayload;
import de.egladil.web.commons_validation.payload.OAuthClientCredentials;
import de.egladil.web.commons_validation.payload.ResponsePayload;
import de.egladil.web.profil_server.error.ClientAuthException;
import de.egladil.web.profil_server.error.LogmessagePrefixes;
import de.egladil.web.profil_server.error.ProfilserverRuntimeException;
import de.egladil.web.profil_server.restclient.InitAccessTokenRestClient;

/**
 * ClientAccessTokenService
 */
@RequestScoped
public class ClientAccessTokenService {

	private static final Logger LOG = LoggerFactory.getLogger(ClientAccessTokenService.class);

	@ConfigProperty(name = "auth.client-id")
	String clientId;

	@ConfigProperty(name = "auth.client-secret")
	String clientSecret;

	@Inject
	@RestClient
	InitAccessTokenRestClient initAccessTokenClient;

	/**
	 * Holt ein accessToken vom authprovider
	 *
	 * @return String. null im Fehlerfall.
	 */
	public String orderAccessToken() {

		String nonce = UUID.randomUUID().toString();
		OAuthClientCredentials credentials = OAuthClientCredentials.create(clientId,
			clientSecret, nonce);

		Response authResponse = null;

		try {

			authResponse = initAccessTokenClient.authenticateClient(credentials);

			ResponsePayload responsePayload = authResponse.readEntity(ResponsePayload.class);

			evaluateResponse(nonce, responsePayload);

			@SuppressWarnings("unchecked")
			Map<String, String> dataMap = (Map<String, String>) responsePayload.getData();
			String accessToken = dataMap.get("accessToken");

			return accessToken;
		} catch (IllegalStateException | RestClientDefinitionException e) {

			LOG.error(e.getMessage(), e);
			throw new ProfilserverRuntimeException(
				"Unerwarteter Fehler beim Anfordern eines client-accessTokens: " + e.getMessage(),
				e);
		} catch (ClientAuthException e) {

			// ist schon geloggt
			return null;

		} catch (WebApplicationException e) {

			LOG.error(e.getMessage(), e);

			return null;
		} finally {

			credentials.clean();

			if (authResponse != null) {

				authResponse.close();
			}
		}

	}

	private void evaluateResponse(final String nonce, final ResponsePayload responsePayload) {

		MessagePayload messagePayload = responsePayload.getMessage();

		if (messagePayload.isOk()) {

			@SuppressWarnings("unchecked")
			Map<String, String> dataMap = (Map<String, String>) responsePayload.getData();
			String responseNonce = dataMap.get("nonce");

			if (!nonce.equals(responseNonce)) {

				{

					LOG.warn(LogmessagePrefixes.BOT + "zurückgesendetes nonce stimmt nicht");
					throw new ClientAuthException();
				}
			}
		} else {

			LOG.error("Authentisierung des Clients hat nicht geklappt: {} - {}", messagePayload.getLevel(),
				messagePayload.getMessage());
			throw new ClientAuthException();
		}

	}
}
