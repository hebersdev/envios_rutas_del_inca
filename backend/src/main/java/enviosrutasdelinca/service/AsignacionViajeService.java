package enviosrutasdelinca.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import enviosrutasdelinca.model.CapacidadCarga;
import enviosrutasdelinca.model.CargaEnvio;
import enviosrutasdelinca.repository.ViajeRepository;
import enviosrutasdelinca.repository.ViajeRepository.CamionDisponible;
import enviosrutasdelinca.repository.HistorialEstadoEnvioRepository;
import enviosrutasdelinca.shared.model.EstadoEnvio;

@Service
public class AsignacionViajeService {

    private final ViajeRepository viajes;
    private final HistorialEstadoEnvioRepository historial;

    public AsignacionViajeService(ViajeRepository viajes, HistorialEstadoEnvioRepository historial) {
        this.viajes = viajes;
        this.historial = historial;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int asignar(long agenciaId, long usuarioId) {
        viajes.bloquearSede(agenciaId);
        Map<Long, List<CargaEnvio>> destinos = new LinkedHashMap<>();
        for (CargaEnvio envio : viajes.pendientes(agenciaId)) {
            destinos.computeIfAbsent(envio.destinoId(), id -> new ArrayList<>()).add(envio);
        }
        int asignados = 0;
        for (var grupo : destinos.entrySet()) {
            List<CargaEnvio> pendientes = grupo.getValue();
            for (var viaje : viajes.programados(agenciaId, grupo.getKey())) {
                asignados += cargar(pendientes, viaje.id(), viaje.placa(),
                        new CapacidadCarga(viaje.pesoCargadoKg(), viaje.volumenCargadoM()),
                        viaje.pesoMaximoKg(), viaje.volumenMaximoM(), usuarioId);
            }
            List<CamionDisponible> libres = new ArrayList<>(viajes.camionesLibres(agenciaId));
            while (!pendientes.isEmpty() && !libres.isEmpty()) {
                CapacidadCarga total = pendientes.stream().reduce(CapacidadCarga.VACIA,
                        CapacidadCarga::sumar, (a, b) -> new CapacidadCarga(
                                a.pesoKg().add(b.pesoKg()), a.volumenM().add(b.volumenM())));
                Comparator<CamionDisponible> orden = Comparator.comparing(CamionDisponible::pesoMaximoKg)
                        .thenComparing(CamionDisponible::volumenMaximoM).thenComparingLong(CamionDisponible::id);
                CamionDisponible camion = libres.stream()
                        .filter(c -> total.cabeEn(c.pesoMaximoKg(), c.volumenMaximoM()))
                        .min(orden).orElseGet(() -> libres.stream().max(orden).orElseThrow());
                libres.remove(camion);
                boolean algunoCabe = pendientes.stream().anyMatch(e -> CapacidadCarga.VACIA.sumar(e)
                        .cabeEn(camion.pesoMaximoKg(), camion.volumenMaximoM()));
                if (!algunoCabe) continue;
                long viajeId = viajes.crear(camion.id(), agenciaId, grupo.getKey());
                asignados += cargar(pendientes, viajeId, camion.placa(), CapacidadCarga.VACIA,
                        camion.pesoMaximoKg(), camion.volumenMaximoM(), usuarioId);
            }
        }
        return asignados;
    }

    private int cargar(List<CargaEnvio> pendientes, long viajeId, String placa, CapacidadCarga carga,
            java.math.BigDecimal pesoMaximo, java.math.BigDecimal volumenMaximo, long usuarioId) {
        int asignados = 0;
        var iterador = pendientes.iterator();
        while (iterador.hasNext()) {
            CargaEnvio envio = iterador.next();
            CapacidadCarga siguiente = carga.sumar(envio);
            if (!siguiente.cabeEn(pesoMaximo, volumenMaximo)) continue;
            if (viajes.asignar(envio.id(), viajeId) == 1) {
                historial.registrar(envio.id(), EstadoEnvio.ASIGNADO_A_VIAJE, usuarioId,
                        "Asignado automáticamente al viaje " + viajeId + " (camión " + placa + ")");
                carga = siguiente;
                asignados++;
            }
            iterador.remove();
        }
        return asignados;
    }
}
