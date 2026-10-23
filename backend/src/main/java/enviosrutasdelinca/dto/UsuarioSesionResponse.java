package enviosrutasdelinca.dto;

public record UsuarioSesionResponse(
        String nombre,
        String correo,
        String rol,
        Long agenciaId
) {
}
