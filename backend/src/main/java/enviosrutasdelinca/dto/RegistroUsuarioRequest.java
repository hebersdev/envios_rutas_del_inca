package enviosrutasdelinca.dto;

import jakarta.validation.constraints.*;

public record RegistroUsuarioRequest(
        @NotBlank(message = "El nombre o razón social es obligatorio.")
        @Size(max = 150, message = "El nombre no puede superar 150 caracteres.")
        String nombre,
        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "Ingresa un correo válido.")
        @Size(max = 150, message = "El correo no puede superar 150 caracteres.")
        String correo,
        @Size(max = 20, message = "El teléfono no puede superar 20 caracteres.")
        String telefono,
        @NotBlank(message = "La contraseña es obligatoria.")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres.")
        String clave,
        @NotBlank(message = "Selecciona el tipo de cliente.")
        @Pattern(regexp = "PERSONA_NATURAL|EMPRESA", message = "El tipo de cliente no es válido.")
        String tipoPersona,
        @Pattern(regexp = "[0-9]{8}", message = "El DNI debe tener exactamente 8 dígitos.")
        String dni,
        @Pattern(regexp = "[0-9]{11}", message = "El RUC debe tener exactamente 11 dígitos.")
        String ruc) {
}