package enviosrutasdelinca.service;

import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import enviosrutasdelinca.dto.EventoSeguimientoResponse;
import enviosrutasdelinca.dto.SeguimientoResponse;
import enviosrutasdelinca.repository.EnvioRepository;
import enviosrutasdelinca.repository.EnvioRepository.CabeceraSeguimiento;
import enviosrutasdelinca.repository.HistorialEstadoEnvioRepository;

@Service
public class SeguimientoService {

    private static final String NO_ENCONTRADO = "No encontramos un envío con ese código.";

    private final EnvioRepository envioRepository;
    private final HistorialEstadoEnvioRepository historialRepository;

    public SeguimientoService(
            EnvioRepository envioRepository,
            HistorialEstadoEnvioRepository historialRepository) {
        this.envioRepository = envioRepository;
        this.historialRepository = historialRepository;
    }

    public SeguimientoResponse consultar(String codigoIngresado) {
        String codigo = codigoIngresado == null
                ? ""
                : codigoIngresado.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);

        if (!codigo.matches("[A-Z0-9-]{1,20}")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, NO_ENCONTRADO);
        }

        CabeceraSeguimiento cabecera = envioRepository.buscarCabeceraSeguimiento(codigo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, NO_ENCONTRADO));

        List<EventoSeguimientoResponse> historial = historialRepository.listarEventos(cabecera.id());

        return new SeguimientoResponse(
                cabecera.codigoEnvio(),
                cabecera.estado(),
                cabecera.estado().textoCliente(),
                cabecera.modalidad(),
                cabecera.origen(),
                cabecera.destino(),
                historial);
    }
}
