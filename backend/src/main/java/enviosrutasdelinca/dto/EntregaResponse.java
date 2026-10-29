package enviosrutasdelinca.dto;

import java.time.LocalDateTime;
import enviosrutasdelinca.shared.model.EstadoEnvio;
import enviosrutasdelinca.shared.model.ModalidadEnvio;

public record EntregaResponse(long id, String codigoEnvio, EstadoEnvio estado, ModalidadEnvio modalidad,
        String destinatarioNombre, String destinatarioTelefono, String direccionEntrega,
        LocalDateTime fechaLlegada, Long motorizadoId, String motorizadoNombre, String sedeDestino) {
}
