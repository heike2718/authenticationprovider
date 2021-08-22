// =====================================================
// Projekt: authprovider
// (c) Heike Winkelvoß
// =====================================================

package de.egladil.web.authprovider.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.PersistenceException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.egladil.web.authprovider.api.ClientInformation;
import de.egladil.web.authprovider.crypto.AuthCryptoService;
import de.egladil.web.authprovider.dao.ClientDao;
import de.egladil.web.authprovider.domain.Client;
import de.egladil.web.authprovider.domain.ClientAccessToken;
import de.egladil.web.authprovider.error.AuthRuntimeException;
import de.egladil.web.authprovider.error.ClientAccessTokenNotFoundException;
import de.egladil.web.authprovider.error.ClientAuthException;
import de.egladil.web.authprovider.error.InvalidRedirectUrl;
import de.egladil.web.authprovider.error.LogmessagePrefixes;
import de.egladil.web.authprovider.payload.ClientCredentials;
import de.egladil.web.authprovider.payload.OAuthAccessTokenPayload;
import de.egladil.web.authprovider.utils.AuthUtils;
import de.egladil.web.commons_net.exception.SessionExpiredException;
import de.egladil.web.commons_net.time.CommonTimeUtils;
import de.egladil.web.commons_validation.payload.OAuthClientCredentials;

/**
 * ClientService
 */
@ApplicationScoped
public class ClientService {

	private static final Logger LOG = LoggerFactory.getLogger(ClientService.class);

	private Map<String, ClientAccessToken> clientAccessTokens = new ConcurrentHashMap<>();

	@Inject
	ClientDao clientDao;

	@Inject
	AuthCryptoService authCryptoService;

	/**
	 *
	 */
	public ClientService() {

	}

	/**
	 * @param clientDao
	 * @param clientAccessTokenDao
	 * @param authCryptoService
	 */
	public ClientService(final ClientDao clientDao, final AuthCryptoService authCryptoService) {

		this.clientDao = clientDao;
		this.authCryptoService = authCryptoService;
	}

	/**
	 * @param  clientId
	 * @return
	 * @throws ClientAccessTokenNotFoundException
	 * @throws InvalidRedirectUrl
	 */
	public ClientInformation getClientInformation(final ClientCredentials clientCredentials) throws AuthRuntimeException, InvalidRedirectUrl, ClientAccessTokenNotFoundException {

		Client client = this.findAndCheckClient(clientCredentials);

		ClientInformation data = ClientInformation.fromClient(client);
		data.setState(clientCredentials.getState());

		return data;
	}

	/**
	 * Authentisiert den Client.
	 *
	 * @param  clientCredentials
	 * @return                   OAuthAccessTokenPayload
	 */
	public OAuthAccessTokenPayload createClientAccessToken(final OAuthClientCredentials clientCredentials) throws ClientAuthException, PersistenceException {

		LOG.info("Client-ID=" + clientCredentials.getClientId());

		Client client = this.authorizeClient(clientCredentials);

		OAuthAccessTokenPayload result = createNewAccessToken(client);

		return result;

	}

	public Client authorizeClient(final OAuthClientCredentials clientCredentials) throws ClientAuthException {

		Client client = clientDao.findByClientId(clientCredentials.getClientId());

		if (client == null) {

			LOG.warn(LogmessagePrefixes.BOT + "kein Client mit ClientID={} bekannt", clientCredentials.getClientId());
			throw new ClientAuthException("Client mit dieser ClientID unbekannt.");
		}

		authCryptoService.verifyClientSecret(clientCredentials.getClientSecret().toCharArray(), client);

		return client;
	}

	private void removeExpiredClientAccessTokens() {

		Set<String> keys = Collections.unmodifiableSet(clientAccessTokens.keySet());

		keys.forEach(key -> {

			ClientAccessToken cat = clientAccessTokens.get(key);

			if (cat != null) {

				LocalDateTime expireDateTime = CommonTimeUtils.transformFromDate(cat.getAccessTokenExpiresAt());
				LocalDateTime nowMinusHours = CommonTimeUtils.now().minusHours(2);

				if (expireDateTime.isBefore(nowMinusHours)) {

					clientAccessTokens.remove(key);
					LOG.debug("expired ClientAccessToken {} removed", StringUtils.abbreviate(key, 11));
				}
			} else {

				clientAccessTokens.remove(key);
			}
		});
	}

