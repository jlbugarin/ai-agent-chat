package com.banco.reclamos.resource;

import java.net.URI;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.banco.reclamos.dto.ActualizarEstadoRequest;
import com.banco.reclamos.dto.EstadoReclamoResponse;
import com.banco.reclamos.dto.ReclamoRequest;
import com.banco.reclamos.dto.ReclamoResponse;
import com.banco.reclamos.service.ReclamoService;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * API REST para registrar reclamos y consultar su estado.
 * Documentada con OpenAPI 3 — ver  /q/swagger-ui  cuando la app esta corriendo.
 */
@Tag(name = "Reclamos", description = "Registro, consulta y seguimiento de reclamos bancarios")
@Path("/api/reclamos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReclamoResource {

    @Inject
    ReclamoService service;

    @POST
    @Operation(
            summary = "Registrar un nuevo reclamo",
            description = "Crea un reclamo, le asigna automaticamente un numero "
                    + "(formato REC-{anio}-{6 digitos}) y lo deja en estado REGISTRADO. "
                    + "El numero de tarjeta se devuelve enmascarado por seguridad."
    )
    @APIResponses({
            @APIResponse(responseCode = "201",
                    description = "Reclamo registrado correctamente",
                    content = @Content(schema = @Schema(implementation = ReclamoResponse.class))),
            @APIResponse(responseCode = "400", description = "Datos invalidos en el cuerpo de la peticion")
    })
    public Response registrar(@Valid ReclamoRequest request) {
        ReclamoResponse creado = ReclamoResponse.from(service.registrar(request));
        return Response.created(URI.create("/api/reclamos/" + creado.nroReclamo))
                .entity(creado)
                .build();
    }

    @GET
    @Operation(
            summary = "Listar reclamos",
            description = "Devuelve todos los reclamos ordenados por fecha de registro descendente. "
                    + "Si se envia el parametro 'dni', filtra solo los reclamos de ese cliente."
    )
    @APIResponse(responseCode = "200", description = "Lista de reclamos (puede estar vacia)")
    public List<ReclamoResponse> listar(
            @Parameter(description = "DNI del cliente para filtrar. Si se omite, devuelve todos los reclamos.",
                    example = "12345678")
            @QueryParam("dni") String dni) {
        var reclamos = (dni == null || dni.isBlank())
                ? service.listar()
                : service.listarPorDni(dni);
        return reclamos.stream().map(ReclamoResponse::from).toList();
    }

    @GET
    @Path("/{nroReclamo}")
    @Operation(
            summary = "Obtener el detalle de un reclamo",
            description = "Devuelve toda la informacion del reclamo. El numero de tarjeta se devuelve enmascarado."
    )
    @APIResponses({
            @APIResponse(responseCode = "200",
                    description = "Reclamo encontrado",
                    content = @Content(schema = @Schema(implementation = ReclamoResponse.class))),
            @APIResponse(responseCode = "404", description = "No existe un reclamo con ese numero")
    })
    public ReclamoResponse obtener(
            @Parameter(description = "Numero del reclamo (formato REC-AAAA-NNNNNN)",
                    example = "REC-2026-000001", required = true)
            @PathParam("nroReclamo") String nroReclamo) {
        return ReclamoResponse.from(service.obtenerPorNro(nroReclamo));
    }

    @GET
    @Path("/{nroReclamo}/estado")
    @Operation(
            summary = "Consultar solo el estado de un reclamo",
            description = "Respuesta liviana con el estado actual, la observacion y la fecha de ultima actualizacion."
    )
    @APIResponses({
            @APIResponse(responseCode = "200",
                    description = "Estado del reclamo",
                    content = @Content(schema = @Schema(implementation = EstadoReclamoResponse.class))),
            @APIResponse(responseCode = "404", description = "No existe un reclamo con ese numero")
    })
    public EstadoReclamoResponse consultarEstado(
            @Parameter(description = "Numero del reclamo", example = "REC-2026-000001", required = true)
            @PathParam("nroReclamo") String nroReclamo) {
        return EstadoReclamoResponse.from(service.obtenerPorNro(nroReclamo));
    }

    @PUT
    @Path("/{nroReclamo}/estado")
    @Operation(
            summary = "Actualizar el estado de un reclamo",
            description = "Cambia el estado del reclamo. Valores validos: "
                    + "REGISTRADO, EN_EVALUACION, EN_PROCESO, RESUELTO, RECHAZADO. "
                    + "Opcionalmente se puede registrar una observacion."
    )
    @APIResponses({
            @APIResponse(responseCode = "200",
                    description = "Estado actualizado",
                    content = @Content(schema = @Schema(implementation = EstadoReclamoResponse.class))),
            @APIResponse(responseCode = "400", description = "Estado invalido o cuerpo malformado"),
            @APIResponse(responseCode = "404", description = "No existe un reclamo con ese numero")
    })
    public EstadoReclamoResponse actualizarEstado(
            @Parameter(description = "Numero del reclamo", example = "REC-2026-000001", required = true)
            @PathParam("nroReclamo") String nroReclamo,
            @Valid ActualizarEstadoRequest request) {
        return EstadoReclamoResponse.from(
                service.actualizarEstado(nroReclamo, request.estado, request.observacion));
    }
}
