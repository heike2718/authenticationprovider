// =====================================================
// Project: bv-admin
// (c) Heike Winkelvoß
// =====================================================
package de.egladil.web.benutzerprofil.domain.exceptions;

/**
 * InaccessableEndpointException
 */
public class InaccessableEndpointException extends RuntimeException {

	private static final long serialVersionUID = -3425060441268282875L;

	/**
	 * @param message
	 */
	public InaccessableEndpointException(final String message) {

		super(message);

	}

}
