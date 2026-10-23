package enviosrutasdelinca.service;

import java.util.*;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import enviosrutasdelinca.dto.TarifaRequest;
import enviosrutasdelinca.dto.TarifaResponse;
import enviosrutasdelinca.repository.TarifaRepository;
import enviosrutasdelinca.repository.TarifaRepository.TarifaRequestData;

@Service
public class TarifaService {

    private final TarifaRepository tarifaRepository;

    public TarifaService(TarifaRepository tarifaRepository) {
        this.tarifaRepository = tarifaRepository;
    }

    public List<TarifaResponse> listar(Boolean activo) {
        return tarifaRepository.listar(activo);
    }

    @Transactional
    public TarifaResponse crear(TarifaRequest solicitud) {
        validar(solicitud);
        validarCruce(solicitud, 0);

        long id = tarifaRepository.crear(datos(solicitud));
        return tarifaRepository.buscarPorId(id).orElseThrow();
    }

    @Transactional
    public TarifaResponse editar(long id, TarifaRequest solicitud) {
        boolean activa = tarifaRepository.estaActiva(id)
                .orElseThrow(() -> new NoSuchElementException("La tarifa no existe."));
        validar(solicitud);
        if (activa) {
            validarCruce(solicitud, id);
        }

        tarifaRepository.editar(id, datos(solicitud));
        return tarifaRepository.buscarPorId(id).orElseThrow();
    }

    @Transactional
    public TarifaResponse cambiarEstado(long id, boolean activo) {
        boolean activa = tarifaRepository.estaActiva(id)
                .orElseThrow(() -> new NoSuchElementException("La tarifa no existe."));

        if (activo && !activa) {
            TarifaResponse tarifa = tarifaRepository.buscarPorId(id).orElseThrow();
            validarCruce(new TarifaRequest(
                    tarifa.rangoPesoMinKg(),
                    tarifa.rangoPesoMaxKg(),
                    tarifa.precioBase(),
                    tarifa.recargoDomicilio()
            ), id);
        }

        tarifaRepository.cambiarEstado(id, activo);
        return tarifaRepository.buscarPorId(id).orElseThrow();
    }

    private void validar(TarifaRequest solicitud) {
        if (solicitud == null) {
            throw new IllegalArgumentException("Los datos de la tarifa son obligatorios.");
        }
        if (solicitud.rangoPesoMinKg() != null
                && solicitud.rangoPesoMaxKg() != null
                && solicitud.rangoPesoMaxKg().compareTo(solicitud.rangoPesoMinKg()) <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_CONTENT,
                    "El peso máximo debe ser mayor al mínimo"
            );
        }
    }

    private void validarCruce(TarifaRequest solicitud, long id) {
        if (tarifaRepository.existeCruce(
                solicitud.rangoPesoMinKg(),
                solicitud.rangoPesoMaxKg(),
                id)) {
            throw new IllegalStateException(
                    "El rango se cruza con otra tarifa activa"
            );
        }
    }

    private TarifaRequestData datos(TarifaRequest solicitud) {
        return new TarifaRequestData(
                solicitud.rangoPesoMinKg(),
                solicitud.rangoPesoMaxKg(),
                solicitud.precioBase(),
                solicitud.recargoDomicilio()
        );
    }
}
