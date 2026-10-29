package enviosrutasdelinca.model;

import java.math.BigDecimal;

public record CargaEnvio(
        long id, String codigoEnvio, long destinoId, String destino,
        BigDecimal pesoKg, BigDecimal altoCm, BigDecimal anchoCm, BigDecimal largoCm) {

    public boolean sinMedidas() {
        return altoCm == null || anchoCm == null || largoCm == null;
    }

    public BigDecimal volumenM() {
        return sinMedidas() ? BigDecimal.ZERO
                : altoCm.multiply(anchoCm).multiply(largoCm).movePointLeft(6);
    }
}
