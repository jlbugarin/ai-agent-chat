# Chat con MCP en Quarkus + Podman AI Lab

Asistente conversacional construido completamente con tecnología abierta:

- **Frontend** en Quarkus + Qute templates + HTMX (sin frameworks JS).
- **LLM local** vía Podman AI Lab (API compatible con OpenAI).
- **Servidor MCP** en Quarkus (`quarkus-mcp-server-sse`) que expone herramientas
  para consultar APIs externas: clima (Open-Meteo), GitHub y Jira (mock).
- **Orquestador** con Quarkus LangChain4j + cliente MCP que conecta el LLM con
  el servidor MCP y deja que el modelo decida qué herramienta invocar.

## Arquitectura

```
[Navegador] ──HTTP──▶ [chat-app:8080]
                          │
              ┌───────────┴────────────┐
              ▼                        ▼
    [Podman AI Lab LLM]      [mcp-server:8081]
       (chat completions)         (@Tool methods)
                                       │
                          ┌────────────┼────────────┐
                          ▼            ▼            ▼
                    [Open-Meteo]  [GitHub API]  [Jira (mock)]
```

## Estructura del proyecto

```
chat-mcp-quarkus/
├── mcp-server/        ← Quarkus app que expone @Tool por MCP en :8081
└── chat-app/          ← Quarkus app con UI + LLM + cliente MCP en :8080
```

## Requisitos previos

- JDK 21
- Maven 3.9+
- [Podman Desktop](https://podman-desktop.io) con la extensión Podman AI Lab
- Un modelo descargado en Podman AI Lab que soporte **tool/function calling**
  (recomendados: `instructlab/granite-7b-lab-GGUF`, Llama 3.1, Mistral 7B).

## Paso 1: Levanta el modelo en Podman AI Lab

1. Abre Podman Desktop → AI Lab → **Models**.
2. Descarga un modelo con soporte para tool calling.
3. Ve a **Services** → **Create model service**, selecciona el modelo y arranca.
4. Apunta la URL y el puerto que asigna AI Lab (algo como `http://localhost:35353/v1`).

## Paso 2: Configura la URL del modelo

Edita `chat-app/src/main/resources/application.properties` y ajusta:

```properties
quarkus.langchain4j.openai.base-url=http://localhost:35353/v1
quarkus.langchain4j.openai.chat-model.model-name=instructlab/granite-7b-lab-GGUF
```

> El `model-name` debe coincidir con el identificador que muestra Podman AI Lab.

## Paso 3: Arranca el servidor MCP

En una terminal:

```bash
cd mcp-server
./mvnw quarkus:dev
```

Quedará escuchando en `http://localhost:8081`. El endpoint MCP está en
`http://localhost:8081/mcp/sse`. Puedes inspeccionar los `@Tool` registrados
desde el Dev UI de Quarkus (`http://localhost:8081/q/dev-ui`).

## Paso 4: Arranca la chat app

En otra terminal:

```bash
cd chat-app
./mvnw quarkus:dev
```

Abre `http://localhost:8080` en el navegador.

## Cómo probarlo

Algunas preguntas que disparan herramientas vía MCP:

- *"¿Cómo está el clima en Lima ahora mismo?"* → llama a `getWeather`.
- *"¿Cuántas estrellas tiene el repositorio quarkusio/quarkus en GitHub?"* → `getGitHubRepoInfo`.
- *"Dame los detalles del issue PROJ-42"* → `getJiraIssue` (respuesta simulada).

Y otras que NO disparan herramientas (el LLM responde con su propio conocimiento):

- *"¿Qué es Quarkus?"*
- *"Explícame qué es un contenedor en pocas palabras."*

## Cómo añadir nuevas herramientas al servidor MCP

Solo añade un método anotado con `@Tool` en una clase del paquete
`com.consultorjava.mcp` (o crea una clase nueva). Por ejemplo:

```java
@Tool(description = "Devuelve el estado de los servicios de AWS para una región")
String getAwsStatus(@ToolArg(description = "Región AWS, ej: us-east-1") String region) {
    // ... lógica ...
}
```

Quarkus la descubre en build-time y queda automáticamente disponible para el LLM
sin tocar nada en la chat-app. Esa es la magia de MCP: desacopla el catálogo de
herramientas del cliente que las consume.

## Troubleshooting

**El LLM no llama a las herramientas.** El modelo cargado debe soportar tool
calling. Modelos pequeños o muy cuantizados a veces fallan en seguir el
protocolo de function calling. Cambia a uno más capaz (Granite, Llama 3.1,
Mistral 7B Instruct).

**Timeout.** Los LLMs locales son lentos. Sube el timeout en
`application.properties`:
```properties
quarkus.langchain4j.openai.timeout=180s
```

**El MCP server no responde.** Verifica que arrancó en :8081 y que
`quarkus.langchain4j.mcp.external-apis.url` apunta a `/mcp/sse` (no `/mcp`).

## Próximos pasos

- Streaming de respuestas con `Multi<String>` y SSE en HTMX.
- Memoria de conversación (`@SessionScoped` + `ChatMemory`).
- Empaquetado con Docker/Podman para correr todo en contenedores.
- Despliegue en OpenShift con OpenShift AI sirviendo el modelo.
