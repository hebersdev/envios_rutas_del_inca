package enviosrutasdelinca.dto;

import java.time.LocalDateTime;
import enviosrutasdelinca.shared.model.EstadoEnvio;

public record EventoSeguimientoResponse(
        EstadoEnvio estado,
        String texto,
        LocalDateTime fecha,
        String sede
) {
}
