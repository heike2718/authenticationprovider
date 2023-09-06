// =====================================================
// Project: profil-server
// (c) Heike Winkelvoß
// =====================================================
package de.egladil.web.profil_server.endpoints;

import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.egladil.web.commons_net.utils.CommonHttpUtils;
import de.egladil.web.commons_validation.payload.MessagePayload;
import de.egladil.web.commons_validation.payload.ResponsePayload;
import de.egladil.web.profil_server.ProfilServerApp;
import de.egladil.web.profil_server.domain.AuthenticatedUser;
import de.egladil.web.profil_server.domain.User;
import de.egladil.web.profil_server.domain.UserSession;
import de.egladil.web.profil_server.error.AuthException;
import de.egladil.web.profil_server.service.AuthenticatedUserService;
import de.egladil.web.profil_server.service.ClientAccessTokenService;
import de.egladil.web.profil_server.service.ProfilSessionService;
import de.egladil.web.profil_server.service.TokenExchangeService;
import de.egladil.web.profil_server.service.UserService;

/**
 * ProfilSessionResource
 */
@RequestScoped
@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
public class ProfilSessionResource {

	private static final Logger LOG = LoggerFactory.getLogger(ProfilSessionResource.class);

	@ConfigProperty(name = "auth-app.url")
	String authAppUrl;

	@ConfigProperty(name = "auth.redirect-url.login")
	String loginRedirectUrl;

	@ConfigProperty(name = "stage")
	String stage;

	@Inject
	AuthenticatedUserService authenticatedUserService;

	@Inject
	ClientAccessTokenService clientAccessTokenService;

	@Inject
	ProfilSessionService profilSessionService;

	@Inject
	UserService userService;

	@Inject
	TokenExchangeService tokenExchangeService;

	@GET
	@Path("/login")
	@PermitAll
	public Response getLoginUrl() {

		String accessToken = clientAccessTokenService.orderAccessToken();

		if (StringUtils.isBlank(accessToken)) {

			return Response.serverError().entity("Fehler beim Authentisieren des Clients").build();
		}

		String redirectUrl = authAppUrl + "#/login?accessToken=" + accessToken + "&state=login&nonce=null&redirectUrl="
			+ loginRedirectUrl;

		LOG.debug(redirectUrl);

		return Response.ok(ResponsePayload.messageOnly(MessagePayload.info(redirectUrl))).build();
	}

	@POST
	@Path("/session")
	@Consumes(MediaType.TEXT_PLAIN)
	@PermitAll
	public Response getTheJwtAndCreateSession(final String oneTimeToken) {

		LOG.info("onieTimeToken=,{}", oneTimeToken);

		String jwt = tokenExchangeService.exchangeTheOneTimeToken(oneTimeToken);

		return this.createTheSessionWithJWT(jwt);
	}

	private Response createTheSessionWithJWT(final String jwt) {

		UserSession userSession = profilSessionService.createUserSession(jwt);
		NewCookie sessionCookie = authenticatedUserService.createSessionCookie(userSession.getSessionId());

		User user = userService.getUser(userSession.getUuid());
		AuthenticatedUser authUser = authenticatedUserService.createAuthenticatedUser(userSession, user);

		ResponsePayload payload = new ResponsePayload(MessagePayload.info("OK"), authUser);
		return Response.ok(payload).cookie(sessionCookie).build();
	}

	@DELETE
	@Path("/logout")
	@PermitAll
	public Response logout(@CookieParam(
		value = ProfilServerApp.CLIENT_COOKIE_PREFIX + CommonHttpUtils.NAME_SESSIONID_COOKIE) final String sessionId) {

		if (sessionId != null) {

			profilSessionService.invalidate(sessionId);
		}

		return Response.ok(ResponsePayload.messageOnly(MessagePayload.info("Sie haben sich erfolreich ausgeloggt")))
			.cookie(CommonHttpUtils.createSessionInvalidatedCookie(ProfilServerApp.CLIENT_COOKIE_PREFIX)).build();

	}

	@DELETE
	@Path("/dev/logout/{sessionid}")
	@PermitAll
	public Response logoutDev(@PathParam(value = "sessionid") final String sessionId) {

		if (!ProfilServerApp.STAGE_DEV.equals(stage)) {

			throw new AuthException("Diese URL darf nur im DEV-Mode aufgerufen werden");
		}

		profilSessionService.invalidate(sessionId);

		return Response.ok(ResponsePayload.messageOnly(MessagePayload.info("Sie haben sich erfolreich ausgeloggt"))).build();
	}
}
