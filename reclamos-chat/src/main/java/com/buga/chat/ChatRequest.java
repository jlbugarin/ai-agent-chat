package com.buga.chat;

/**
 * Mensaje entrante desde el navegador.
 * - sessionId: identificador del hilo de conversación (memoria por ventana).
 * - mensaje: texto libre del usuario.
 */
public record ChatRequest(String sessionId, String mensaje) {
}
