package enviosrutasdelinca.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import enviosrutasdelinca.shared.model.EstadoEnvio;
import enviosrutasdelinca.shared.model.ModalidadEnvio;

public record EnvioResponse(
        Long id,
        String codigoEnvio,
        EstadoEnvio estado,
        ModalidadEnvio modalidad,
        Remitente remitente,
        Sede origen,
        Sede destino,
        String descripcionPaquete,
        BigDecimal pesoKg,
        BigDecimal altoCm,
        BigDecimal anchoCm,
        BigDecimal largoCm,
        BigDecimal pesoVolumetricoKg,
        BigDecimal pesoFacturableKg,
        String destinatarioNombre,
        String destinatarioDocumento,
        String destinatarioTelefono,
        String direccionEntrega,
        BigDecimal costoEnvio,
        LocalDateTime fechaRegistro,
        boolean puedeCambiarEstado
) {
    public record Remitente(Long id, String nombre, String documento, String telefono) {}

    public record Sede(Long id, String nombre, String ciudad, String direccion) {}

    public EnvioResponse conPuedeCambiarEstado(boolean valor) {
        return new EnvioResponse(
                id, codigoEnvio, estado, modalidad, remitente, origen, destino,
                descripcionPaquete, pesoKg, altoCm, anchoCm, largoCm,
                pesoVolumetricoKg, pesoFacturableKg,
                destinatarioNombre, destinatarioDocumento, destinatarioTelefono,
                direccionEntrega, costoEnvio, fechaRegistro, valor);
    }
}