package enviosrutasdelinca.model;

import java.time.LocalDateTime;

public record Usuario (

    Long id,
    String nombre,
    String correo,
    String clave,
    String telefono,
    String dni,
    String ruc,
    String rol,
    Long agenciaId,
    Boolean activo,
    LocalDateTime fechaRegistro

) {

}