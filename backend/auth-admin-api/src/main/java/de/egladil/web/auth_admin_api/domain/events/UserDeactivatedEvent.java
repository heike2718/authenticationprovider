// =====================================================
// Project: auth-admin-api
// (c) Heike Winkelvoß
// =====================================================
package de.egladil.web.auth_admin_api.domain.events;

/**
 * UserDeactivatedEvent
 */
public class UserDeactivatedEvent extends AbstractAuthAdminEvent {

	public UserDeactivatedEvent(final AuthAdminEventPayload eventPayload) {

		super(eventPayload);

	}

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

		return EventType.USER_DEACTIVATED;
	}

}
