package enviosrutasdelinca.dto;

import java.math.BigDecimal;

import enviosrutasdelinca.shared.model.ModalidadEnvio;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record EnvioRequest(
        @Positive(message = "El cliente no es válido.")
        Long clienteId,

        @NotNull(message = "Selecciona la sede de destino.")
        @Positive(message = "La sede de destino no es válida.")
        Long agenciaDestinoId,

        @NotNull(message = "Selecciona la modalidad de entrega.")
        ModalidadEnvio modalidad,

        @Size(max = 500, message = "La descripción admite hasta 500 caracteres.")
        String descripcionPaquete,

        @NotNull(message = "El peso es obligatorio.")
        @DecimalMin(value = "0.01", message = "El peso debe ser mayor a 0.")
        @Digits(integer = 8, fraction = 2, message = "El peso admite hasta 2 decimales.")
        BigDecimal pesoKg,

        @DecimalMin(value = "0.01", message = "El alto debe ser mayor a 0.")
        @Digits(integer = 8, fraction = 2, message = "El alto admite hasta 2 decimales.")
        BigDecimal altoCm,

        @DecimalMin(value = "0.01", message = "El ancho debe ser mayor a 0.")
        @Digits(integer = 8, fraction = 2, message = "El ancho admite hasta 2 decimales.")
        BigDecimal anchoCm,

        @DecimalMin(value = "0.01", message = "El largo debe ser mayor a 0.")
        @Digits(integer = 8, fraction = 2, message = "El largo admite hasta 2 decimales.")
        BigDecimal largoCm,

        @NotBlank(message = "Indica el nombre del remitente.")
        @Size(max = 150, message = "El nombre del remitente admite hasta 150 caracteres.")
        String remitenteNombre,

        @NotBlank(message = "Indica el documento del remitente.")
        @Pattern(regexp = "\\d{8}|\\d{11}",
                message = "El documento debe ser DNI (8 dígitos) o RUC (11 dígitos).")
        String remitenteDocumento,

        @Size(max = 20, message = "El teléfono del remitente admite hasta 20 caracteres.")
        String remitenteTelefono,

        @NotBlank(message = "Indica el nombre y el documento de quien recibe.")
        @Size(max = 150, message = "El nombre de quien recibe admite hasta 150 caracteres.")
        String destinatarioNombre,

        @NotBlank(message = "Indica el nombre y el documento de quien recibe.")
        @Pattern(regexp = "\\d{8}|\\d{11}",
                message = "Ingresa un DNI de 8 dígitos o un RUC de 11 para quien recibe.")
        String destinatarioDocumento,

        @Size(max = 20, message = "El teléfono de quien recibe admite hasta 20 caracteres.")
        String destinatarioTelefono,

        @Size(max = 255, message = "La dirección de entrega admite hasta 255 caracteres.")
        String direccionEntrega
) {

    public EnvioRequest {
        descripcionPaquete = limpiar(descripcionPaquete);
        remitenteNombre = limpiar(remitenteNombre);
        remitenteDocumento = limpiar(remitenteDocumento);
        remitenteTelefono = limpiar(remitenteTelefono);
        destinatarioNombre = limpiar(destinatarioNombre);
        destinatarioDocumento = limpiar(destinatarioDocumento);
        destinatarioTelefono = limpiar(destinatarioTelefono);
        direccionEntrega = modalidad == ModalidadEnvio.DOMICILIO
                ? limpiar(direccionEntrega)
                : null;
    }

    private static String limpiar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}