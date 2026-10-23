package enviosrutasdelinca.model;

import java.math.BigDecimal;

public record Tarifa(
    Long id,
    BigDecimal rangoPesoMinKg,
    BigDecimal rangoPesoMaxKg,
    BigDecimal precioBase,
    BigDecimal recargoDomicilio,
    Boolean activo
) {
}