package com.banco.reclamos.dto;

import com.banco.reclamos.entity.EstadoReclamo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Datos para actualizar el estado de un reclamo.
 */
public class ActualizarEstadoRequest {

    @NotNull(message = "estado es obligatorio")
    public EstadoReclamo estado;

    @Size(max = 1000)
    public String observacion;
}
