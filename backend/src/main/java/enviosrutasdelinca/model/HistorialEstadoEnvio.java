package enviosrutasdelinca.model;

import java.time.LocalDateTime;

public record HistorialEstadoEnvio (
        Long id,
        Long envioId,
        String estado,
        LocalDateTime fecha,
        Long usuarioId,
        String comentario
) {
}