package enviosrutasdelinca.shared.documentos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import enviosrutasdelinca.dto.EnvioResponse;
import enviosrutasdelinca.shared.model.ModalidadEnvio;

public final class FormatoEnvio {

    public static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private FormatoEnvio() {
    }

    public static String texto(String valor) {
        return valor == null || valor.isBlank() ? "No registrado" : valor.strip();
    }

    public static String decimal(BigDecimal valor) {
        return valor == null ? "—" : valor.stripTrailingZeros().toPlainString();
    }

    public static String moneda(BigDecimal valor) {
        return "S/ " + valor.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    public static String modalidad(EnvioResponse envio) {
        return envio.modalidad() == ModalidadEnvio.AGENCIA ? "RECOJO EN AGENCIA" : "ENTREGA A DOMICILIO";
    }

    public static String direccion(EnvioResponse envio) {
        return envio.modalidad() == ModalidadEnvio.AGENCIA
                ? envio.destino().nombre() + " · " + envio.destino().direccion()
                : texto(envio.direccionEntrega());
    }

    public static String medidas(EnvioResponse envio) {
        if (envio.altoCm() == null || envio.anchoCm() == null || envio.largoCm() == null) return "No registradas";
        return decimal(envio.altoCm()) + " × " + decimal(envio.anchoCm()) + " × " + decimal(envio.largoCm()) + " cm";
    }
}