	/**
	 * Erzeugt für den gegebenen Client ein neues AccessToken und speichert dies in der DB.
	 *
	 * @param  client
	 *                Client
	 * @return        ClientAccessToken das gespeicherte Token
	 */
	private OAuthAccessTokenPayload createNewAccessToken(final Client client) {

		ClientAccessToken clientAccessToken = new ClientAccessToken();

		String accessTokenId = AuthUtils.newTokenId();
		clientAccessToken.setAccessToken(accessTokenId);

		clientAccessToken
			.setAccessTokenExpiresAt(CommonTimeUtils.getInterval(CommonTimeUtils.now(), client.getAccessTokenExpirationMinutes(),
				ChronoUnit.MINUTES).getEndTime());

		clientAccessToken.setClientId(client.getClientId());

		clientAccessTokens.put(accessTokenId, clientAccessToken);

		OAuthAccessTokenPayload result = new OAuthAccessTokenPayload();
		result.setAccessToken(clientAccessToken.getAccessToken());
		result.setExpiresAt(clientAccessToken.getAccessTokenExpiresAt().getTime());

		return result;
	}

	/**
	 * Identifiziert den Client anhand seines accessTokens und validiert ihn.
	 *
	 * @param  clientCredentials
	 * @return
	 * @throws AuthRuntimeException
	 * @throws InvalidRedirectUrl
	 */
	public Client findAndCheckClient(final ClientCredentials clientCredentials) throws ClientAccessTokenNotFoundException, InvalidRedirectUrl {

		LOG.info("{}", clientCredentials);

		removeExpiredClientAccessTokens();

		String accessTokenId = clientCredentials.getAccessToken();

		ClientAccessToken clientAccessToken = clientAccessTokens.get(accessTokenId);

		if (clientAccessToken == null) {

			String msg = "Kein ClientAccessToken mit ID='" + accessTokenId + "' vorhanden";
			LOG.error(msg);
			throw new SessionExpiredException("Das ClientAccessToken ist abgelaufen. Bitte aktualisieren Sie Ihren Browser.");
		}

		LocalDateTime expireDateTime = CommonTimeUtils.transformFromDate(clientAccessToken.getAccessTokenExpiresAt());
		LocalDateTime now = CommonTimeUtils.now();

		if (now.isAfter(expireDateTime)) {

			clientAccessTokens.remove(accessTokenId);
			throw new SessionExpiredException("Das ClientAccessToken ist abgelaufen. Bitte aktualisieren Sie Ihren Browser.");
		}

		final String clientId = clientAccessToken.getClientId();

		Client client = clientDao.findByClientId(clientId);

		if (client != null) {

			checkRedirectUrl(client.getRedirectUrls(), clientCredentials.getRedirectUrl());

			return client;
		}

		String msg = "ClientAccessToken " + accessTokenId + " hat keinen Client";
		LOG.error(msg);
		throw new AuthRuntimeException(msg);
	}

	void checkRedirectUrl(final String redirectUrls, final String redirectUrl) throws InvalidRedirectUrl {

		String[] allowedRedirectUrls = StringUtils.split(redirectUrls, ',');
		final String testString = this.stripProtokollAndTailingSlash(redirectUrl);

		LOG.debug("suchen redirectUrls mit testString={}", testString);

		Optional<String> optUrl = Arrays.stream(allowedRedirectUrls).filter(url -> url.equals(testString)).findFirst();

		if (!optUrl.isPresent()) {

			LOG.warn("Possible BOT Attack: redirect url '{}' fehlt in clients.REDIRECT_URLS (führendes http:// wird ignoriert)",
				redirectUrl);
			throw new InvalidRedirectUrl();
		} else {

			LOG.debug("redirectUrl valid");
		}
	}

	private String stripProtokollAndTailingSlash(final String redirectUrl) {

		String testString = redirectUrl.replace("http://", "");
		testString = testString.replace("https://", "");

		if (testString.endsWith("/")) {

			testString = testString.substring(0, testString.length() - 1);
		}
		return testString;
	}
}
