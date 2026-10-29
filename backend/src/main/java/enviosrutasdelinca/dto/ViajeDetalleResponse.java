package enviosrutasdelinca.dto;

import java.util.List;
import enviosrutasdelinca.model.CargaEnvio;

public record ViajeDetalleResponse(ViajeResponse viaje, List<CargaEnvio> envios) {
}
