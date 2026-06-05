package com.banco.reclamos.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.banco.reclamos.entity.CanalRegistro;
import com.banco.reclamos.entity.EstadoReclamo;
import com.banco.reclamos.entity.MedioRespuesta;
import com.banco.reclamos.entity.Reclamo;
import com.banco.reclamos.entity.TipoMoneda;

/**
 * Representacion de salida de un reclamo. El numero de tarjeta se devuelve
 * enmascarado por seguridad.
 */
public class ReclamoResponse {

    public Long id;
    public String nroReclamo;

    public String nombres;
    public String apellidos;
    public String dni;
    public String email;
    public String direccion;
    public String representante;
    public String numeroCelular;
    public MedioRespuesta medioRespuesta;

    public String motivoReclamo;
    public String producto;
    public String nroTarjeta;
    public String marcaTipo;
    public LocalDateTime fechaRegistro;
    public LocalDate fechaVencimiento;
    public CanalRegistro canalRegistro;
    public String descripcionReclamo;
    public BigDecimal montoReclamo;
    public TipoMoneda tipoMoneda;

    public EstadoReclamo estado;
    public String observacionEstado;
    public LocalDateTime fechaActualizacion;

    public static ReclamoResponse from(Reclamo r) {
        ReclamoResponse dto = new ReclamoResponse();
        dto.id = r.id;
        dto.nroReclamo = r.nroReclamo;
        dto.nombres = r.nombres;
        dto.apellidos = r.apellidos;
        dto.dni = r.dni;
        dto.email = r.email;
        dto.direccion = r.direccion;
        dto.representante = r.representante;
        dto.numeroCelular = r.numeroCelular;
        dto.medioRespuesta = r.medioRespuesta;
        dto.motivoReclamo = r.motivoReclamo;
        dto.producto = r.producto;
        dto.nroTarjeta = enmascarar(r.nroTarjeta);
        dto.marcaTipo = r.marcaTipo;
        dto.fechaRegistro = r.fechaRegistro;
        dto.fechaVencimiento = r.fechaVencimiento;
        dto.canalRegistro = r.canalRegistro;
        dto.descripcionReclamo = r.descripcionReclamo;
        dto.montoReclamo = r.montoReclamo;
        dto.tipoMoneda = r.tipoMoneda;
        dto.estado = r.estado;
        dto.observacionEstado = r.observacionEstado;
        dto.fechaActualizacion = r.fechaActualizacion;
        return dto;
    }

    private static String enmascarar(String tarjeta) {
        if (tarjeta == null) {
            return null;
        }
        String limpio = tarjeta.replaceAll("\\s+", "");
        if (limpio.length() <= 4) {
            return "****";
        }
        String ultimos = limpio.substring(limpio.length() - 4);
        return "**** **** **** " + ultimos;
    }
}
