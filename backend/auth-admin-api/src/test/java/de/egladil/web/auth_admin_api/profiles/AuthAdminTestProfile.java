// =====================================================
// Project: mja-api
// (c) Heike Winkelvoß
// =====================================================
package de.egladil.web.auth_admin_api.profiles;

import java.util.HashMap;
import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;

/**
 * AuthAdminTestProfile
 */
public class AuthAdminTestProfile implements QuarkusTestProfile {

	@Override
	public Map<String, String> getConfigOverrides() {

		Map<String, String> configOverrides = new HashMap<>();
		configOverrides.put("csrf.enabled", "false");
		configOverrides.put("mock.session", "true");
		configOverrides.put("mock.benutzerid", "20721575-8c45-4201-a025-7a9fece1f2aa");
		configOverrides.put("mock.benutzer.fullname", "Moki Bulruk");
		configOverrides.put("mock.role", "AUTH_ADMIN");
		configOverrides.put("quarkus.http.test.timeout", "600S");
		configOverrides.put("quarkus.http.test.timeout", "600S");
		configOverrides.put("quarkus.mailer.mock", "true");
		configOverrides.put("quarkus.datasource.jdbc.url", "jdbc:mariadb://172.26.0.2:3306/authbv");

		System.out.println("config overridden:");

		return configOverrides;
	}

}
