// =====================================================
// Project: mkadmin-server
// (c) Heike Winkelvoß
// =====================================================
package de.egladil.web.authprovider.endpoints;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import de.egladil.web.commons_validation.payload.MessagePayload;
import de.egladil.web.commons_validation.payload.ResponsePayload;

/**
 * VersionResource
 */
@RequestScoped
@Path("version")
@Produces(MediaType.APPLICATION_JSON)
public class VersionResource {

	@ConfigProperty(name = "quarkus.application.version", defaultValue = "5.2.1")
	String version;

	@GET
	public Response getVersion() {

		return Response.ok(ResponsePayload.messageOnly(MessagePayload.info(version))).build();

	}

}
