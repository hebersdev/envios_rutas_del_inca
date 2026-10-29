package enviosrutasdelinca.dto;

import java.util.List;
import jakarta.validation.constraints.*;

public record AsignarMotorizadoRequest(
        @NotEmpty(message = "Selecciona al menos un envío.")
        @Size(max = 100, message = "Selecciona como máximo 100 envíos por operación.")
        List<@NotNull @Positive Long> envioIds,
        @NotNull(message = "Selecciona un motorizado.") @Positive Long motorizadoId) {
}
