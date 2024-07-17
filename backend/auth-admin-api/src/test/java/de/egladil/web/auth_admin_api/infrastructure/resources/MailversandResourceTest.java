// =====================================================
// Project: auth-admin-api
// (c) Heike Winkelvoß
// =====================================================
package de.egladil.web.auth_admin_api.infrastructure.resources;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import de.egladil.web.auth_admin_api.domain.Jobstatus;
import de.egladil.web.auth_admin_api.domain.auth.dto.MessagePayload;
import de.egladil.web.auth_admin_api.domain.mailversand.api.MailversandauftragOverview;
import de.egladil.web.auth_admin_api.domain.mailversand.api.MailversandauftragRequestDto;
import de.egladil.web.auth_admin_api.infrastructure.persistence.dao.MailversandDao;
import de.egladil.web.auth_admin_api.infrastructure.persistence.entities.PersistenterMailversandauftragReadOnly;
import de.egladil.web.auth_admin_api.profiles.AuthAdminTestProfile;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;

/**
 * MailversandResourceTest
 */
@QuarkusTest
@TestHTTPEndpoint(MailversandResource.class)
@TestProfile(AuthAdminTestProfile.class)
@TestMethodOrder(OrderAnnotation.class)
public class MailversandResourceTest {

	@Inject
	MailversandDao mailversandDao;

	@Test // - wollen die DB micht vollknallen
	@Order(1)
	@TestSecurity(user = "iche", roles = { "AUTH_ADMIN" })
	void testCVExceptionOnInsert() {

		//
		String idInfomailtext = "81188afe-abfa-4870-ae27-98259fdf36aa";
		String versandJahrMonat = DateTimeFormatter.ofPattern("yyyy-MM").format(LocalDateTime.now());

		List<PersistenterMailversandauftragReadOnly> mailversandauftraege = mailversandDao
			.findMailversandauftraegeWithInfomailtextAndJahrMonat(idInfomailtext, versandJahrMonat);

		if (mailversandauftraege.isEmpty()) {

			System.out.println("Legen neuen Mailversandauftrag für " + versandJahrMonat + " an");

			// Arrange: von 8 IDs sind 5 bestätigt
			MailversandauftragRequestDto requestPayload = new MailversandauftragRequestDto();
			requestPayload.setIdInfomailtext(idInfomailtext);
			requestPayload.setBenutzerUUIDs(Arrays.asList(new String[] {
				"b2e4faab-c89d-4431-b3e9-6e45faded2c4",
				"14a8fd8e-13d9-48bd-9f3f-e86be83ee871",
				"0835e24f-d238-4c65-a1ac-92cc50d17646",
				"a4395ae1-7de8-4a6e-8e8b-6d085025816f",
				"5eaaf0ed-4949-4ccd-a9c3-2db9af3559c2",
				"868d5890-d4d0-45ab-811d-4bfc081b48ec",
				"02a76d60-0aa0-4097-a55e-2ed6d5823088",
				"009fac43-1650-4a45-8ab8-30e646f935b3" }));

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
			assertEquals(Jobstatus.WAITING, responsePayload.getStatus());
			assertEquals(idInfomailtext, responsePayload.getIdInfomailtext());

		}

		{

			System.out.println("Legen newiten Mailversandauftrag für " + versandJahrMonat + " mit gleicher Empfängerliste an");

			// Arrange: von 8 IDs sind 5 bestätigt
			MailversandauftragRequestDto requestPayload = new MailversandauftragRequestDto();
			requestPayload.setIdInfomailtext(idInfomailtext);
			requestPayload.setBenutzerUUIDs(Arrays.asList(new String[] {
				"b2e4faab-c89d-4431-b3e9-6e45faded2c4",
				"14a8fd8e-13d9-48bd-9f3f-e86be83ee871",
				"0835e24f-d238-4c65-a1ac-92cc50d17646",
				"a4395ae1-7de8-4a6e-8e8b-6d085025816f",
				"5eaaf0ed-4949-4ccd-a9c3-2db9af3559c2",
				"868d5890-d4d0-45ab-811d-4bfc081b48ec",
				"02a76d60-0aa0-4097-a55e-2ed6d5823088",
				"009fac43-1650-4a45-8ab8-30e646f935b3" }));

			// Act
			MessagePayload responsePayload = given()
				.contentType(ContentType.JSON)
				.body(requestPayload)
				.post("auftraege")
				.then()
				.statusCode(409)
				.extract()
				.as(MessagePayload.class);

			// Assert
			assertEquals("WARN", responsePayload.getLevel());
			assertEquals("An diesen Benutzerkreis wurde in diesem Monat bereits eine Mail versendet.",
				responsePayload.getMessage());

		}

	}

	@Test // - wollen die DB micht vollknallen
	@Order(2)
	@TestSecurity(user = "iche", roles = { "AUTH_ADMIN" })
	void testLoad() {

		// Act
		MailversandauftragOverview[] responsePayload = given()
			.contentType(ContentType.JSON)
			.get("auftraege")
			.then()
			.statusCode(200)
			.extract()
			.as(MailversandauftragOverview[].class);

		// Assert
		assertTrue(responsePayload.length > 0);

	}

}
