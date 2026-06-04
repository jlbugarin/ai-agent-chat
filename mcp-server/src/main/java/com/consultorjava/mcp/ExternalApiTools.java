package com.consultorjava.mcp;

import com.consultorjava.mcp.client.GeocodingClient;
import com.consultorjava.mcp.client.GitHubClient;
import com.consultorjava.mcp.client.WeatherClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Conjunto de herramientas (tools) que el servidor MCP expone al LLM.
 * Cada método anotado con @Tool se descubre en build-time y queda disponible
 * vía protocolo MCP en http://localhost:8081/mcp/sse.
 *
 * El LLM, al recibir una pregunta del usuario en el chat, decide cuál de
 * estas herramientas invocar en función de la descripción que aquí declaramos.
 */
public class ExternalApiTools {

    @Inject
    @RestClient
    GeocodingClient geocodingClient;

    @Inject
    @RestClient
    WeatherClient weatherClient;

    @Inject
    @RestClient
    GitHubClient gitHubClient;

    private final ObjectMapper mapper = new ObjectMapper();

    @Tool(description = """
            Devuelve el clima actual de una ciudad: temperatura, viento y código del tiempo.
            Útil cuando el usuario pregunta por el clima, temperatura o pronóstico de una ubicación.
            """)
    String getWeather(
            @ToolArg(description = "Nombre de la ciudad, por ejemplo: Lima, Madrid, Bogotá") String city) {
        try {
            // Paso 1: geocoding para obtener latitud/longitud
            try (Response geoResp = geocodingClient.search(city, 1, "es", "json")) {
                if (geoResp.getStatus() != 200) {
                    return "No pude obtener las coordenadas de '" + city + "'.";
                }
                JsonNode geo = mapper.readTree(geoResp.readEntity(String.class));
                JsonNode results = geo.path("results");
                if (!results.isArray() || results.isEmpty()) {
                    return "No se encontró la ciudad: " + city;
                }
                double lat = results.get(0).path("latitude").asDouble();
                double lon = results.get(0).path("longitude").asDouble();
                String resolvedName = results.get(0).path("name").asText();
                String country = results.get(0).path("country").asText();

                // Paso 2: pronóstico actual
                try (Response wResp = weatherClient.forecast(lat, lon, true, "auto")) {
                    if (wResp.getStatus() != 200) {
                        return "No pude consultar el clima de " + resolvedName + ".";
                    }
                    JsonNode w = mapper.readTree(wResp.readEntity(String.class));
                    JsonNode current = w.path("current_weather");
                    return String.format(
                            "Clima actual en %s, %s: %.1f°C, viento %.1f km/h, código del tiempo %d.",
                            resolvedName, country,
                            current.path("temperature").asDouble(),
                            current.path("windspeed").asDouble(),
                            current.path("weathercode").asInt());
                }
            }
        } catch (Exception e) {
            return "Error consultando el clima: " + e.getMessage();
        }
    }

    @Tool(description = """
            Devuelve información pública de un repositorio en GitHub: descripción,
            número de estrellas, lenguaje principal, forks y URL.
            Útil cuando el usuario pregunta por un repositorio o proyecto en GitHub.
            """)
    String getGitHubRepoInfo(
            @ToolArg(description = "Dueño del repositorio (usuario u organización), por ejemplo: 'quarkusio'") String owner,
            @ToolArg(description = "Nombre del repositorio, por ejemplo: 'quarkus'") String repo) {
        try (Response resp = gitHubClient.getRepository(owner, repo)) {
            if (resp.getStatus() == 404) {
                return "El repositorio " + owner + "/" + repo + " no existe.";
            }
            if (resp.getStatus() != 200) {
                return "No pude consultar GitHub (HTTP " + resp.getStatus() + ").";
            }
            JsonNode r = mapper.readTree(resp.readEntity(String.class));
            return String.format(
                    "Repositorio %s/%s - %s. Lenguaje: %s. Estrellas: %d. Forks: %d. URL: %s",
                    owner, repo,
                    r.path("description").asText("(sin descripción)"),
                    r.path("language").asText("desconocido"),
                    r.path("stargazers_count").asInt(),
                    r.path("forks_count").asInt(),
                    r.path("html_url").asText());
        } catch (Exception e) {
            return "Error consultando GitHub: " + e.getMessage();
        }
    }

    @Tool(description = """
            Consulta una incidencia (issue) en Jira por su clave (por ejemplo PROJ-123).
            Devuelve título, estado, asignado y prioridad.
            Esta es una implementación simulada para demo; en producción se conectaría a la API real de Jira.
            """)
    String getJiraIssue(
            @ToolArg(description = "Clave del issue en Jira, formato PROYECTO-NUMERO, por ejemplo PROJ-42") String issueKey) {
        // Simulación: en producción, inyectar un RestClient apuntando a {jira}/rest/api/3/issue/{key}
        // con autenticación Basic (email + API token).
        return String.format(
                "Issue %s - Título: 'Refactorizar módulo de pagos'. Estado: 'En progreso'. " +
                "Asignado: 'María García'. Prioridad: 'Alta'. (Resultado simulado)",
                issueKey);
    }
}
