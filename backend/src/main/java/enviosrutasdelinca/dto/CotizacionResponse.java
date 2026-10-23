package enviosrutasdelinca.dto;

import java.math.BigDecimal;

import enviosrutasdelinca.shared.model.ModalidadEnvio;

public record CotizacionResponse(
        Long origenId,
        Long destinoId,
        BigDecimal pesoKg,
        ModalidadEnvio modalidad,
        BigDecimal precioBase,
        BigDecimal recargoDomicilio,
        BigDecimal total,
        BigDecimal rangoPesoMinKg,
        BigDecimal rangoPesoMaxKg) {
}
