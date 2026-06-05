package com.banco.reclamos.mcp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.banco.reclamos.dto.ReclamoRequest;
import com.banco.reclamos.entity.CanalRegistro;
import com.banco.reclamos.entity.EstadoReclamo;
import com.banco.reclamos.entity.MedioRespuesta;
import com.banco.reclamos.entity.Reclamo;
import com.banco.reclamos.entity.TipoMoneda;
import com.banco.reclamos.service.ReclamoService;

import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

/**
 * Capa MCP Server.
 *
 * Esta clase se registra automaticamente como un bean CDI @Singleton por la
 * extension quarkus-mcp-server. Cada metodo anotado con @Tool queda expuesto
 * como una herramienta MCP, reutilizando exactamente el mismo
 * {@link ReclamoService} que usan los endpoints REST.
 *
 * Transporte por defecto: HTTP/SSE en  http://localhost:8081/mcp/sse
 *
 * Nota: la validacion JAX-RS @Valid NO corre en el code-path MCP, por lo que
 * se invoca el Validator de Jakarta manualmente sobre cada ReclamoRequest.
 */
public class ReclamoMcpTools {

    @Inject
    ReclamoService service;

    @Inject
    Validator validator;

    @Tool(description = "Registra un nuevo reclamo bancario y devuelve el numero de reclamo generado automaticamente.")
    public String registrar_reclamo(
            @ToolArg(description = "Nombres del cliente") String nombres,
            @ToolArg(description = "Apellidos del cliente") String apellidos,
            @ToolArg(description = "DNI del cliente (8 a 15 digitos)") String dni,
            @ToolArg(description = "Correo electronico del cliente") String email,
            @ToolArg(description = "Direccion del cliente", required = false) String direccion,
            @ToolArg(description = "Representante o apoderado", required = false) String representante,
            @ToolArg(description = "Numero de celular", required = false) String numeroCelular,
            @ToolArg(description = "Medio de respuesta: EMAIL, TELEFONO, SMS, WHATSAPP o CARTA") String medioRespuesta,
            @ToolArg(description = "Motivo del reclamo") String motivoReclamo,
            @ToolArg(description = "Producto asociado", required = false) String producto,
            @ToolArg(description = "Numero de tarjeta", required = false) String nroTarjeta,
            @ToolArg(description = "Marca / tipo (ej: VISA CREDITO)", required = false) String marcaTipo,
            @ToolArg(description = "Fecha de vencimiento en formato yyyy-MM-dd", required = false) String fechaVencimiento,
            @ToolArg(description = "Canal de registro: WEB, APP_MOVIL, AGENCIA, CALL_CENTER o CORREO_ELECTRONICO") String canalRegistro,
            @ToolArg(description = "Descripcion detallada del reclamo") String descripcionReclamo,
            @ToolArg(description = "Monto del reclamo (ej: 150.50)", required = false) String montoReclamo,
            @ToolArg(description = "Tipo de moneda: SOLES o DOLARES") String tipoMoneda) {

        ReclamoRequest req = new ReclamoRequest();
        req.nombres = nombres;
        req.apellidos = apellidos;
        req.dni = dni;
        req.email = email;
        req.direccion = direccion;
        req.representante = representante;
        req.numeroCelular = numeroCelular;
        req.medioRespuesta = parseEnum(MedioRespuesta.class, medioRespuesta, "medioRespuesta");
        req.motivoReclamo = motivoReclamo;
        req.producto = producto;
        req.nroTarjeta = nroTarjeta;
        req.marcaTipo = marcaTipo;
        if (fechaVencimiento != null && !fechaVencimiento.isBlank()) {
            req.fechaVencimiento = LocalDate.parse(fechaVencimiento);
        }
        req.canalRegistro = parseEnum(CanalRegistro.class, canalRegistro, "canalRegistro");
        req.descripcionReclamo = descripcionReclamo;
        if (montoReclamo != null && !montoReclamo.isBlank()) {
            req.montoReclamo = new BigDecimal(montoReclamo);
        }
        req.tipoMoneda = parseEnum(TipoMoneda.class, tipoMoneda, "tipoMoneda");

        // Validacion manual (la validacion JAX-RS no se aplica en el codepath MCP).
        Set<ConstraintViolation<ReclamoRequest>> violaciones = validator.validate(req);
        if (!violaciones.isEmpty()) {
            String mensaje = violaciones.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining("; "));
            throw new IllegalArgumentException("Datos invalidos: " + mensaje);
        }

