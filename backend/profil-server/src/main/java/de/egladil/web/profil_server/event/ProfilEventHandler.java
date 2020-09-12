// =====================================================
// Project: profil-server
// (c) Heike Winkelvoß
// =====================================================
package de.egladil.web.profil_server.event;

import java.util.Map;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.egladil.web.commons_validation.payload.MessagePayload;
import de.egladil.web.commons_validation.payload.ResponsePayload;
import de.egladil.web.profil_server.domain.StoredEvent;
import de.egladil.web.profil_server.error.ProfilserverRuntimeException;
import de.egladil.web.profil_server.restclient.MkGatewayRestClient;

/**
 * ProfilEventHandler
 */
@ApplicationScoped
public class ProfilEventHandler {

	private static final Logger LOG = LoggerFactory.getLogger(ProfilEventHandler.class);

	@ConfigProperty(name = "mkv-app.client-id")
	private String clientId;

	@Inject
	EventRepository eventRepository;

	@Inject
	@RestClient
	MkGatewayRestClient mkGateway;

	public void handleEvent(@Observes final ProfilEvent event) {

		try {

			String body = new ObjectMapper().writeValueAsString(event);

			LOG.debug("Event body = " + body);

			StoredEvent storedEvent = StoredEvent.createEvent(event.occuredOn(), event.typeName(), body);

			this.eventRepository.appendEvent(storedEvent);

			if (event.propagateToListeners()) {

				sendEventToListeners(event);
			}
		} catch (JsonProcessingException e) {

			throw new ProfilserverRuntimeException("konnte event nicht serialisieren: " + e.getMessage(), e);

		}

	}

	/**
	 * @param event
	 */
	private void sendEventToListeners(final ProfilEvent event) {

		if (!event.propagateToListeners()) {

			return;
		}

		String syncToken = getSyncToken(event);

		if (syncToken == null) {

			LOG.error("Datensynchronisation hat keine Freigabe: syncToken ist null");
		}

		if (ProfilEvent.TYPE_USER_CHANGED.equals(event.typeName())) {

			try {

				UserChanged userChanged = (UserChanged) event;

				ChangeUserCommand command = new ChangeUserCommand(syncToken, userChanged);

				Response mkGatewayResponse = mkGateway.propagateUserChanged(command);

				if (mkGatewayResponse.getStatus() != 200) {

					LOG.error("Status {} vom mk-gateway beim senden des Events {} ", mkGatewayResponse.getStatus(), userChanged);
				}

			} catch (Exception e) {

				LOG.error("Konnte change-event nicht propagieren: {} - {}", event, e.getMessage(), e);
			}

		}

		if (ProfilEvent.TYPE_USER_DELETED.equals(event.typeName())) {

			LOG.info("Senden Löschevent an mk-gateway");

			try {

				UserDeleted userDeleted = (UserDeleted) event;

				DeleteUserCommand command = DeleteUserCommand.fromEvent(userDeleted).withSyncToken(syncToken);

				Response mkGatewayResponse = mkGateway.propagateUserDeleted(command);

				LOG.info("Antwort: " + mkGatewayResponse.getStatus());

				if (mkGatewayResponse.getStatus() != 200) {

					LOG.error("Status {} vom mk-gateway beim senden des Events {} ", mkGatewayResponse.getStatus(), userDeleted);
				}

			} catch (Exception e) {

				LOG.error("Konnte delete-event nicht propagieren: {} - {}", event, e.getMessage(), e);
			}
		}
	}

	/**
	 * @param event
	 */
	private String getSyncToken(final ProfilEvent event) {

		String nonce = UUID.randomUUID().toString();

		SyncHandshake handshake = new SyncHandshake(clientId, nonce);

		try {

			Response mkGatewayResponse = mkGateway.getSyncToken(handshake);

			ResponsePayload responsePayload = mkGatewayResponse.readEntity(ResponsePayload.class);

			MessagePayload messagePayload = responsePayload.getMessage();

			if (messagePayload.isOk()) {

				try {

					@SuppressWarnings("unchecked")
					Map<String, Object> data = (Map<String, Object>) responsePayload.getData();

					HandshakeAck ack = HandshakeAck.fromResponse(data);

					if (!nonce.equals(ack.nonce())) {

						LOG.error("Nonce wurde geändert");
						return null;
					}

					return ack.syncToken();

				} catch (ClassCastException e) {

					LOG.error(e.getMessage());
					throw new ProfilserverRuntimeException("Konnte ResponsePayload vom mk-gateway nicht verarbeiten");
				}

			}

			return null;
		} catch (Exception e) {

			LOG.error("Keine Freigabe fürs Senden des Events {} - {}", event, e.getMessage(), e);
			return null;
		}
	}

}
