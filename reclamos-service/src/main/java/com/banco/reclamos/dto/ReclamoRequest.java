package com.banco.reclamos.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.banco.reclamos.entity.CanalRegistro;
import com.banco.reclamos.entity.MedioRespuesta;
import com.banco.reclamos.entity.TipoMoneda;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos requeridos para registrar un reclamo.
 */
public class ReclamoRequest {

    @NotBlank(message = "nombres es obligatorio")
    @Size(max = 120)
    public String nombres;

    @NotBlank(message = "apellidos es obligatorio")
    @Size(max = 120)
    public String apellidos;

    @NotBlank(message = "dni es obligatorio")
    @Pattern(regexp = "\\d{8,15}", message = "dni debe contener entre 8 y 15 digitos")
    public String dni;

    @NotBlank(message = "email es obligatorio")
    @Email(message = "email no tiene un formato valido")
    public String email;

    @Size(max = 250)
    public String direccion;

    /** Representante o apoderado (opcional). */
    @Size(max = 200)
    public String representante;

    @Pattern(regexp = "\\+?\\d{6,20}", message = "numeroCelular no es valido")
    public String numeroCelular;

    @NotNull(message = "medioRespuesta es obligatorio (EMAIL, TELEFONO, SMS, WHATSAPP, CARTA)")
    public MedioRespuesta medioRespuesta;

    @NotBlank(message = "motivoReclamo es obligatorio")
    @Size(max = 250)
    public String motivoReclamo;

    @Size(max = 120)
    public String producto;

    @Size(max = 25)
    public String nroTarjeta;

    /** Marca / tipo (ej: VISA CREDITO). */
    @Size(max = 80)
    public String marcaTipo;

    public LocalDate fechaVencimiento;

    @NotNull(message = "canalRegistro es obligatorio (WEB, APP_MOVIL, AGENCIA, CALL_CENTER, CORREO_ELECTRONICO)")
    public CanalRegistro canalRegistro;

    @NotBlank(message = "descripcionReclamo es obligatorio")
    @Size(max = 2000)
    public String descripcionReclamo;

    @DecimalMin(value = "0.0", inclusive = true, message = "montoReclamo no puede ser negativo")
    public BigDecimal montoReclamo;

    @NotNull(message = "tipoMoneda es obligatorio (SOLES o DOLARES)")
    public TipoMoneda tipoMoneda;
}
