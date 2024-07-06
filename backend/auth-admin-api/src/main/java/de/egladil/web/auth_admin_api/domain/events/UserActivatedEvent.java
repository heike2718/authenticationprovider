// =====================================================
// Project: auth-admin-api
// (c) Heike Winkelvoß
// =====================================================
package de.egladil.web.auth_admin_api.domain.events;

/**
 * UserActivatedEvent
 */
public class UserActivatedEvent extends AbstractAuthAdminEvent {

	@Override
	public boolean writeToEventStore() {

		return true;
	}

	@Override
	public boolean propagateToListeners() {

		return false;
	}

	@Override
	public EventType eventType() {

		return EventType.USER_ACTIVATED;
	}

}
