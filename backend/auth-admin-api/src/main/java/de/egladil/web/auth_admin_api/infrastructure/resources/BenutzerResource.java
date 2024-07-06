// =====================================================
// Project: auth-admin-api
// (c) Heike Winkelvoß
// =====================================================
package de.egladil.web.auth_admin_api.infrastructure.resources;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import de.egladil.web.auth_admin_api.domain.auth.dto.MessagePayload;
import de.egladil.web.auth_admin_api.domain.benutzer.BenutzerSearchResult;
import de.egladil.web.auth_admin_api.domain.benutzer.BenutzerService;
import de.egladil.web.auth_admin_api.domain.benutzer.BenutzerSuchparameter;
import de.egladil.web.auth_admin_api.domain.validation.ValidationErrorResponseDto;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * BenutzerResource
 */
@RequestScoped
@Path("auth-admin-api/benutzer")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
@Tag(name = "BenutzerResource")
public class BenutzerResource {

	@Inject
	BenutzerService benutzerService;

	@POST
	@RolesAllowed({ "AUTH_ADMIN" })
	@Operation(
		operationId = "findUsers", summary = "Gibt alle Benutzer zurück, die auf die gegebene Suchanfrage passen.")
	@APIResponse(
		name = "OKResponse",
		responseCode = "200",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = BenutzerSearchResult.class)))
	@APIResponse(
		name = "BadRequest",
		responseCode = "400",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(type = SchemaType.ARRAY, implementation = ValidationErrorResponseDto.class)))
	@APIResponse(
		name = "NotAuthorized",
		responseCode = "401",
		content = @Content(
			mediaType = "application/json"))
	@APIResponse(
		name = "Forbidden",
		description = "kann auch vorkommen, wenn mod_security zuschlägt",
		responseCode = "403",
		content = @Content(
			mediaType = "application/json"))
	@APIResponse(
		name = "ServerError",
		description = "server error",
		responseCode = "500", content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = MessagePayload.class)))
	public Response findUsers(@Valid final BenutzerSuchparameter userSerachDto) {

		BenutzerSearchResult responsePayload = benutzerService.findUsers(userSerachDto);
		// TODO output-validierung
		return Response.ok(responsePayload).build();
	}

	public Response benutzerAktivieren(final String uuid) {

		return Response.serverError().entity(MessagePayload.error("Funbktion noch nicht implementiert")).build();
	}

}
