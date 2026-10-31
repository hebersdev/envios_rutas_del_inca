package enviosrutasdelinca.service;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class GeneradorCodigoEnvio {

    private static final int LONGITUD = 10;
    private final SecureRandom aleatorio = new SecureRandom();

    public String generar() {
        StringBuilder codigo = new StringBuilder(LONGITUD);
        codigo.append(aleatorio.nextInt(9) + 1);
        for (int posicion = 1; posicion < LONGITUD; posicion++) {
            codigo.append(aleatorio.nextInt(10));
        }
        return codigo.toString();
    }
}
