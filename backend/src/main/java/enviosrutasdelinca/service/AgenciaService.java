package enviosrutasdelinca.service;

import enviosrutasdelinca.dto.AgenciaResumenResponse;
import enviosrutasdelinca.dto.AgenciaRequest;
import enviosrutasdelinca.dto.AgenciaResponse;
import enviosrutasdelinca.repository.AgenciaRepository;
import enviosrutasdelinca.repository.UbicacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AgenciaService {

    private final AgenciaRepository agenciaRepository;
    private final UbicacionRepository ubicacionRepository;

    public AgenciaService(
            AgenciaRepository agenciaRepository,
            UbicacionRepository ubicacionRepository) {
        this.agenciaRepository = agenciaRepository;
        this.ubicacionRepository = ubicacionRepository;
    }

    public List<AgenciaResumenResponse> listarActivas() {
        return agenciaRepository.listarActivas();
    }

    public List<AgenciaResponse> listar(Boolean activo) {
        return agenciaRepository.listar(activo);
    }

    public AgenciaResponse buscarPorId(long id) {
        return agenciaRepository.buscarPorId(id)
                .orElseThrow(() -> new NoSuchElementException("No se encontró la sede."));
    }

    @Transactional
    public AgenciaResponse crear(AgenciaRequest solicitud) {
        String nombre = solicitud.nombre().trim();
        validarNombreUnico(nombre, null);

        long ubicacionId = obtenerUbicacion(solicitud);
        validarUbicacionActivaUnica(ubicacionId, null);
        long id = agenciaRepository.crear(nombre, ubicacionId);
        return buscarPorId(id);
    }

    @Transactional
    public AgenciaResponse editar(long id, AgenciaRequest solicitud) {
        AgenciaResponse actual = buscarPorId(id);
        String nombre = solicitud.nombre().trim();
        validarNombreUnico(nombre, id);

        long ubicacionId = obtenerUbicacion(solicitud);
        if (actual.activo()) {
            validarUbicacionActivaUnica(ubicacionId, id);
        }

        agenciaRepository.editar(id, nombre, ubicacionId);
        return buscarPorId(id);
    }

    @Transactional
    public AgenciaResponse cambiarEstado(long id, boolean activo) {
        AgenciaResponse actual = buscarPorId(id);

        if (actual.activo() == activo) {
            return actual;
        }

        if (!activo) {
            if (agenciaRepository.contarUsuariosActivos(id) > 0
                    || agenciaRepository.contarCamionesActivos(id) > 0) {
                throw new IllegalStateException(
                        "No se puede desactivar: tiene usuarios o camiones activos"
                );
            }
        } else {
            long ubicacionId = agenciaRepository.buscarUbicacionId(id)
                    .orElseThrow(() -> new NoSuchElementException("No se encontró la sede."));
            validarUbicacionActivaUnica(ubicacionId, id);
        }

        agenciaRepository.cambiarEstado(id, activo);
        return buscarPorId(id);
    }

    private long obtenerUbicacion(AgenciaRequest solicitud) {
        String direccion = solicitud.direccion().trim();
        String ciudad = solicitud.ciudad().trim();
        return ubicacionRepository.buscarId(direccion, ciudad)
                .orElseGet(() -> ubicacionRepository.crear(
                        direccion,
                        ciudad,
                        solicitud.departamento().trim()
                ));
    }

    private void validarNombreUnico(String nombre, Long idExcluido) {
        if (agenciaRepository.existeNombre(nombre, idExcluido)) {
            throw new IllegalStateException("Ya existe una sede con ese nombre");
        }
    }

    private void validarUbicacionActivaUnica(long ubicacionId, Long idExcluido) {
        if (agenciaRepository.existeAgenciaActivaEnUbicacion(ubicacionId, idExcluido)) {
            throw new IllegalStateException("Ya existe una sede en esa dirección");
        }
    }
}