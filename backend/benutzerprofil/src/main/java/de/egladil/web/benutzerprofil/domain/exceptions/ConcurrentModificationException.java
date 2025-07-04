// =====================================================
// Project: benutzerprofil
// (c) Heike Winkelvoß
// =====================================================
package de.egladil.web.benutzerprofil.domain.exceptions;

/**
 * ConcurrentModificationException
 */
public class ConcurrentModificationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final int defaultStatuscode = 409;

	public ConcurrentModificationException(final String message) {

		super(message);

	}

	public int getDefaultStatuscode() {

		return defaultStatuscode;
	}

}
