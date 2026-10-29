package enviosrutasdelinca.model;

import java.math.BigDecimal;

public record Camion (
    Long id,
    String placa,
    BigDecimal pesoMaximoKg,
    BigDecimal volumenMaximoM,
    Long agenciaBaseId,
    Long agenciaActualId,
    String estado,
    Boolean activo
) {
}