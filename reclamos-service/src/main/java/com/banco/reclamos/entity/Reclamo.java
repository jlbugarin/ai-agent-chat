package com.banco.reclamos.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * Entidad que representa un reclamo almacenado en la base de datos H2.
 *
 * Usa el patron "active record" de Panache: los campos son publicos y los
 * metodos de consulta se exponen como metodos estaticos. Hibernate genera
 * automaticamente getters/setters a nivel de bytecode.
 */
@Entity
@Table(
        name = "reclamos",
        indexes = {
                @Index(name = "idx_reclamo_nro", columnList = "nro_reclamo", unique = true),
                @Index(name = "idx_reclamo_dni", columnList = "dni")
        }
)
public class Reclamo extends PanacheEntity {

    /** Numero de reclamo de negocio, autogenerado (ej: REC-2026-000001). */
    @Column(name = "nro_reclamo", unique = true, length = 30)
    public String nroReclamo;

    // ---------- Datos del cliente ----------
    @Column(nullable = false, length = 120)
    public String nombres;

    @Column(nullable = false, length = 120)
    public String apellidos;

    @Column(nullable = false, length = 15)
    public String dni;

    @Column(nullable = false, length = 150)
    public String email;

    @Column(length = 250)
    public String direccion;

    /** Representante o apoderado (opcional). */
    @Column(name = "representante", length = 200)
    public String representante;

    @Column(name = "numero_celular", length = 20)
    public String numeroCelular;

    @Enumerated(EnumType.STRING)
    @Column(name = "medio_respuesta", length = 20)
    public MedioRespuesta medioRespuesta;

    // ---------- Datos del reclamo ----------
    @Column(name = "motivo_reclamo", length = 250)
    public String motivoReclamo;

    @Column(length = 120)
    public String producto;

    @Column(name = "nro_tarjeta", length = 25)
    public String nroTarjeta;

    /** Marca / tipo (ej: VISA CREDITO, MASTERCARD DEBITO). */
    @Column(name = "marca_tipo", length = 80)
    public String marcaTipo;

    @Column(name = "fecha_registro", nullable = false)
    public LocalDateTime fechaRegistro;

    @Column(name = "fecha_vencimiento")
    public LocalDate fechaVencimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal_registro", length = 25)
    public CanalRegistro canalRegistro;

    @Column(name = "descripcion_reclamo", length = 2000)
    public String descripcionReclamo;

    @Column(name = "monto_reclamo", precision = 15, scale = 2)
    public BigDecimal montoReclamo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_moneda", length = 10)
    public TipoMoneda tipoMoneda;

    // ---------- Seguimiento del estado ----------
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public EstadoReclamo estado;

    @Column(name = "observacion_estado", length = 1000)
    public String observacionEstado;

    @Column(name = "fecha_actualizacion")
    public LocalDateTime fechaActualizacion;

    // ---------- Metodos de consulta (Panache) ----------

    /** Busca un reclamo por su numero de negocio. */
    public static Reclamo findByNroReclamo(String nroReclamo) {
        return find("nroReclamo", nroReclamo).firstResult();
    }
}
