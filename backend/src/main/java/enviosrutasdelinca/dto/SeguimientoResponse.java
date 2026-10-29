package enviosrutasdelinca.dto;

import java.util.List;

import enviosrutasdelinca.shared.model.EstadoEnvio;
import enviosrutasdelinca.shared.model.ModalidadEnvio;

public record SeguimientoResponse(
        String codigoEnvio,
        EstadoEnvio estado,
        String estadoTexto,
        ModalidadEnvio modalidad,
        String origen,
        String destino,
        List<EventoSeguimientoResponse> historial
) {
}
