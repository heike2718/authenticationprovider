// =====================================================
// Project: authprovider
// (c) Heike Winkelvoß
// =====================================================
package de.egladil.web.authprovider.payload;

import java.util.List;

import de.egladil.web.auth_validations.annotations.UuidString;
import de.egladil.web.auth_validations.dto.OAuthClientCredentials;
import jakarta.validation.constraints.NotNull;

/**
 * UuidPayloadList
 */
public class UuidPayloadList {

	@UuidString
	private String adminUUID;

	@NotNull
	private OAuthClientCredentials clientCredentials;

	private List<String> uuids;

	public static UuidPayloadList createDefaultPayload(final OAuthClientCredentials clientCredentials, final List<String> uuids) {

		UuidPayloadList result = new UuidPayloadList();
		result.clientCredentials = clientCredentials;
		result.uuids = uuids;
		return result;
	}

	public static UuidPayloadList createAdminPayload(final String adminUUID, final OAuthClientCredentials clientCredentials,
		final List<String> uuids) {

		UuidPayloadList result = new UuidPayloadList();
		result.adminUUID = adminUUID;
		result.clientCredentials = clientCredentials;
		result.uuids = uuids;
		return result;
	}

	/**
	 *
	 */
	public UuidPayloadList() {

	}

	public List<String> getUuids() {

		return uuids;
	}

	public void setUuids(final List<String> uuids) {

		this.uuids = uuids;
	}

	public String getAdminUUID() {

		return adminUUID;
	}

	public OAuthClientCredentials getClientCredentials() {

		return clientCredentials;
	}

}
