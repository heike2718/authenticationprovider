// =====================================================
// Project: profil-server
// (c) Heike Winkelvoß
// =====================================================
package de.egladil.web.authprovider.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * StoredEvent
 */
@Entity
@Table(name = "EVENTS")
public class StoredEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;

	@Column(name = "TIME_OCCURED")
	private LocalDateTime occuredOn;

	@Column(name = "NAME")
	private String name;

	@Column(name = "BODY")
	private String body;

	@Version
	@Column(name = "VERSION")
	private int version;

	public static StoredEvent createEvent(final LocalDateTime occuredOn, final String name, final String body) {

		StoredEvent result = new StoredEvent();
		result.body = body;
		result.name = name;
		result.occuredOn = occuredOn;

		return result;

	}

	/**
	 *
	 */
	StoredEvent() {

	}

	public Long getId() {

		return id;
	}

	public LocalDateTime getOccuredOn() {

		return occuredOn;
	}

	public String getName() {

		return name;
	}

	public String getBody() {

		return body;
	}
}
