# reclamos-chat

Cliente web de chat que conversa con un LLM (OpenAI) y delega las acciones
sobre reclamos al MCP server `reclamos-service` ejecutándose en una **JVM separada**.

## Arquitectura (dos JVMs)

```
[Navegador HTML/JS]
        │  HTTP :8080  POST /chat
        ▼
[reclamos-chat  (JVM #1, puerto 8080)]
   ├── ChatResource (REST)
   └── AsistenteReclamos (LangChain4j AI Service)
            │                              │
            │ HTTPS                        │ SSE :8081/mcp/sse
            ▼                              ▼
        [OpenAI]                  [reclamos-service (JVM #2, puerto 8081)]
                                         └── ReclamoMcpTools (@Tool ...)
                                                  └── ReclamoService → H2
```

Al estar en JVMs separadas **no hay arranque circular**, así que el health-check
del MCP client queda activo (`health-check-enabled=true`).

## Pre-requisitos

1. `reclamos-service` corriendo en el puerto **8081** con SSE habilitado.
   En su `application.properties`:

   ```
   quarkus.http.port=8081
   quarkus.mcp.server.sse.root-path=/mcp
   ```

2. Variable de entorno con tu API key de OpenAI:

   ```powershell
   $env:OPENAI_API_KEY = "sk-..."
   ```

## Levantar (dev mode)

Abre **dos terminales**:

```powershell
# Terminal 1 — MCP server (puerto 8081)
cd D:\...\reclamos-service
mvn quarkus:dev "-Dquarkus.http.port=8081"
```

```powershell
# Terminal 2 — chat + LLM (puerto 8080)
cd D:\...\reclamos-chat
$env:OPENAI_API_KEY = "sk-..."
mvn quarkus:dev
```

Luego abre: **http://localhost:8080/chat.html**

## Cómo probarlo

Mensajes de ejemplo para enviar desde el chat:

- "Quiero registrar un reclamo. Me llamo Juan Pérez, DNI 12345678,
  juan@example.com. El motivo es un cargo no reconocido en mi tarjeta Visa."
- "¿Cuál es el estado de mi reclamo REC-2025-000001?"
- "Lista todos mis reclamos. Mi DNI es 12345678."

El asistente decide qué *tool* MCP invocar a partir del system prompt y los
descriptores publicados por `ReclamoMcpTools`.

## Estructura

```
reclamos-chat/
├── pom.xml
├── README.md
└── src/main/
    ├── java/com/buga/chat/
    │   ├── AsistenteReclamos.java   ← AI Service + @McpToolBox("reclamos")
    │   ├── ChatRequest.java
    │   ├── ChatResponse.java
    │   └── ChatResource.java        ← REST /chat
    └── resources/
        ├── application.properties
        └── META-INF/resources/
            ├── chat.html
            ├── chat.css
            └── chat.js
```

## Troubleshooting

- **`McpToolBox cannot be resolved to a type`** → falta `quarkus-langchain4j-mcp`.
  Verifica con `mvn dependency:tree -Dincludes=io.quarkiverse.langchain4j`.
- **Connection refused a localhost:8081** → el MCP server no está arriba o
  está en otro puerto. Confirma con `curl http://localhost:8081/q/health`.
- **"not-set" como API key** → exporta `OPENAI_API_KEY` antes de `mvn quarkus:dev`.
- **CORS** → el front se sirve desde el mismo puerto 8080 que `/chat`, así que
  no debería haber CORS. Si llamas desde otro origen, ajusta `quarkus.http.cors.origins`.
