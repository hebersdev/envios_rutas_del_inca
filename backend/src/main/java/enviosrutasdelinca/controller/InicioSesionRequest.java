package enviosrutasdelinca.controller;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record InicioSesionRequest(
        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "Ingresa un correo válido.")
        String correo,
        @NotBlank(message = "La contraseña es obligatoria.")
        String clave) {
}