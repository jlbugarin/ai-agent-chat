package com.banco.reclamos.exception;

/**
 * Se lanza cuando se busca un reclamo que no existe en la base de datos.
 */
public class ReclamoNoEncontradoException extends RuntimeException {

    public ReclamoNoEncontradoException(String nroReclamo) {
        super("No se encontro el reclamo con numero " + nroReclamo);
    }
}
