// =====================================================
// Project: profil-server
// (c) Heike Winkelvoß
// =====================================================
package de.egladil.web.profil_server.endpoints;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.egladil.web.commons_net.utils.CommonHttpUtils;
import de.egladil.web.commons_validation.ValidationDelegate;
import de.egladil.web.commons_validation.payload.MessagePayload;
import de.egladil.web.commons_validation.payload.OAuthClientCredentials;
import de.egladil.web.commons_validation.payload.ResponsePayload;
import de.egladil.web.profil_server.ProfilServerApp;
import de.egladil.web.profil_server.domain.AuthenticatedUser;
import de.egladil.web.profil_server.domain.User;
import de.egladil.web.profil_server.domain.UserSession;
import de.egladil.web.profil_server.error.AuthException;
import de.egladil.web.profil_server.error.LogmessagePrefixes;
import de.egladil.web.profil_server.error.ProfilserverRuntimeException;
import de.egladil.web.profil_server.error.PropagationFailedException;
import de.egladil.web.profil_server.event.ProfilEventHandler;
import de.egladil.web.profil_server.event.UserChanged;
import de.egladil.web.profil_server.event.UserDeleted;
import de.egladil.web.profil_server.payload.ChangeProfileDataPayload;
import de.egladil.web.profil_server.payload.ChangeProfilePasswordPayload;
import de.egladil.web.profil_server.payload.ProfileDataPayload;
import de.egladil.web.profil_server.payload.ProfilePasswordPayload;
import de.egladil.web.profil_server.payload.SelectProfilePayload;
import de.egladil.web.profil_server.restclient.ProfileRestClient;
import de.egladil.web.profil_server.service.AuthenticatedUserService;
import de.egladil.web.profil_server.service.ProfilSessionService;
import de.egladil.web.profil_server.service.UserService;

/**
 * ProfileResource
 */
