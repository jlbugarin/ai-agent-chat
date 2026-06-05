# reclamos-service — Quarkus + Panache + MCP Server

Aplicación en **Java + Quarkus (Red Hat build)** que permite **registrar
reclamos** bancarios y **consultar su estado** mediante **APIs REST**,
persistiendo en una base **H2 local** con **Hibernate ORM + Panache**.
Expone además una capa **MCP Server** sobre las mismas operaciones, que es
consumida por el módulo `reclamos-chat` (LangChain4j + LLM).

## Requisitos

- JDK 17+
- Maven 3.9+
- Acceso al repo Red Hat GA y a Maven Central la primera vez

## Estructura

```
src/main/java/com/banco/reclamos
├── entity/      Reclamo (PanacheEntity) + enums (TipoMoneda, MedioRespuesta, CanalRegistro, EstadoReclamo)
├── dto/         ReclamoRequest, ReclamoResponse, EstadoReclamoResponse, ActualizarEstadoRequest
├── service/     ReclamoService (logica compartida REST + MCP)
├── resource/    ReclamoResource (API REST en /api/reclamos)
├── exception/   ReclamoNoEncontradoException + mapper a HTTP 404
└── mcp/         ReclamoMcpTools (5 @Tool MCP)
```

REST y MCP usan **el mismo `ReclamoService`**, así que su comportamiento es idéntico.

## Ejecutar (dev mode)

```bash
cd reclamos-service
mvn quarkus:dev
```

- REST: `http://localhost:8081/api/reclamos`
- MCP SSE: `http://localhost:8081/mcp/sse`
- Health: `http://localhost:8081/q/health`

### Ejemplo: registrar un reclamo (REST)

```bash
curl -X POST http://localhost:8081/api/reclamos \
  -H "Content-Type: application/json" \
  -d '{
    "nombres": "Juan",
    "apellidos": "Perez",
    "dni": "12345678",
    "email": "juan@example.com",
    "direccion": "Av. Siempre Viva 123",
    "numeroCelular": "987654321",
    "medioRespuesta": "EMAIL",
    "motivoReclamo": "Cobro indebido",
    "producto": "Tarjeta de credito",
    "nroTarjeta": "4111111111111111",
    "marcaTipo": "VISA CREDITO",
    "fechaVencimiento": "2027-05-31",
    "canalRegistro": "WEB",
    "descripcionReclamo": "Cargo no reconocido por 150.50",
    "montoReclamo": 150.50,
    "tipoMoneda": "SOLES"
  }'
```

### Ejemplo: consultar estado

```bash
curl http://localhost:8081/api/reclamos/REC-2026-000001/estado
```

## Capa MCP Server

`ReclamoMcpTools` expone estas tools por **HTTP/SSE** en `http://localhost:8081/mcp/sse`:

- `registrar_reclamo`
- `consultar_reclamo`
- `consultar_estado_reclamo`
- `listar_reclamos`
- `actualizar_estado_reclamo`

### Conectar el chat (reclamos-chat) por SSE

El módulo `reclamos-chat` ya viene configurado con:

```properties
quarkus.langchain4j.mcp.reclamos.transport-type=http
quarkus.langchain4j.mcp.reclamos.url=http://localhost:8081/mcp/sse
```

### Conectar Claude Desktop (STDIO)

1. Empaquetar: `mvn package` (produce `target/quarkus-app/quarkus-run.jar`)
2. En `claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "reclamos": {
      "command": "cmd",
      "args": [
        "/c",
        "cd /d D:\\mcp-reclamos\\reclamos-service\\target\\quarkus-app && java -Dquarkus.profile=stdio -jar quarkus-run.jar"
      ]
    }
  }
}
```

El `cmd /c cd /d ...` evita el `AccessDeniedException` sobre
`C:\WINDOWS\System32\config` cuando Claude Desktop lanza el proceso desde
ese directorio. El perfil `stdio` redirige los logs a `reclamos-mcp.log`
para no contaminar el stdout reservado al JSON-RPC.

## Notas de versiones

- Quarkus `3.20.1.redhat-00003` (LTS)
- `quarkus-mcp-server-sse / stdio` `1.2.0` (alineado con Quarkus 3.20 LTS)
- Hibernate ORM con Panache + H2 (archivo local)
