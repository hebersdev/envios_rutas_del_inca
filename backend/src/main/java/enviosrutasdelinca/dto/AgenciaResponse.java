package enviosrutasdelinca.dto;

public record AgenciaResponse(
        Long id,
        String nombre,
        String direccion,
        String ciudad,
        String departamento,
        boolean activo
) {
}
