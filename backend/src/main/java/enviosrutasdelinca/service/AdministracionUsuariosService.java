package enviosrutasdelinca.service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import enviosrutasdelinca.dto.ActualizarRolUsuarioRequest;
import enviosrutasdelinca.dto.AgenciaResponse;
import enviosrutasdelinca.dto.UsuarioAdministracionRequest;
import enviosrutasdelinca.dto.UsuarioAdministracionResponse;
import enviosrutasdelinca.repository.AgenciaRepository;
import enviosrutasdelinca.repository.UsuarioRepository;

@Service
public class AdministracionUsuariosService {

    private final UsuarioRepository usuarioRepository;
    private final AgenciaRepository agenciaRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AdministracionUsuariosService(
            UsuarioRepository usuarioRepository,
            AgenciaRepository agenciaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.agenciaRepository = agenciaRepository;
    }

    public List<UsuarioAdministracionResponse> listar() {
        return usuarioRepository.listarPersonalActivo();
    }

    @Transactional
    public UsuarioAdministracionResponse crear(UsuarioAdministracionRequest solicitud) {
        String correo = solicitud.correo().trim().toLowerCase(Locale.ROOT);
        if (usuarioRepository.existePorCorreo(correo)) {
            throw new IllegalStateException("Ya existe una cuenta con ese correo.");
        }
        if (solicitud.clave().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("La contraseña no puede superar 72 bytes.");
        }

        Long agenciaId = validarAgencia(solicitud.rol(), solicitud.agenciaId());
        try {
            long id = usuarioRepository.crearPersonal(
                    solicitud.nombre().trim(),
                    correo,
                    passwordEncoder.encode(solicitud.clave()),
                    solicitud.rol(),
                    agenciaId
            );
            return usuarioRepository.buscarPersonalActivo(id)
                    .orElseThrow(() -> new NoSuchElementException("No se encontró el usuario creado."));
        } catch (DuplicateKeyException error) {
            throw new IllegalStateException("Ya existe una cuenta con ese correo.");
        }
    }

    @Transactional
    public UsuarioAdministracionResponse actualizarRol(
            long id,
            ActualizarRolUsuarioRequest solicitud,
            String correoAdministrador) {
        UsuarioAdministracionResponse actual = buscarActivo(id);
        String nuevoRol = solicitud.rol();
        if (actual.correo().equalsIgnoreCase(correoAdministrador)
                && !actual.rol().equals(nuevoRol)) {
            throw new IllegalStateException("No puedes cambiar tu propio rol.");
        }
        if (actual.rol().equals("ADMINISTRADOR")
                && !nuevoRol.equals("ADMINISTRADOR")
                && usuarioRepository.contarAdministradoresActivos() <= 1) {
            throw new IllegalStateException("Debe permanecer al menos un administrador activo.");
        }

        Long agenciaId = validarAgencia(nuevoRol, solicitud.agenciaId());
        usuarioRepository.actualizarRol(id, nuevoRol, agenciaId);
        return buscarActivo(id);
    }

    @Transactional
    public void desactivar(long id, String correoAdministrador) {
        UsuarioAdministracionResponse actual = buscarActivo(id);
        if (actual.correo().equalsIgnoreCase(correoAdministrador)) {
            throw new IllegalStateException("No puedes desactivar tu propia cuenta.");
        }
        if (actual.rol().equals("ADMINISTRADOR")
                && usuarioRepository.contarAdministradoresActivos() <= 1) {
            throw new IllegalStateException("Debe permanecer al menos un administrador activo.");
        }
        usuarioRepository.desactivar(id);
    }

    private UsuarioAdministracionResponse buscarActivo(long id) {
        return usuarioRepository.buscarPersonalActivo(id)
                .orElseThrow(() -> new NoSuchElementException("No se encontró el usuario activo."));
    }

    private Long validarAgencia(String rol, Long agenciaId) {
        if (rol.equals("ADMINISTRADOR")) {
            if (agenciaId != null) {
                throw new IllegalArgumentException("El administrador no debe tener una sede asignada.");
            }
            return null;
        }

        if (agenciaId == null) {
            throw new IllegalArgumentException("Selecciona una sede para este rol.");
        }

        AgenciaResponse agencia = agenciaRepository.buscarPorId(agenciaId)
                .filter(AgenciaResponse::activo)
                .orElseThrow(() -> new IllegalArgumentException("La sede seleccionada no existe o está inactiva."));
        return agencia.id();
    }
}