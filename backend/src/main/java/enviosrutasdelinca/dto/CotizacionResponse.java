package enviosrutasdelinca.dto;

import java.math.BigDecimal;

import enviosrutasdelinca.shared.model.ModalidadEnvio;

public record CotizacionResponse(
        long origenId,
        long destinoId,
        BigDecimal pesoKg,
        BigDecimal pesoVolumetricoKg,
        BigDecimal pesoFacturableKg,
        ModalidadEnvio modalidad,
        BigDecimal precioBase,
        BigDecimal recargo,
        BigDecimal total,
        BigDecimal rangoPesoMinKg,
        BigDecimal rangoPesoMaxKg
) {
}