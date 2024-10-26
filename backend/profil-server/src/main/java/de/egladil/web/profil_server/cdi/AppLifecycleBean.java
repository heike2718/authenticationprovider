// =====================================================
// Project: mk-gateway
// (c) Heike Winkelvoß
// =====================================================
package de.egladil.web.profil_server.cdi;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

/**
 * AppLifecycleBean
 */
@ApplicationScoped
public class AppLifecycleBean {

	private static final Logger LOGGER = LoggerFactory.getLogger(AppLifecycleBean.class);

	@ConfigProperty(name = "quarkus.http.cors.origins", defaultValue = "")
	String corsAllowedOrigins;

	@ConfigProperty(name = "quarkus.rest-client.mk-gateway.url")
	String mkGatewayRoute;

	@ConfigProperty(name = "quarkus.rest-client.accesstoken.url")
	String accessTokenRoute;

	@ConfigProperty(name = "quarkus.rest-client.token-exchange.url")
	String tokenExchangeRoute;

	@ConfigProperty(name = "quarkus.rest-client.profile.url")
	String profileRoute;

	@ConfigProperty(name = "auth.redirect-url.login")
	String redirectUrl;

	@ConfigProperty(name = "quarkus.http.port")
	String port;

	void onStartup(@Observes final StartupEvent ev) {

		LOGGER.info(" ===========>  quarkus.http.cors.origins={}", corsAllowedOrigins);
		LOGGER.info(" ===========>  mkGatewayRoute={}", mkGatewayRoute);
		LOGGER.info(" ===========>  accessTokenRoute={}", accessTokenRoute);
		LOGGER.info(" ===========>  tokenExchangeRoute={}", tokenExchangeRoute);
		LOGGER.info(" ===========>  redirectUrl={}", redirectUrl);
		LOGGER.info(" ===========>  profileRoute={}", profileRoute);
		LOGGER.info(" ===========>  profil-server-port={}", port);
	}

}
