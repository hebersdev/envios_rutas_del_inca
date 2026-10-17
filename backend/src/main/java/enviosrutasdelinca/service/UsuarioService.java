package enviosrutasdelinca.service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import enviosrutasdelinca.controller.RegistroUsuarioRequest;
import enviosrutasdelinca.model.Usuario;
import enviosrutasdelinca.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario iniciarSesion(String correo, String clave) {

        Usuario usuario = usuarioRepository
                                .buscarPorCorreo(correo.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() ->
                        new RuntimeException(
                                "Correo o contraseña incorrectos"
                        )
                );


        if (!passwordEncoder.matches(
                clave, usuario.getClave())) {

            throw new RuntimeException(
                    "Correo o contraseña incorrectos"
            );
        }

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new RuntimeException(
                    "La cuenta está desactivada"
            );
        }
        return usuario;
    }

        public void registrarUsuario(RegistroUsuarioRequest datos) {
                String correo = datos.correo().trim().toLowerCase(Locale.ROOT);
                String dni = normalizar(datos.dni());
                String ruc = normalizar(datos.ruc());

                if (datos.clave().getBytes(StandardCharsets.UTF_8).length > 72) {
                        throw new IllegalArgumentException("La contraseña supera el máximo permitido.");
                }
                if (usuarioRepository.existePorCorreo(correo)) {
                        throw new IllegalArgumentException("Ya existe una cuenta con ese correo.");
                }
                if ("PERSONA_NATURAL".equals(datos.tipoPersona())) {
                        if (dni == null || !dni.matches("\\d{8}") || ruc != null) {
                                throw new IllegalArgumentException("Una persona natural debe registrar un DNI válido de 8 dígitos.");
                        }
                        if (usuarioRepository.existePorDni(dni)) {
                                throw new IllegalArgumentException("Ya existe una cuenta con ese DNI.");
                        }
                } else if ("EMPRESA".equals(datos.tipoPersona())) {
                        if (ruc == null || !ruc.matches("\\d{11}") || dni != null) {
                                throw new IllegalArgumentException("Una empresa debe registrar un RUC válido de 11 dígitos.");
                        }
                        if (usuarioRepository.existePorRuc(ruc)) {
                                throw new IllegalArgumentException("Ya existe una cuenta con ese RUC.");
                        }
                } else {
                        throw new IllegalArgumentException("El tipo de cliente no es válido.");
                }

                Usuario usuario = new Usuario();
                usuario.setNombre(datos.nombre().trim());
                usuario.setCorreo(correo);
                usuario.setClave(passwordEncoder.encode(datos.clave()));
                usuario.setTelefono(normalizar(datos.telefono()));
                usuario.setDni(dni);
                usuario.setRuc(ruc);
                usuario.setRol("cliente");
                usuario.setAgenciaId(null);
                usuario.setActivo(true);
                usuarioRepository.guardar(usuario);
        }

        private String normalizar(String valor) {
                if (valor == null || valor.isBlank()) {
                        return null;
                }
                return valor.trim();
        }
}
