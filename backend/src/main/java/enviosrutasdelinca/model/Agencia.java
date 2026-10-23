package enviosrutasdelinca.model;

import java.time.LocalDateTime;

public record Agencia (
        Long id,
        String nombre,
        Long ubicacionId,
        boolean activo,
        LocalDateTime fechaCreacion
) {
}