// =====================================================
// Project: profil-server
// (c) Heike Winkelvoß
// =====================================================
package de.egladil.web.bv_admin.infrastructure.restclient;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * SyncHandshake
 */
public class SyncHandshake {

	@JsonProperty
	private String sendingClientId;

	@JsonProperty
	private String nonce;

	SyncHandshake() {

	}

	public SyncHandshake(final String sendingClientId, final String nonce) {

		this.sendingClientId = sendingClientId;
		this.nonce = nonce;
	}

	public String sendingClientId() {

		return sendingClientId;
	}

	public String nonce() {

		return nonce;
	}

}