@RequestScoped
@Path("profiles")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProfileResource {

	private static final Logger LOG = LoggerFactory.getLogger(ProfileResource.class);

	@ConfigProperty(name = "stage")
	String stage;

	@ConfigProperty(name = "auth.client-id")
	String clientId;

	@ConfigProperty(name = "auth.client-secret")
	String clientSecret;

	@Inject
	AuthenticatedUserService authentiatedUserService;

	@Inject
	UserService userService;

	@Inject
	@RestClient
	ProfileRestClient profileRestClient;

	@Inject
	ProfilSessionService profilSessionService;

	@Context
	SecurityContext securityContext;

	@Inject
	Event<UserChanged> userChangedEvent;

	@Inject
	Event<UserDeleted> userDeletedEvent;

	@Inject
	ProfilEventHandler eventHandler;

	private ValidationDelegate validationDelegate = new ValidationDelegate();

	@PUT
	@Path("/profile/password")
	@PermitAll
	public Response changePassword(final ProfilePasswordPayload payload) {

		UserSession userSession = getUserSession();

		NewCookie sessionCookie = authentiatedUserService.createSessionCookie(userSession.getSessionId());

		String expectedNonce = UUID.randomUUID().toString();
		OAuthClientCredentials clientCredentials = OAuthClientCredentials.create(clientId, clientSecret, expectedNonce);

		Response authProviderResponse = null;

		try {

			validationDelegate.check(payload, ProfilePasswordPayload.class);

			ChangeProfilePasswordPayload changePasswordPayload = ChangeProfilePasswordPayload.create(clientCredentials, payload,
				userSession.getUuid());

			authProviderResponse = profileRestClient.changePassword(changePasswordPayload);

			ResponsePayload responsePayload = authProviderResponse.readEntity(ResponsePayload.class);
			@SuppressWarnings("unchecked")
			Map<String, String> dataMap = (Map<String, String>) responsePayload.getData();
			String nonce = dataMap.get("nonce");

			if (!expectedNonce.equals(nonce)) {

				LOG.warn(LogmessagePrefixes.BOT + "angefragter Entdpoint hat das nonce geändert: expected={}, actual={}",
					expectedNonce, nonce);

				throw new ProfilserverRuntimeException("Der authprovider konnte nicht erreich werden");

			}

			if (authProviderResponse.getStatus() == 200) {

				User user = userService.getUser(userSession.getUuid());
				AuthenticatedUser responseData = authentiatedUserService.createAuthenticatedUser(userSession, user);

				ResponsePayload mappedResponsePayload = new ResponsePayload(responsePayload.getMessage(), responseData);

				LOG.info("idRef={} - UUID={}: Passwort geändert", getStringAbbreviated(userSession.getIdReference()),
					getStringAbbreviated(userSession.getUuid()));
				return Response.ok(mappedResponsePayload).cookie(sessionCookie).build();
			} else {

				LOG.error("authprovider antwortete mit dem Status {}", authProviderResponse.getStatus());
				throw new ProfilserverRuntimeException("Die Daten konnten wegen eines Serverfehlers nicht geändert werden.");

			}

		} catch (ProfilserverRuntimeException e) {

			LOG.error("idRef={} - UUID={}: {}", getStringAbbreviated(userSession.getIdReference()),
				getStringAbbreviated(userSession.getUuid()), e.getMessage());

			return createProfilServerRuntimeErrorResponse(userSession, sessionCookie, e);

		} catch (Exception e) {

			LOG.error("idRef={} - UUID={}: {}", getStringAbbreviated(userSession.getIdReference()),
				getStringAbbreviated(userSession.getUuid()), e.getMessage(), e);

			return createUnexpectedErrorResponse(userSession, sessionCookie);

		} finally {

			clientCredentials.clean();
			payload.clean();

			if (authProviderResponse != null) {

				authProviderResponse.close();
			}
		}
	}

	private Response createUnexpectedErrorResponse(final UserSession userSession, final NewCookie sessionCookie) {

		if (!ProfilServerApp.STAGE_DEV.equals(stage)) {

			userSession.clearSessionId();
		}

		AuthenticatedUser authenticatedUser = authentiatedUserService.createAuthenticatedUser(userSession, null);
		return Response.serverError()
			.entity(new ResponsePayload(
				MessagePayload.error("Es ist ein Fehler aufgetreten. Bitte senden Sie eine Mail an info@egladil.de"),
				authenticatedUser))
			.cookie(sessionCookie).build();
	}

	private Response createProfilServerRuntimeErrorResponse(final UserSession userSession, final NewCookie sessionCookie, final ProfilserverRuntimeException e) {

		if (!ProfilServerApp.STAGE_DEV.equals(stage)) {

			userSession.clearSessionId();
		}
		AuthenticatedUser authenticatedUser = authentiatedUserService.createAuthenticatedUser(userSession, null);

		return Response.serverError().entity(new ResponsePayload(MessagePayload.error(e.getMessage()), authenticatedUser))
			.cookie(sessionCookie).build();
	}

	private Response createPropagationFailedErrorResponse(final UserSession userSession, final NewCookie sessionCookie, final PropagationFailedException e) {

		if (!ProfilServerApp.STAGE_DEV.equals(stage)) {

			userSession.clearSessionId();
		}
		AuthenticatedUser authenticatedUser = authentiatedUserService.createAuthenticatedUser(userSession, null);

		return Response.serverError().entity(new ResponsePayload(MessagePayload.error(e.getMessage()), authenticatedUser))
			.cookie(sessionCookie).build();
	}

	/**
	 * @param  idRef
	 * @param  payload
	 * @return         ResponsePayload mit AuthenticatedUser als data.
	 */
	@PUT
	@Path("/profile/data")
	@PermitAll
	public Response changeData(final ProfileDataPayload payload) {

		UserSession userSession = getUserSession();

		NewCookie sessionCookie = authentiatedUserService.createSessionCookie(userSession.getSessionId());

		String expectedNonce = UUID.randomUUID().toString();
		OAuthClientCredentials clientCredentials = OAuthClientCredentials.create(clientId, clientSecret, expectedNonce);

		Response authProviderResponse = null;

		try {

			validationDelegate.check(payload, ProfileDataPayload.class);

			ChangeProfileDataPayload changeDataPayload = ChangeProfileDataPayload.create(clientCredentials, payload,
				userSession.getUuid());

			authProviderResponse = profileRestClient.changeData(changeDataPayload);

			LOG.debug("Response-Status={}", authProviderResponse.getStatus());

			ResponsePayload responsePayload = authProviderResponse.readEntity(ResponsePayload.class);
			@SuppressWarnings("unchecked")
			Map<String, String> dataMap = (Map<String, String>) responsePayload.getData();
			String nonce = dataMap.get("nonce");

			if (!expectedNonce.equals(nonce)) {

				LOG.warn(LogmessagePrefixes.BOT + "angefragter Entdpoint hat das nonce geändert: expected={}, actual={}",
					expectedNonce, nonce);

				throw new ProfilserverRuntimeException("Der authprovider konnte nicht erreich werden");

			}

			if (authProviderResponse.getStatus() == 200) {

				User user = new User();
				user.setEmail(dataMap.get("email"));
				user.setLoginName(dataMap.get("loginName"));
				user.setNachname(dataMap.get("nachname"));
				user.setVorname(dataMap.get("vorname"));

				AuthenticatedUser responseData = authentiatedUserService.createAuthenticatedUser(userSession, user);

				ResponsePayload mappedResponsePayload = new ResponsePayload(responsePayload.getMessage(), responseData);

				LOG.info("idRef={} - UUID={}: Daten geändert", getStringAbbreviated(userSession.getIdReference()),
					getStringAbbreviated(userSession.getUuid()));

				if (userChangedEvent != null) {

					UserChanged event = new UserChanged(securityContext.getUserPrincipal().getName(), user);

					if (eventHandler != null) {

						// synchron wegen der Fehlermeldung (ich weiß selbst, dass das total grottig ist. Muss mal vollständig
						// refactored werden
						eventHandler.handleSynchron(event);

					} else {

						userChangedEvent.fire(event);
					}

				}

				return Response.ok(mappedResponsePayload).cookie(sessionCookie).build();

			} else {

				LOG.error("idRef={} - UUID={}: authprovider antwortete mit dem Status {}",
					getStringAbbreviated(userSession.getIdReference()),
					getStringAbbreviated(userSession.getUuid()), authProviderResponse.getStatus());
				throw new ProfilserverRuntimeException("Die Daten konnten wegen eines Serverfehlers nicht geändert werden.");

			}

		} catch (PropagationFailedException e) {

			return createPropagationFailedErrorResponse(userSession, sessionCookie, e);

		} catch (ProfilserverRuntimeException e) {

			LOG.error("idRef={} - UUID={}: {}", getStringAbbreviated(userSession.getIdReference()),
				getStringAbbreviated(userSession.getUuid()), e.getMessage());

			return createProfilServerRuntimeErrorResponse(userSession, sessionCookie, e);

		} catch (Exception e) {

			LOG.error("idRef={} - UUID={}: {}", getStringAbbreviated(userSession.getIdReference()),
				getStringAbbreviated(userSession.getUuid()), e.getMessage(), e);

			return createUnexpectedErrorResponse(userSession, sessionCookie);

		} finally {

			clientCredentials.clean();

			if (authProviderResponse != null) {

				authProviderResponse.close();
			}
		}
	}

	@DELETE
	@Path("/profile")
	@PermitAll
	public Response deleteAccount() {

		UserSession userSession = getUserSession();

		String expectedNonce = UUID.randomUUID().toString();
		OAuthClientCredentials clientCredentials = OAuthClientCredentials.create(clientId, clientSecret, expectedNonce);

		Response authProviderResponse = null;

		try {

			SelectProfilePayload selectProfilePayload = SelectProfilePayload.create(clientCredentials, userSession.getUuid());

			authProviderResponse = profileRestClient.deleteProfile(selectProfilePayload);

			ResponsePayload responsePayload = authProviderResponse.readEntity(ResponsePayload.class);
			String nonce = (String) responsePayload.getData();

			if (!expectedNonce.equals(nonce)) {

				LOG.warn(LogmessagePrefixes.BOT + "angefragter Entdpoint hat das nonce geändert: expected={}, actual={}",
					expectedNonce, nonce);

				throw new ProfilserverRuntimeException("angefragter Entdpoint hat das nonce geändert");

			}

			if (authProviderResponse.getStatus() == 200) {

				LOG.info("User mit UUID={} gelöscht", userSession.getUuid());

				profilSessionService.invalidate(userSession.getSessionId());

				if (userDeletedEvent != null) {

					UserDeleted event = new UserDeleted(securityContext.getUserPrincipal().getName());

					if (eventHandler != null) {

						// synchron wegen der Fehlermeldung (ich weiß selbst, dass das total grottig ist. Muss mal vollständig
						// refactored werden
						eventHandler.handleSynchron(event);

					} else {

						userDeletedEvent.fire(event);
					}

				}

				return Response
					.ok(ResponsePayload.messageOnly(MessagePayload.info("Ihr Benutzerkonto wurde erfolgreich gelöscht.")))
					.cookie(CommonHttpUtils.createSessionInvalidatedCookie(ProfilServerApp.CLIENT_COOKIE_PREFIX)).build();

			} else {

				LOG.error("idRef={} - UUID={}: authprovider antwortete mit dem Status {}",
					getStringAbbreviated(userSession.getIdReference()),
					getStringAbbreviated(userSession.getUuid()), authProviderResponse.getStatus());
				throw new ProfilserverRuntimeException("Die Daten konnten wegen eines Serverfehlers nicht geändert werden.");

			}

		} catch (PropagationFailedException e) {

			NewCookie sessionCookie = authentiatedUserService.createSessionCookie(userSession.getSessionId());
			return createPropagationFailedErrorResponse(userSession, sessionCookie, e);

		} catch (ProfilserverRuntimeException e) {

			LOG.error("idRef={} - UUID={}: {}", getStringAbbreviated(userSession.getIdReference()),
				getStringAbbreviated(userSession.getUuid()), e.getMessage());

			NewCookie sessionCookie = authentiatedUserService.createSessionCookie(userSession.getSessionId());

			return createProfilServerRuntimeErrorResponse(userSession, sessionCookie, e);

		} catch (Exception e) {

			LOG.error("idRef={} - UUID={}: {}", getStringAbbreviated(userSession.getIdReference()),
				getStringAbbreviated(userSession.getUuid()), e.getMessage());

			NewCookie sessionCookie = authentiatedUserService.createSessionCookie(userSession.getSessionId());

			return createUnexpectedErrorResponse(userSession, sessionCookie);

		} finally {

			clientCredentials.clean();

			if (authProviderResponse != null) {

				authProviderResponse.close();
			}
		}
	}

	@GET
	@Path("/profile")
	@PermitAll
	public Response reloadUser() {

		UserSession userSession = getUserSession();

		User user = userService.getUser(userSession.getUuid());

		AuthenticatedUser responseData = authentiatedUserService.createAuthenticatedUser(userSession, user);

		ResponsePayload responsePayload = new ResponsePayload(MessagePayload.info("OK"), responseData);

		return Response.ok(responsePayload).build();
	}

	private UserSession getUserSession() {

		Principal principal = securityContext.getUserPrincipal();

		if (principal != null) {

			return (UserSession) principal;
		}

		LOG.error("keine UserSession für Principal vorhanden");
		throw new AuthException("Keine Berechtigung");
	}

	private String getStringAbbreviated(final String string) {

		return StringUtils.abbreviate(string, 11);
	}
}
