package enviosrutasdelinca.service;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import enviosrutasdelinca.shared.security.UsuarioActual;
import enviosrutasdelinca.shared.model.AsignacionSolicitada;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import enviosrutasdelinca.dto.CamionRequest;
import enviosrutasdelinca.dto.CamionResponse;
import enviosrutasdelinca.dto.AgenciaResponse;
import enviosrutasdelinca.model.Usuario;
import enviosrutasdelinca.repository.AgenciaRepository;
import enviosrutasdelinca.repository.CamionRepository;
import enviosrutasdelinca.repository.UsuarioRepository;

@Service
public class CamionService {

    private final CamionRepository camionRepository;
    private final AgenciaRepository agenciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ApplicationEventPublisher eventos;
    private final UsuarioActual usuarioActual;

    public CamionService(
            CamionRepository camionRepository,
            AgenciaRepository agenciaRepository,
            UsuarioRepository usuarioRepository,
            ApplicationEventPublisher eventos,
            UsuarioActual usuarioActual) {
        this.camionRepository = camionRepository;
        this.agenciaRepository = agenciaRepository;
        this.usuarioRepository = usuarioRepository;
        this.eventos = eventos;
        this.usuarioActual = usuarioActual;
    }

    public List<CamionResponse> listar(
            String estado,
            Long agenciaActualId,
            Boolean activo,
            String correoUsuario) {
        Usuario usuario = usuarioActivo(correoUsuario);
        Long agenciaFiltrada = "ENCARGADO_LOGISTICA".equals(usuario.rol())
                ? usuario.agenciaId()
                : agenciaActualId;
        return camionRepository.listar(estado, agenciaFiltrada, activo);
    }

    @Transactional
    public CamionResponse crear(CamionRequest solicitud) {
        camionRepository.bloquearSede(solicitud.agenciaBaseId());
        validarAgenciaBase(solicitud.agenciaBaseId());
        String placa = normalizarPlaca(solicitud.placa());
        validarPlacaDisponible(placa, null);
        long id = camionRepository.crear(
                placa,
                solicitud.pesoMaximoKg(),
                solicitud.volumenMaximoM(),
                solicitud.agenciaBaseId()
        );
        CamionResponse camion = buscarPorId(id);
        solicitarAsignacion(camion);
        return camion;
    }

    @Transactional
    public CamionResponse editar(long id, CamionRequest solicitud) {
        bloquearSinViaje(id);
        validarAgenciaBase(solicitud.agenciaBaseId());
        String placa = normalizarPlaca(solicitud.placa());
        validarPlacaDisponible(placa, id);
        camionRepository.editar(
                id,
                placa,
                solicitud.pesoMaximoKg(),
                solicitud.volumenMaximoM(),
                solicitud.agenciaBaseId()
        );
        return buscarPorId(id);
    }

    @Transactional
    public CamionResponse cambiarEstado(long id, String estado, String correoUsuario) {
        Usuario usuario = usuarioActivo(correoUsuario);
        validarEnSede(buscarPorId(id), usuario);
        CamionResponse camion = bloquearSinViaje(id);
        validarEnSede(camion, usuario);
        if (!camion.activo()) {
            throw new IllegalStateException("No se puede cambiar el estado de un camión dado de baja.");
        }
        camionRepository.cambiarEstado(id, estado);
        CamionResponse actualizado = buscarPorId(id);
        if ("DISPONIBLE".equals(estado)) solicitarAsignacion(actualizado);
        return actualizado;
    }

    @Transactional
    public CamionResponse cambiarActivo(long id, boolean activo) {
        bloquearSinViaje(id);
        camionRepository.cambiarActivo(id, activo);
        CamionResponse actualizado = buscarPorId(id);
        if (activo) solicitarAsignacion(actualizado);
        return actualizado;
    }

    private CamionResponse buscarPorId(long id) {
        return camionRepository.buscarPorId(id)
                .orElseThrow(() -> new NoSuchElementException("El camión no existe."));
    }

    private Usuario usuarioActivo(String correo) {
        Usuario usuario = usuarioRepository.buscarPorCorreo(correo)
                .filter(u -> Boolean.TRUE.equals(u.activo()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "No tienes permiso para esta acción."));
        if (!"ADMINISTRADOR".equals(usuario.rol())
                && !"ENCARGADO_LOGISTICA".equals(usuario.rol())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "No tienes permiso para esta acción.");
        }
        return usuario;
    }

    private void validarEnSede(CamionResponse camion, Usuario usuario) {
        if ("ENCARGADO_LOGISTICA".equals(usuario.rol())) {
            if (!camion.agenciaActualId().equals(usuario.agenciaId())) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No se encontró el camión.");
            }
        }
    }

    private void validarAgenciaBase(long agenciaBaseId) {
        AgenciaResponse agencia = agenciaRepository.buscarPorId(agenciaBaseId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNPROCESSABLE_CONTENT,
                        "La sede base no existe o está inactiva."));
        if (!agencia.activo()) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_CONTENT,
                    "La sede base no existe o está inactiva.");
        }
    }

    private CamionResponse bloquearSinViaje(long id) {
        long sede = camionRepository.agenciaActual(id)
                .orElseThrow(() -> new NoSuchElementException("El camión no existe."));
        camionRepository.bloquearSede(sede);
        var camion = camionRepository.bloquearPorId(id)
                .orElseThrow(() -> new NoSuchElementException("El camión no existe."));
        if (camionRepository.tieneViajeAbierto(id) || "EN_VIAJE".equals(camion.estado())) {
            throw new IllegalStateException("El camión tiene un viaje abierto. Gestiona primero el viaje.");
        }
        return camion;
    }

    private void solicitarAsignacion(CamionResponse camion) {
        if (camion.activo() && "DISPONIBLE".equals(camion.estado())) {
            eventos.publishEvent(new AsignacionSolicitada(camion.agenciaActualId(), usuarioActual.obtener().id()));
        }
    }

    private String normalizarPlaca(String placa) {
        if (placa == null || placa.isBlank()) {
            throw new IllegalArgumentException("La placa es obligatoria.");
        }
        return placa.trim().toUpperCase(Locale.ROOT);
    }

    private void validarPlacaDisponible(String placa, Long id) {
        if (camionRepository.existePlaca(placa, id)) {
            throw new IllegalStateException("Ya existe un camión con esa placa.");
        }
    }
}
