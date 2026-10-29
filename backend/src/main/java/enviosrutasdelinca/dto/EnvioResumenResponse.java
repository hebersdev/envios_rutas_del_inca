package enviosrutasdelinca.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import enviosrutasdelinca.shared.model.EstadoEnvio;
import enviosrutasdelinca.shared.model.ModalidadEnvio;

public record EnvioResumenResponse(
        Long id,
        String codigoEnvio,
        EstadoEnvio estado,
        ModalidadEnvio modalidad,
        BigDecimal pesoKg,
        BigDecimal costoEnvio,
        LocalDateTime fechaRegistro,
        String remitenteNombre,
        String destinoNombre
) {
}
