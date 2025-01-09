// =====================================================
// Project: bv-admin
// (c) Heike Winkelvoß
// =====================================================
package de.egladil.web.bv_admin.domain.auth.util;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.egladil.web.bv_admin.domain.auth.config.AuthConstants;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.NewCookie;;

/**
 * CsrfCookieService
 */
@ApplicationScoped
public class CsrfCookieService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CsrfCookieService.class);

	@ConfigProperty(name = "cookies.secure")
	boolean cookiesSecure;

	@Inject
	SecureTokenService csrfTokenService;

	/**
	 * Erzeugt ein neues CsrfToken.
	 *
	 * @return
	 */
	public NewCookie createCsrfTokenCookie() {

		String csrfToken = csrfTokenService.createRandomToken().replaceAll("\"", "");

		LOGGER.debug("csrfToken={}", csrfToken);

		return new NewCookie.Builder(AuthConstants.CSRF_TOKEN_COOKIE_NAME).path("/").comment("csrf").maxAge(-1)
			.httpOnly(false).secure(cookiesSecure).build();
	}
}
