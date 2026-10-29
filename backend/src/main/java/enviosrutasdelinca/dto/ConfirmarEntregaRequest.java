package enviosrutasdelinca.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ConfirmarEntregaRequest(
        @NotBlank(message = "Indica el documento de quien recibe.")
        @Pattern(regexp = "[0-9]{8}|[0-9]{11}", message = "Escribe un DNI de 8 dígitos o un RUC de 11 dígitos.")
        String documento) {
}
