package enviosrutasdelinca.service;

import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import enviosrutasdelinca.dto.EnvioPendienteResponse;
import enviosrutasdelinca.dto.ViajeDetalleResponse;
import enviosrutasdelinca.dto.ViajeResponse;
import enviosrutasdelinca.model.Usuario;
import enviosrutasdelinca.repository.CamionRepository;
import enviosrutasdelinca.repository.HistorialEstadoEnvioRepository;
import enviosrutasdelinca.repository.ViajeRepository;
import enviosrutasdelinca.shared.model.AsignacionSolicitada;
import enviosrutasdelinca.shared.model.EstadoEnvio;
import enviosrutasdelinca.shared.model.EstadoViaje;

@Service
public class ViajeService {

    private final ViajeRepository viajes;
    private final CamionRepository camiones;
    private final HistorialEstadoEnvioRepository historial;
    private final ApplicationEventPublisher eventos;

    public ViajeService(ViajeRepository viajes, CamionRepository camiones,
            HistorialEstadoEnvioRepository historial, ApplicationEventPublisher eventos) {
        this.viajes = viajes;
        this.camiones = camiones;
        this.historial = historial;
        this.eventos = eventos;
    }

    @Transactional(readOnly = true)
    public List<ViajeResponse> listar(Usuario usuario, EstadoViaje estado) {
        return viajes.listar(sedeConsulta(usuario), estado);
    }

    @Transactional(readOnly = true)
    public ViajeDetalleResponse obtener(long id, Usuario usuario) {
        Long sede = sedeConsulta(usuario);
        ViajeResponse viaje = buscar(id, false);
        if (sede != null && sede != viaje.agenciaOrigenId() && sede != viaje.agenciaDestinoId()) {
            throw noEncontrado();
        }
        return new ViajeDetalleResponse(viaje, viajes.envios(id));
    }

    @Transactional
    public List<EnvioPendienteResponse> pendientes(Usuario usuario) {
        long sede = sedeCoordinador(usuario);
        return viajes.pendientes(sede).stream()
                .map(e -> new EnvioPendienteResponse(e, viajes.cabeEnLaFlota(e)
                        ? "No hay camión disponible en la sede"
                        : "No cabe en ningún camión de la empresa"))
                .toList();
    }

    public long sedeCoordinador(Usuario usuario) {
        if (!"ENCARGADO_LOGISTICA".equals(usuario.rol()) || usuario.agenciaId() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el coordinador puede realizar esta acción.");
        }
        return usuario.agenciaId();
    }

    @Transactional
    public void despachar(long id, Usuario usuario) {
        ViajeResponse viaje = bloquearParaOperacion(id, usuario, true);
        exigirEstado(viaje, EstadoViaje.PROGRAMADO);
        var camion = camiones.bloquearPorId(viaje.camionId()).orElseThrow(ViajeService::noEncontrado);
        if (!camion.activo() || !"DISPONIBLE".equals(camion.estado())
                || camion.agenciaActualId() != viaje.agenciaOrigenId()) {
            throw conflicto("El camión no está disponible en la sede de origen.");
        }
        var envios = viajes.envios(id);
        if (envios.isEmpty()) throw conflicto("El viaje debe tener al menos un envío.");
        var carga = new enviosrutasdelinca.model.CapacidadCarga(viaje.pesoCargadoKg(), viaje.volumenCargadoM());
        if (!carga.cabeEn(camion.pesoMaximoKg(), camion.volumenMaximoM())) {
            throw conflicto("La carga supera la capacidad del camión.");
        }
        viajes.despachar(id, usuario.id());
        viajes.actualizarCamion(viaje.camionId(), "EN_VIAJE", viaje.agenciaOrigenId());
        cambiarEnvios(id, EstadoEnvio.EN_RUTA, false, usuario.id(), "Viaje " + id + " despachado");
    }

    @Transactional
    public void llegada(long id, Usuario usuario) {
        ViajeResponse viaje = bloquearParaOperacion(id, usuario, false);
        exigirEstado(viaje, EstadoViaje.EN_RUTA);
        camiones.bloquearPorId(viaje.camionId()).orElseThrow(ViajeService::noEncontrado);
        viajes.registrarLlegada(id, usuario.id());
        viajes.actualizarCamion(viaje.camionId(), "DISPONIBLE", viaje.agenciaDestinoId());
        cambiarEnvios(id, EstadoEnvio.LLEGO_A_AGENCIA_DESTINO, false, usuario.id(),
                "Llegada del viaje " + id + " a la agencia de destino");
        eventos.publishEvent(new AsignacionSolicitada(viaje.agenciaDestinoId(), usuario.id()));
    }

    @Transactional
    public void finalizar(long id, Usuario usuario) {
        ViajeResponse viaje = bloquearParaOperacion(id, usuario, false);
        exigirEstado(viaje, EstadoViaje.LLEGADO);
        viajes.finalizar(id);
    }

    @Transactional
    public void cancelar(long id, Usuario usuario) {
        ViajeResponse viaje = bloquearParaOperacion(id, usuario, true);
        exigirEstado(viaje, EstadoViaje.PROGRAMADO);
        cambiarEnvios(id, EstadoEnvio.REGISTRADO, true, usuario.id(), "Viaje " + id + " cancelado");
        viajes.eliminar(id);
    }

    private void cambiarEnvios(long viajeId, EstadoEnvio estado, boolean liberar, long usuarioId, String mensaje) {
        for (long envioId : viajes.cambiarEnvios(viajeId, estado.name(), liberar)) {
            historial.registrar(envioId, estado, usuarioId, mensaje);
        }
    }

    private ViajeResponse bloquearParaOperacion(long id, Usuario usuario, boolean origen) {
        long sede = sedeCoordinador(usuario);
        viajes.bloquearSede(sede);
        ViajeResponse viaje = buscar(id, true);
        if (sede != (origen ? viaje.agenciaOrigenId() : viaje.agenciaDestinoId())) throw noEncontrado();
        return viaje;
    }

    private Long sedeConsulta(Usuario usuario) {
        return sedeCoordinador(usuario);
    }

    private ViajeResponse buscar(long id, boolean bloquear) {
        return viajes.buscar(id, bloquear).orElseThrow(ViajeService::noEncontrado);
    }

    private void exigirEstado(ViajeResponse viaje, EstadoViaje estado) {
        if (viaje.estado() != estado) throw conflicto("El viaje debe estar en estado " + estado + ".");
    }

    private static ResponseStatusException noEncontrado() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el viaje.");
    }

    private static ResponseStatusException conflicto(String mensaje) {
        return new ResponseStatusException(HttpStatus.CONFLICT, mensaje);
    }
}
