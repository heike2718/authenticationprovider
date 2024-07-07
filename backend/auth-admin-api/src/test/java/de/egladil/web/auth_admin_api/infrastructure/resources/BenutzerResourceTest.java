// =====================================================
// Project: auth-admin-api
// (c) Heike Winkelvoß
// =====================================================
package de.egladil.web.auth_admin_api.infrastructure.resources;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import de.egladil.web.auth_admin_api.domain.benutzer.BenutzerSearchResult;
import de.egladil.web.auth_admin_api.domain.benutzer.BenutzerSuchparameter;
import de.egladil.web.auth_admin_api.domain.benutzer.BenutzerTrefferlisteItem;
import de.egladil.web.auth_admin_api.domain.validation.ValidationErrorResponseDto;
import de.egladil.web.auth_admin_api.profiles.AuthAdminTestProfile;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;

/**
 * BenutzerResourceTest
 */
@QuarkusTest
@TestHTTPEndpoint(BenutzerResource.class)
@TestProfile(AuthAdminTestProfile.class)
public class BenutzerResourceTest {

	@Test
	@TestSecurity(user = "iche", roles = { "AUTH_ADMIN" })
	void should_validateInput() {

		BenutzerSuchparameter dto = new BenutzerSuchparameter();
		dto.setDateModified("abscefg:hijklmn:stuv");
		dto.setEmail(
			"Rainer Rainer Rainer Rainer Rainer Rainer Rainer Rainer Rainer Rainer Rainer Γεια σας Rainer Rainerja Rainer Rainer Rainer Rainer Rainer Rainer Rainer Rainer Rainer Rainer Rainer Γεια σας Rainer Rainerja Rainer Rainer Rainer Rainer Rainer Rainer Rainer Rai");

		dto.setNachname("Rainer Rainer Rainer Rainer Rainer Rainer Rainer Rainer Rainer Rainer Rainer Γεια σας Rainer Rainerja");
		dto.setRolle(
			"STANDARD und ADMIN STANDARD und ADMIN STANDARD und ADMIN STANDARD und ADMIN STANDARD und ADMIN STANDARD und ADMIN STANDARD und ADMIN STANDARD und ADMIN");
		dto.setUuid("732b2ed8xy-732b2ed8-732b2ed8-732b2ed823");
		dto.setVorname("Rainer Rainer Rainer Rainer Rainer Rainer Rainer Rainer Rainer Rainer Rainer Γεια σας Rainer Rainerja");

		ValidationErrorResponseDto[] responsePayload = given()
			.contentType(ContentType.JSON)
			.body(dto)
			.post("")
			.then()
			.statusCode(400)
			.extract()
			.as(ValidationErrorResponseDto[].class);

		assertEquals(6, responsePayload.length);

	}

	@Test
	@TestSecurity(user = "iche", roles = { "AUTH_ADMIN" })
	void should_returnTheResultList() {

		BenutzerSuchparameter dto = new BenutzerSuchparameter();
		dto.setUuid("7");
		dto.setSortByLabelname("email");
		dto.setPageIndex(2);
		dto.setPageSize(11);

		BenutzerSearchResult responsePayload = given()
			.contentType(ContentType.JSON)
			.body(dto)
			.post("")
			.then()
			.statusCode(200)
			.extract()
			.as(BenutzerSearchResult.class);

		assertTrue(responsePayload.getAnzahlGesamt() > 11);
		List<BenutzerTrefferlisteItem> items = responsePayload.getItems();
		assertEquals(11, items.size());

		long anzahlMit7 = items.stream().filter(i -> i.getUuid().contains("7")).count();
		assertEquals(11, anzahlMit7);

	}

	@Test
	@TestSecurity(user = "iche", roles = { "AUTH_ADMIN" })
	void should_excludeAdminsFromResultList() {

		BenutzerSuchparameter dto = new BenutzerSuchparameter();
		dto.setVorname("checki");
		dto.setPageIndex(1);
		dto.setPageSize(50);

		BenutzerSearchResult responsePayload = given()
			.contentType(ContentType.JSON)
			.body(dto)
			.post("")
			.then()
			.statusCode(200)
			.extract()
			.as(BenutzerSearchResult.class);

		assertEquals(0, responsePayload.getAnzahlGesamt());
		List<BenutzerTrefferlisteItem> items = responsePayload.getItems();
		assertEquals(0, items.size());
	}
}
