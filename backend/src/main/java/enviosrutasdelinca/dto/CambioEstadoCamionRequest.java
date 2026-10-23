package enviosrutasdelinca.dto;

import jakarta.validation.constraints.*;

public record CambioEstadoCamionRequest(
        @NotBlank(message = "Selecciona un estado.")
        @Pattern(
                regexp = "DISPONIBLE|EN_MANTENIMIENTO",
                message = "El estado solicitado no es válido."
        )
        String estado) {
}
