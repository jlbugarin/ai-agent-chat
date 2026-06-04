package com.consultorjava.chat;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/")
public class ChatResource {

    @Inject
    ChatService chatService;

    /**
     * Plantillas Qute con type-safety: cada método estático corresponde a un
     * archivo .html en src/main/resources/templates/ChatResource/.
     */
    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance index();
        public static native TemplateInstance messages(String userMessage, String assistantMessage);
    }

    /**
     * Página principal del chat.
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance home() {
        return Templates.index();
    }

    /**
     * Endpoint que HTMX invoca al enviar un mensaje. Devuelve un fragmento HTML
     * con el mensaje del usuario + la respuesta del asistente, que HTMX
     * insertará al final del contenedor de mensajes.
     */
    @POST
    @Path("/chat")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance chat(@FormParam("message") String message) {
        String reply;
        try {
            reply = chatService.chat(message);
        } catch (Exception e) {
            reply = "⚠️ No pude procesar tu mensaje: " + e.getMessage();
        }
        return Templates.messages(message, reply);
    }
}
