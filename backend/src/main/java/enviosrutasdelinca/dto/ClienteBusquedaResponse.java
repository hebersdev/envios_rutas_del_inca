package enviosrutasdelinca.dto;

public record ClienteBusquedaResponse(
        Long id,
        String nombre,
        String correo,
        String telefono,
        String documento
) {
}