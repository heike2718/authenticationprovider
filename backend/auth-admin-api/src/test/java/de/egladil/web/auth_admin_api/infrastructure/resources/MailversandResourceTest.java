// =====================================================
// Project: auth-admin-api
// (c) Heike Winkelvoß
// =====================================================
package de.egladil.web.auth_admin_api.infrastructure.resources;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;

import de.egladil.web.auth_admin_api.domain.Jobstatus;
import de.egladil.web.auth_admin_api.domain.mailversand.MailversandauftragOverview;
import de.egladil.web.auth_admin_api.domain.mailversand.MailversandauftragRequestDto;
import de.egladil.web.auth_admin_api.profiles.AuthAdminTestProfile;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;

/**
 * MailversandResourceTest
 */
@QuarkusTest
@TestHTTPEndpoint(MailversandResource.class)
@TestProfile(AuthAdminTestProfile.class)
@TestMethodOrder(OrderAnnotation.class)
public class MailversandResourceTest {

	// @Test // - wollen die DB micht vollknallen
	@Order(1)
	@TestSecurity(user = "iche", roles = { "AUTH_ADMIN" })
	void testInsert() {

		// Arrange: von 8 IDs sind 5 bestätigt
		MailversandauftragRequestDto requestPayload = new MailversandauftragRequestDto();
		String idInfomailtext = "d67e7bc6-3de7-4348-aa3d-174da70fa177";
		requestPayload.setIdInfomailtext(idInfomailtext);
		requestPayload.setBenutzerIds(Arrays.asList(new Long[] { 76L, 22L, 20L, 18L, 39L, 19L, 7L, 13L }));

		// Act
		MailversandauftragOverview responsePayload = given()
			.contentType(ContentType.JSON)
			.body(requestPayload)
			.post("auftraege")
			.then()
			.statusCode(201)
			.extract()
			.as(MailversandauftragOverview.class);

		// Assert
		assertNotNull(responsePayload.getUuid());
		assertFalse(StringUtils.isBlank(responsePayload.getBetreff()));
		assertEquals(5L, responsePayload.getAnzahlEmpfaenger());
		assertEquals(3, responsePayload.getAnzahlGruppen());
		assertEquals(Jobstatus.NEW, responsePayload.getStatus());
		assertEquals(idInfomailtext, responsePayload.getIdInfomailtext());

	}

}
