package enviosrutasdelinca.dto;

import enviosrutasdelinca.shared.model.EstadoEnvio;
import jakarta.validation.constraints.NotNull;

public record CambioEstadoEnvioRequest(
        @NotNull(message = "El nuevo estado es obligatorio.")
        EstadoEnvio estado
) {
}