        Reclamo r = service.registrar(req);
        return "Reclamo registrado correctamente. Nro: " + r.nroReclamo
                + ", estado: " + r.estado;
    }

    @Tool(description = "Consulta el detalle completo de un reclamo por su numero.")
    public String consultar_reclamo(
            @ToolArg(description = "Numero del reclamo (ej: REC-2026-000001)") String nroReclamo) {
        Reclamo r = service.obtenerPorNro(nroReclamo);
        return formatear(r);
    }

    @Tool(description = "Consulta unicamente el estado de un reclamo por su numero.")
    public String consultar_estado_reclamo(
            @ToolArg(description = "Numero del reclamo (ej: REC-2026-000001)") String nroReclamo) {
        Reclamo r = service.obtenerPorNro(nroReclamo);
        String obs = (r.observacionEstado == null || r.observacionEstado.isBlank())
                ? ""
                : " (" + r.observacionEstado + ")";
        return "Reclamo " + r.nroReclamo + " - estado: " + r.estado + obs
                + ", ultima actualizacion: " + r.fechaActualizacion;
    }

    @Tool(description = "Lista los reclamos registrados. Permite filtrar opcionalmente por DNI.")
    public String listar_reclamos(
            @ToolArg(description = "DNI del cliente (opcional). Si se omite, lista todos.", required = false) String dni) {
        List<Reclamo> reclamos = (dni == null || dni.isBlank())
                ? service.listar()
                : service.listarPorDni(dni);
        if (reclamos.isEmpty()) {
            return "No se encontraron reclamos"
                    + (dni == null ? "." : " para el DNI " + dni + ".");
        }
        return reclamos.stream()
                .map(r -> "- " + r.nroReclamo + " | " + r.estado + " | "
                        + r.nombres + " " + r.apellidos + " (DNI " + r.dni + ")"
                        + " | " + r.motivoReclamo)
                .collect(Collectors.joining("\n"));
    }

    @Tool(description = "Actualiza el estado de un reclamo. Estados validos: REGISTRADO, EN_EVALUACION, EN_PROCESO, RESUELTO, RECHAZADO.")
    public String actualizar_estado_reclamo(
            @ToolArg(description = "Numero del reclamo (ej: REC-2026-000001)") String nroReclamo,
            @ToolArg(description = "Nuevo estado: REGISTRADO, EN_EVALUACION, EN_PROCESO, RESUELTO o RECHAZADO") String estado,
            @ToolArg(description = "Observacion adicional del cambio de estado", required = false) String observacion) {
        EstadoReclamo nuevo = parseEnum(EstadoReclamo.class, estado, "estado");
        Reclamo r = service.actualizarEstado(nroReclamo, nuevo, observacion);
        return "Reclamo " + r.nroReclamo + " actualizado a estado " + r.estado
                + " el " + r.fechaActualizacion + ".";
    }

    // -------- Helpers --------

    private static <E extends Enum<E>> E parseEnum(Class<E> type, String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " es obligatorio");
        }
        try {
            return Enum.valueOf(type, value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(field + " invalido: '" + value + "'");
        }
    }

    private String formatear(Reclamo r) {
        String tarjeta = (r.nroTarjeta == null || r.nroTarjeta.isBlank())
                ? "-"
                : "**** " + (r.nroTarjeta.length() >= 4
                        ? r.nroTarjeta.substring(r.nroTarjeta.length() - 4)
                        : r.nroTarjeta);
        return "==== Reclamo " + r.nroReclamo + " ====\n"
                + "Cliente: " + r.nombres + " " + r.apellidos + " (DNI " + r.dni + ")\n"
                + "Email: " + r.email + "\n"
                + "Direccion: " + valor(r.direccion) + "\n"
                + "Representante: " + valor(r.representante) + "\n"
                + "Celular: " + valor(r.numeroCelular) + "\n"
                + "Medio de respuesta: " + r.medioRespuesta + "\n"
                + "Motivo: " + valor(r.motivoReclamo) + "\n"
                + "Producto: " + valor(r.producto) + "\n"
                + "Tarjeta: " + tarjeta + "\n"
                + "Marca/Tipo: " + valor(r.marcaTipo) + "\n"
                + "Canal de registro: " + r.canalRegistro + "\n"
                + "Descripcion: " + valor(r.descripcionReclamo) + "\n"
                + "Monto: " + (r.montoReclamo == null ? "-" : r.montoReclamo) + " " + r.tipoMoneda + "\n"
                + "Fecha de registro: " + r.fechaRegistro + "\n"
                + "Fecha de vencimiento: " + valor(r.fechaVencimiento) + "\n"
                + "Estado: " + r.estado + "\n"
                + "Observacion estado: " + valor(r.observacionEstado) + "\n"
                + "Ultima actualizacion: " + r.fechaActualizacion;
    }

    private static String valor(Object o) {
        return o == null ? "-" : o.toString();
    }
}
