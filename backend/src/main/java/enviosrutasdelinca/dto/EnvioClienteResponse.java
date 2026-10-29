package enviosrutasdelinca.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import enviosrutasdelinca.shared.model.EstadoEnvio;
import enviosrutasdelinca.shared.model.ModalidadEnvio;

public record EnvioClienteResponse(String codigoEnvio, EstadoEnvio estado, ModalidadEnvio modalidad,
        String origen, String destino, LocalDateTime fechaRegistro, LocalDateTime fechaEntrega,
        BigDecimal costoEnvio, String descripcionPaquete, BigDecimal pesoKg,
        String destinatarioNombre, String direccionEntrega) {
}
