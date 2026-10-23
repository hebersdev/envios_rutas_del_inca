package enviosrutasdelinca.dto;

import java.math.BigDecimal;

public record CamionResponse(
        Long id,
        String placa,
        BigDecimal pesoMaximoKg,
        BigDecimal volumenMaximoM3,
        Long agenciaBaseId,
        String agenciaBaseNombre,
        Long agenciaActualId,
        String agenciaActualNombre,
        String estado,
        Boolean activo) {
}
