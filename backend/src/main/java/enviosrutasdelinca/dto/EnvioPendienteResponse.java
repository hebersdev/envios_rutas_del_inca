package enviosrutasdelinca.dto;

import enviosrutasdelinca.model.CargaEnvio;

public record EnvioPendienteResponse(CargaEnvio envio, String motivo) {
}
