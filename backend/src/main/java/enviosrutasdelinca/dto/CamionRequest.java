package enviosrutasdelinca.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;

public record CamionRequest(
        @NotBlank(message = "La placa es obligatoria.")
        @Size(max = 15, message = "La placa no puede superar 15 caracteres.")
        @Pattern(regexp = "[A-Za-z0-9-]+", message = "La placa contiene caracteres no válidos.")
        String placa,
        @NotNull(message = "La capacidad de peso es obligatoria.")
        @DecimalMin(value = "0.01", message = "La capacidad de peso debe ser mayor a 0.")
        @Digits(integer = 8, fraction = 2, message = "La capacidad de peso admite hasta 2 decimales.")
        BigDecimal pesoMaximoKg,
        @NotNull(message = "La capacidad de volumen es obligatoria.")
        @DecimalMin(value = "0.01", message = "La capacidad de volumen debe ser mayor a 0.")
        @Digits(integer = 8, fraction = 2, message = "La capacidad de volumen admite hasta 2 decimales.")
        BigDecimal volumenMaximoM,
        @NotNull(message = "Selecciona la sede base.")
        @Positive(message = "La sede base no es válida.")
        Long agenciaBaseId) {
}
