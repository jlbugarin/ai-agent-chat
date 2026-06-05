package com.banco.reclamos.service;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

import com.banco.reclamos.dto.ReclamoRequest;
import com.banco.reclamos.entity.EstadoReclamo;
import com.banco.reclamos.entity.Reclamo;
import com.banco.reclamos.exception.ReclamoNoEncontradoException;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

/**
 * Servicio que concentra la logica de negocio de reclamos.
 * Es reutilizado tanto por la capa REST como por la capa MCP, de modo que
 * ambas exponen exactamente el mismo comportamiento.
 */
@ApplicationScoped
public class ReclamoService {

    /**
     * Registra un nuevo reclamo y genera automaticamente su numero
     * (formato REC-{anio}-{secuencia de 6 digitos}).
     */
    @Transactional
    public Reclamo registrar(ReclamoRequest req) {
        Reclamo r = new Reclamo();
        r.nombres = req.nombres;
        r.apellidos = req.apellidos;
        r.dni = req.dni;
        r.email = req.email;
        r.direccion = req.direccion;
        r.representante = req.representante;
        r.numeroCelular = req.numeroCelular;
        r.medioRespuesta = req.medioRespuesta;
        r.motivoReclamo = req.motivoReclamo;
        r.producto = req.producto;
        r.nroTarjeta = req.nroTarjeta;
        r.marcaTipo = req.marcaTipo;
        r.fechaVencimiento = req.fechaVencimiento;
        r.canalRegistro = req.canalRegistro;
        r.descripcionReclamo = req.descripcionReclamo;
        r.montoReclamo = req.montoReclamo;
        r.tipoMoneda = req.tipoMoneda;

        r.fechaRegistro = LocalDateTime.now();
        r.fechaActualizacion = r.fechaRegistro;
        r.estado = EstadoReclamo.REGISTRADO;
        r.observacionEstado = "Reclamo registrado correctamente.";

        // Persiste para obtener el id autogenerado y luego construye el nro de negocio.
        r.persistAndFlush();
        r.nroReclamo = "REC-" + Year.now().getValue() + "-" + String.format("%06d", r.id);
        r.persist();
        return r;
    }

    /** Obtiene el reclamo completo por su numero de negocio. */
    public Reclamo obtenerPorNro(String nroReclamo) {
        Reclamo r = Reclamo.findByNroReclamo(nroReclamo);
        if (r == null) {
            throw new ReclamoNoEncontradoException(nroReclamo);
        }
        return r;
    }

    /** Lista todos los reclamos ordenados por fecha de registro descendente. */
    public List<Reclamo> listar() {
        return Reclamo.listAll(Sort.by("fechaRegistro").descending());
    }

    /** Lista los reclamos asociados a un DNI. */
    public List<Reclamo> listarPorDni(String dni) {
        return Reclamo.list("dni", Sort.by("fechaRegistro").descending(), dni);
    }

    /** Actualiza el estado de un reclamo. */
    @Transactional
    public Reclamo actualizarEstado(String nroReclamo, EstadoReclamo nuevoEstado, String observacion) {
        Reclamo r = Reclamo.findByNroReclamo(nroReclamo);
        if (r == null) {
            throw new ReclamoNoEncontradoException(nroReclamo);
        }
        r.estado = nuevoEstado;
        if (observacion != null && !observacion.isBlank()) {
            r.observacionEstado = observacion;
        }
        r.fechaActualizacion = LocalDateTime.now();
        r.persist();
        return r;
    }
}
