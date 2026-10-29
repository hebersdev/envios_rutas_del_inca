package enviosrutasdelinca.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import enviosrutasdelinca.shared.model.EstadoEnvio;
import enviosrutasdelinca.shared.model.ModalidadEnvio;

public record Envio(
        Long id,
        String codigoEnvio,
        Long clienteId,
        Long counterRecepcionId,
        ModalidadEnvio modalidad,
        String descripcionPaquete,
        BigDecimal pesoKg,
        BigDecimal altoCm,
        BigDecimal anchoCm,
        BigDecimal largoCm,
        String destinatarioNombre,
        String destinatarioDocumento,
        String destinatarioTelefono,
        String direccionEntrega,
        Long agenciaOrigenId,
        Long agenciaDestinoId,
        BigDecimal costoEnvio,
        EstadoEnvio estado,
        Long viajeId,
        Long motorizadoId,
        LocalDateTime fechaRegistro,
        LocalDateTime fechaEntrega
) {
}
