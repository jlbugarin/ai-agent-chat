package com.banco.reclamos.dto;

import java.time.LocalDateTime;

import com.banco.reclamos.entity.EstadoReclamo;
import com.banco.reclamos.entity.Reclamo;

/**
 * Respuesta liviana para la consulta del estado de un reclamo.
 */
public class EstadoReclamoResponse {

    public String nroReclamo;
    public EstadoReclamo estado;
    public String observacionEstado;
    public LocalDateTime fechaActualizacion;

    public static EstadoReclamoResponse from(Reclamo r) {
        EstadoReclamoResponse dto = new EstadoReclamoResponse();
        dto.nroReclamo = r.nroReclamo;
        dto.estado = r.estado;
        dto.observacionEstado = r.observacionEstado;
        dto.fechaActualizacion = r.fechaActualizacion;
        return dto;
    }
}
