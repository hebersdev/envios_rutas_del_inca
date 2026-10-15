package enviosrutasdelinca.service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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
                .buscarPorCorreo(correo)
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
}
