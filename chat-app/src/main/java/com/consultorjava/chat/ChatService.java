package com.consultorjava.chat;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.mcp.runtime.McpToolBox;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * AI Service declarativo. Quarkus genera la implementación en build-time.
 *
 * - @RegisterAiService: lo convierte en un bean CDI inyectable.
 * - @McpToolBox("external-apis"): expone como herramientas todos los @Tool del
 *   servidor MCP llamado "external-apis" en application.properties.
 * - El LLM lee las descripciones de cada @Tool y decide cuáles llamar
 *   en función de la pregunta del usuario.
 */
@RegisterAiService
@ApplicationScoped
@McpToolBox("external-apis")
public interface ChatService {

    @SystemMessage("""
            Eres un asistente útil y conciso que responde en español.
            Cuando el usuario pregunte por información del clima, repositorios de GitHub
            o issues de Jira, DEBES usar las herramientas disponibles vía MCP
            para obtener información real, en lugar de inventar respuestas.
            Si la información obtenida por las herramientas es suficiente,
            responde de forma natural y breve sin mencionar que usaste una herramienta.
            Si una pregunta no requiere consultar APIs externas, responde directamente.
            """)
    String chat(@UserMessage String message);
}
