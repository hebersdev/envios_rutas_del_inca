package enviosrutasdelinca.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AgenciaRequest(
        @NotBlank(message = "El nombre de la sede es obligatorio.")
        @Size(max = 150, message = "El nombre no puede superar 150 caracteres.")
        String nombre,
        @NotBlank(message = "La dirección es obligatoria.")
        @Size(max = 255, message = "La dirección no puede superar 255 caracteres.")
        String direccion,
        @NotBlank(message = "La ciudad es obligatoria.")
        @Size(max = 100, message = "La ciudad no puede superar 100 caracteres.")
        String ciudad,
        @NotBlank(message = "El departamento es obligatorio.")
        @Size(max = 100, message = "El departamento no puede superar 100 caracteres.")
        String departamento
) {
}
