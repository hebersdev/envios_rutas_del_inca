package enviosrutasdelinca.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import enviosrutasdelinca.dto.CotizacionResponse;
import enviosrutasdelinca.model.Tarifa;
import enviosrutasdelinca.repository.TarifaRepository;
import enviosrutasdelinca.shared.model.ModalidadEnvio;

@Service
public class CotizacionService {

    private static final BigDecimal CERO = new BigDecimal("0.00");

    private final TarifaRepository tarifaRepository;

    public CotizacionService(TarifaRepository tarifaRepository) {
        this.tarifaRepository = tarifaRepository;
    }

    public CotizacionResponse cotizar(
            long origenId,
            long destinoId,
            BigDecimal pesoKg,
            ModalidadEnvio modalidad) {
        if (origenId <= 0 || destinoId <= 0) {
            throw new IllegalArgumentException("Las sedes indicadas no son válidas.");
        }
        if (origenId == destinoId) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_CONTENT,
                    "El origen y el destino no pueden ser iguales"
            );
        }
        if (pesoKg == null
                || pesoKg.compareTo(BigDecimal.ZERO) <= 0
                || pesoKg.scale() > 2) {
            throw new IllegalArgumentException(
                    "El peso debe ser mayor a 0 y admitir como máximo 2 decimales."
            );
        }
        if (modalidad == null) {
            throw new IllegalArgumentException("La modalidad de envío no es válida.");
        }
        if (!tarifaRepository.rutaDisponible(origenId, destinoId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ruta no disponible");
        }

        Tarifa tarifa = tarifaRepository.buscarTarifaActiva(
                        pesoKg)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNPROCESSABLE_CONTENT,
                        mensajePesoNoCubierto()
                ));

        BigDecimal recargo = modalidad == ModalidadEnvio.DOMICILIO
                ? tarifa.recargoDomicilio()
                : CERO;
        BigDecimal total = tarifa.precioBase()
                .add(recargo)
                .setScale(2, RoundingMode.HALF_UP);

        return new CotizacionResponse(
                origenId,
                destinoId,
                pesoKg.setScale(2, RoundingMode.HALF_UP),
                modalidad,
                tarifa.precioBase().setScale(2, RoundingMode.HALF_UP),
                recargo.setScale(2, RoundingMode.HALF_UP),
                total,
                tarifa.rangoPesoMinKg().setScale(2, RoundingMode.HALF_UP),
                tarifa.rangoPesoMaxKg().setScale(2, RoundingMode.HALF_UP)
        );
    }

    private String mensajePesoNoCubierto() {
        return tarifaRepository.pesoMaximoActivo()
                .map(maximo -> "El peso supera el máximo permitido ("
                        + maximo.stripTrailingZeros().toPlainString() + " kg)")
                .orElse("No hay tarifas activas configuradas.");
    }
}
