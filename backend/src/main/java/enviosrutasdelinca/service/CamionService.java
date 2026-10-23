package enviosrutasdelinca.service;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
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

    public CamionService(
            CamionRepository camionRepository,
            AgenciaRepository agenciaRepository,
            UsuarioRepository usuarioRepository) {
        this.camionRepository = camionRepository;
        this.agenciaRepository = agenciaRepository;
        this.usuarioRepository = usuarioRepository;
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
        validarAgenciaBase(solicitud.agenciaBaseId());
        String placa = normalizarPlaca(solicitud.placa());
        validarPlacaDisponible(placa, null);
        long id = camionRepository.crear(
                placa,
                solicitud.pesoMaximoKg(),
                solicitud.volumenMaximoM3(),
                solicitud.agenciaBaseId()
        );
        return buscarPorId(id);
    }

    @Transactional
    public CamionResponse editar(long id, CamionRequest solicitud) {
        String estado = camionRepository.buscarEstado(id)
                .orElseThrow(() -> new NoSuchElementException("El camión no existe."));
        if ("EN_VIAJE".equals(estado)) {
            throw new IllegalStateException("En viaje");
        }
        validarAgenciaBase(solicitud.agenciaBaseId());
        String placa = normalizarPlaca(solicitud.placa());
        validarPlacaDisponible(placa, id);
        camionRepository.editar(
                id,
                placa,
                solicitud.pesoMaximoKg(),
                solicitud.volumenMaximoM3(),
                solicitud.agenciaBaseId()
        );
        return buscarPorId(id);
    }

    @Transactional
    public CamionResponse cambiarEstado(long id, String estado, String correoUsuario) {
        String estadoActual = camionRepository.buscarEstado(id)
                .orElseThrow(() -> new NoSuchElementException("El camión no existe."));
        validarEnSede(id, correoUsuario);
        if (!Boolean.TRUE.equals(camionRepository.buscarPorId(id).orElseThrow().activo())) {
            throw new IllegalStateException("No se puede cambiar el estado de un camión dado de baja.");
        }
        if ("EN_VIAJE".equals(estadoActual)) {
            throw new IllegalStateException("En viaje");
        }
        camionRepository.cambiarEstado(id, estado);
        return buscarPorId(id);
    }

    @Transactional
    public CamionResponse cambiarActivo(long id, boolean activo) {
        String estado = camionRepository.buscarEstado(id)
                .orElseThrow(() -> new NoSuchElementException("El camión no existe."));
        if ("EN_VIAJE".equals(estado)) {
            throw new IllegalStateException("En viaje");
        }
        camionRepository.cambiarActivo(id, activo);
        return buscarPorId(id);
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

    private void validarEnSede(long camionId, String correoUsuario) {
        Usuario usuario = usuarioActivo(correoUsuario);
        if ("ENCARGADO_LOGISTICA".equals(usuario.rol())) {
            Long agenciaActual = camionRepository.agenciaActual(camionId).orElseThrow();
            if (!agenciaActual.equals(usuario.agenciaId())) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "El camión no pertenece a tu sede actual.");
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
