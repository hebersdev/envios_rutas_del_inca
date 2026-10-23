package enviosrutasdelinca.dto;

import java.math.BigDecimal;

public record TarifaResponse(
        Long id,
        BigDecimal rangoPesoMinKg,
        BigDecimal rangoPesoMaxKg,
        BigDecimal precioBase,
        BigDecimal recargoDomicilio,
        Boolean activo) {
}
