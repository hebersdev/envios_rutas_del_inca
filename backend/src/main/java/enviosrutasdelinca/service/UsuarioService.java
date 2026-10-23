package enviosrutasdelinca.service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import enviosrutasdelinca.dto.RegistroUsuarioRequest;
import enviosrutasdelinca.model.Usuario;
import enviosrutasdelinca.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public Usuario iniciarSesion(String correo, String clave) {
        if (correo == null || correo.isBlank()
                || clave == null || clave.isEmpty()) {
            throw new IllegalArgumentException(
                    "Correo o contraseña incorrectos"
            );
        }

        String correoNormalizado =
                correo.trim().toLowerCase(Locale.ROOT);

        Usuario usuario = usuarioRepository
                .buscarPorCorreo(correoNormalizado)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Correo o contraseña incorrectos"
                ));

        if (!passwordEncoder.matches(clave, usuario.clave())) {
            throw new IllegalArgumentException(
                    "Correo o contraseña incorrectos"
            );
        }

        if (!Boolean.TRUE.equals(usuario.activo())) {
            throw new IllegalStateException(
                    "La cuenta está desactivada"
            );
        }

        return usuario;
    }

    public Optional<Usuario> buscarUsuarioActivo(String correo) {
        if (correo == null || correo.isBlank()) {
            return Optional.empty();
        }

        return usuarioRepository.buscarPorCorreo(
                        correo.trim().toLowerCase(Locale.ROOT))
                .filter(usuario -> Boolean.TRUE.equals(usuario.activo()));
    }

    @Transactional
    public void registrarUsuario(RegistroUsuarioRequest datos) {
        if (datos == null) {
            throw new IllegalArgumentException(
                    "Los datos del usuario son obligatorios."
            );
        }

        if (datos.nombre() == null || datos.nombre().isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre es obligatorio."
            );
        }

        if (datos.correo() == null || datos.correo().isBlank()) {
            throw new IllegalArgumentException(
                    "El correo es obligatorio."
            );
        }

        if (datos.clave() == null || datos.clave().isEmpty()) {
            throw new IllegalArgumentException(
                    "La contraseña es obligatoria."
            );
        }

        String correo =
                datos.correo().trim().toLowerCase(Locale.ROOT);

        String dni = normalizar(datos.dni());
        String ruc = normalizar(datos.ruc());
        String tipoPersona = normalizar(datos.tipoPersona());

        if (datos.clave().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException(
                    "La contraseña supera el máximo permitido de 72 bytes."
            );
        }

        if (usuarioRepository.existePorCorreo(correo)) {
            throw new IllegalArgumentException(
                    "Ya existe una cuenta con ese correo."
            );
        }

        if ("PERSONA_NATURAL".equals(tipoPersona)) {
            if (dni == null || !dni.matches("\\d{8}") || ruc != null) {
                throw new IllegalArgumentException(
                        "Una persona natural debe registrar un DNI válido de 8 dígitos y no registrar RUC."
                );
            }

            if (usuarioRepository.existePorDni(dni)) {
                throw new IllegalArgumentException(
                        "Ya existe una cuenta con ese DNI."
                );
            }

        } else if ("EMPRESA".equals(tipoPersona)) {
            if (ruc == null || !ruc.matches("\\d{11}") || dni != null) {
                throw new IllegalArgumentException(
                        "Una empresa debe registrar un RUC válido de 11 dígitos y no registrar DNI."
                );
            }

            if (usuarioRepository.existePorRuc(ruc)) {
                throw new IllegalArgumentException(
                        "Ya existe una cuenta con ese RUC."
                );
            }

        } else {
            throw new IllegalArgumentException(
                    "El tipo de cliente no es válido."
            );
        }

        Usuario usuario = new Usuario(
                null,
                datos.nombre().trim(),
                correo,
                passwordEncoder.encode(datos.clave()),
                normalizar(datos.telefono()),
                dni,
                ruc,
                "CLIENTE",
                null,
                true,
                null
        );

        usuarioRepository.guardar(usuario);
    }

    private String normalizar(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }
}
