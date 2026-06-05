package com.banco.reclamos.exception;

import java.util.Map;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Mapea ReclamoNoEncontradoException a 404 con un cuerpo JSON estandar.
 */
@Provider
public class ReclamoNoEncontradoMapper implements ExceptionMapper<ReclamoNoEncontradoException> {

    @Override
    public Response toResponse(ReclamoNoEncontradoException ex) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of(
                        "error", "RECLAMO_NO_ENCONTRADO",
                        "mensaje", ex.getMessage()))
                .build();
    }
}
