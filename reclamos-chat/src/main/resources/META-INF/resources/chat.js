(function () {
    "use strict";

    // sessionId persistente por pestaña: se reusa al refrescar para no perder memoria
    function getSessionId() {
        var key = "buga.chat.sessionId";
        var sid = sessionStorage.getItem(key);
        if (!sid) {
            sid = "sess-" + Math.random().toString(36).slice(2, 12)
                  + "-" + Date.now().toString(36);
            sessionStorage.setItem(key, sid);
        }
        return sid;
    }

    var sessionId = getSessionId();
    var messagesEl = document.getElementById("messages");
    var form = document.getElementById("composer");
    var input = document.getElementById("input");
    var sendBtn = document.getElementById("send");

    function addMessage(text, who, opts) {
        opts = opts || {};
        var div = document.createElement("div");
        div.className = "msg " + who + (opts.thinking ? " thinking" : "");
        div.textContent = text;
        messagesEl.appendChild(div);
        messagesEl.scrollTop = messagesEl.scrollHeight;
        return div;
    }

    async function send(mensaje) {
        addMessage(mensaje, "user");
        var thinking = addMessage("Escribiendo...", "bot", { thinking: true });
        sendBtn.disabled = true;
        input.disabled = true;

        try {
            var r = await fetch("/chat", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ sessionId: sessionId, mensaje: mensaje })
            });

            var data = null;
            try { data = await r.json(); } catch (_) { /* respuesta no-JSON */ }

            thinking.remove();

            if (!r.ok) {
                addMessage(
                    (data && data.respuesta) ||
                    "No se pudo procesar tu mensaje (HTTP " + r.status + ").",
                    "bot"
                );
            } else {
                addMessage((data && data.respuesta) || "(sin respuesta)", "bot");
            }
        } catch (e) {
            thinking.remove();
            addMessage(
                "No pude conectarme con el asistente. Verifica que el servicio esté activo.",
                "bot"
            );
        } finally {
            sendBtn.disabled = false;
            input.disabled = false;
            input.focus();
        }
    }

    form.addEventListener("submit", function (e) {
        e.preventDefault();
        var txt = input.value.trim();
        if (!txt) return;
        input.value = "";
        send(txt);
    });

    // Enter envía, Shift+Enter inserta salto de línea
    input.addEventListener("keydown", function (e) {
        if (e.key === "Enter" && !e.shiftKey) {
            e.preventDefault();
            form.requestSubmit();
        }
    });

    // Mensaje de bienvenida
    addMessage(
        "Hola, soy el Asistente Buga. ¿En qué puedo ayudarte con tu reclamo hoy?",
        "bot"
    );
    input.focus();
})();
