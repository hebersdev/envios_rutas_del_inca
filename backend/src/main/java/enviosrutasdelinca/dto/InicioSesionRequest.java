package enviosrutasdelinca.dto;

import jakarta.validation.constraints.*;

public record InicioSesionRequest(
        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "Ingresa un correo válido.")
        String correo,
        @NotBlank(message = "La contraseña es obligatoria.")
        String clave) {
}