package enviosrutasdelinca.model;

import java.math.BigDecimal;

public record CapacidadCarga(BigDecimal pesoKg, BigDecimal volumenM) {

    public static final BigDecimal LIMITE_OCUPACION = BigDecimal.ONE;
    public static final CapacidadCarga VACIA = new CapacidadCarga(BigDecimal.ZERO, BigDecimal.ZERO);

    public CapacidadCarga sumar(CargaEnvio envio) {
        return new CapacidadCarga(pesoKg.add(envio.pesoKg()), volumenM.add(envio.volumenM()));
    }

    public boolean cabeEn(BigDecimal pesoMaximo, BigDecimal volumenMaximo) {
        return pesoKg.compareTo(pesoMaximo.multiply(LIMITE_OCUPACION)) <= 0
                && volumenM.compareTo(volumenMaximo.multiply(LIMITE_OCUPACION)) <= 0;
    }
}
