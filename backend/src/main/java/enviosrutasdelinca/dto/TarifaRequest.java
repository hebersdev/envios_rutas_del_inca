package enviosrutasdelinca.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;

public record TarifaRequest(
        @NotNull(message = "El peso mínimo es obligatorio.")
        @DecimalMin(value = "0.00", message = "El peso mínimo no puede ser negativo.")
        @Digits(integer = 8, fraction = 2, message = "El peso mínimo admite hasta 2 decimales.")
        BigDecimal rangoPesoMinKg,
        @NotNull(message = "El peso máximo es obligatorio.")
        @DecimalMin(value = "0.01", message = "El peso máximo debe ser mayor a 0.")
        @Digits(integer = 8, fraction = 2, message = "El peso máximo admite hasta 2 decimales.")
        BigDecimal rangoPesoMaxKg,
        @NotNull(message = "El precio base es obligatorio.")
        @DecimalMin(value = "0.01", message = "El precio base debe ser mayor a 0.")
        @Digits(integer = 8, fraction = 2, message = "El precio base admite hasta 2 decimales.")
        BigDecimal precioBase,
        @DecimalMin(value = "0.00", message = "El recargo a domicilio no puede ser negativo.")
        @Digits(integer = 8, fraction = 2, message = "El recargo admite hasta 2 decimales.")
        BigDecimal recargoDomicilio) {

    public TarifaRequest {
        if (recargoDomicilio == null) {
            recargoDomicilio = BigDecimal.ZERO;
        }
    }
}
