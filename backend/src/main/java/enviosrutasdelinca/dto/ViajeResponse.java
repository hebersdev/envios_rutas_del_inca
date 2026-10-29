package enviosrutasdelinca.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import enviosrutasdelinca.shared.model.EstadoViaje;

public record ViajeResponse(
        long id, long camionId, String placa, long agenciaOrigenId, String origen,
        long agenciaDestinoId, String destino, EstadoViaje estado,
        BigDecimal pesoMaximoKg, BigDecimal volumenMaximoM,
        BigDecimal pesoCargadoKg, BigDecimal volumenCargadoM, long cantidadEnvios,
        boolean sinMedidas, LocalDateTime fechaSalida, LocalDateTime fechaLlegada) {
}
