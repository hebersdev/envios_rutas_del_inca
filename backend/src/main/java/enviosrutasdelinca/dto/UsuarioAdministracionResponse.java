package enviosrutasdelinca.dto;

public record UsuarioAdministracionResponse(
        Long id,
        String nombre,
        String correo,
        Long agenciaId,
        String sede,
        String rol,
        Boolean activo
) {
}