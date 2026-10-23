package enviosrutasdelinca.model;

import java.time.LocalDateTime;

public record Viaje (

    Long id,
    Long camionId,
    Long agenciaOrigenId,
    Long agenciaDestinoId,
    String estado,
    LocalDateTime fechaSalida,
    LocalDateTime fechaLlegada,
    Long encargadoSalidaId,
    Long encargadoLlegadaId,
    LocalDateTime fechaCreacion
)   {

}