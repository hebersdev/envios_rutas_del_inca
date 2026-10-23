package enviosrutasdelinca.dto;

import jakarta.validation.constraints.*;

public record ActualizarRolUsuarioRequest(
        @NotBlank(message = "Selecciona un rol.")
        @Pattern(regexp = "ADMINISTRADOR|ENCARGADO_LOGISTICA|COUNTER|MOTORIZADO", message = "El rol no es válido.")
        String rol,
        Long agenciaId
) {
}