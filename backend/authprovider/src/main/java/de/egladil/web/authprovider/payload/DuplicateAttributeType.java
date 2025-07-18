// =====================================================
// Project: authprovider
// (c) Heike Winkelvoß
// =====================================================
package de.egladil.web.authprovider.payload;

/**
 * DuplicateAttributeType
 */
public enum DuplicateAttributeType {

	EMAIL_AND_LOGINNAME(912, "BenutzerprofilResource.data.duplicate.emailAndLoginName"),
	EMAIL(910, "BenutzerprofilResource.data.duplicate.email"),
	LOGINNAME(911, "BenutzerprofilResource.data.duplicate.loginName");

	private final int detailedStatuscode;

	private final String applicationMessagesKey;

	private DuplicateAttributeType(final int detailedStatuscode, final String applicationMessagesKey) {

		this.detailedStatuscode = detailedStatuscode;
		this.applicationMessagesKey = applicationMessagesKey;
	}

	public int getDetailedStatuscode() {

		return detailedStatuscode;
	}

	public String getApplicationMessagesKey() {

		return applicationMessagesKey;
	}

}
