package enviosrutasdelinca.dto;

import jakarta.validation.constraints.*;

public record UsuarioAdministracionRequest(
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 150, message = "El nombre no puede superar 150 caracteres.")
        String nombre,
        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "Ingresa un correo válido.")
        @Size(max = 150, message = "El correo no puede superar 150 caracteres.")
        String correo,
        @NotBlank(message = "La contraseña es obligatoria.")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres.")
        String clave,
        @NotBlank(message = "Selecciona un rol.")
        @Pattern(regexp = "ADMINISTRADOR|ENCARGADO_LOGISTICA|COUNTER|MOTORIZADO", message = "El rol no es válido.")
        String rol,
        Long agenciaId
) {
}