package com.buga.chat;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

/**
 * Endpoint que el chat HTML/JS consume con fetch('/chat', { ... }).
 * Toda la lógica del LLM y la invocación de tools MCP queda dentro de AsistenteReclamos.
 */
@Path("/chat")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ChatResource {

    private static final Logger LOG = Logger.getLogger(ChatResource.class);

    @Inject
    AsistenteReclamos asistente;

    @POST
    public Response chat(ChatRequest request) {
        if (request == null || request.mensaje() == null || request.mensaje().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ChatResponse("Por favor envía un mensaje."))
                    .build();
        }

        String sessionId = (request.sessionId() != null && !request.sessionId().isBlank())
                ? request.sessionId()
                : "anon";

        try {
            String respuesta = asistente.chat(sessionId, request.mensaje());
            return Response.ok(new ChatResponse(respuesta)).build();
        } catch (Exception e) {
            LOG.errorf(e, "Error procesando mensaje del chat (session=%s)", sessionId);
            return Response.serverError()
                    .entity(new ChatResponse(
                            "Disculpa, ocurrió un error procesando tu solicitud. "
                            + "Por favor intenta de nuevo en unos minutos."))
                    .build();
        }
    }
}
