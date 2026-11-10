package enviosrutasdelinca.config;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import enviosrutasdelinca.repository.UsuarioRepository;

@Component
public class AdministradorInicial implements ApplicationRunner {

    private final UsuarioRepository usuarios;
    private final String nombre;
    private final String correo;
    private final String clave;

    public AdministradorInicial(
            UsuarioRepository usuarios,
            @Value("${ADMIN_BOOTSTRAP_NAME:}") String nombre,
            @Value("${ADMIN_BOOTSTRAP_EMAIL:}") String correo,
            @Value("${ADMIN_BOOTSTRAP_PASSWORD:}") String clave) {
        this.usuarios = usuarios;
        this.nombre = nombre;
        this.correo = correo;
        this.clave = clave;
    }

    @Override
    public void run(ApplicationArguments argumentos) {
        if (nombre.isBlank() && correo.isBlank() && clave.isBlank()) {
            return;
        }

        if (nombre.isBlank() || correo.isBlank() || clave.isBlank()) {
            throw new IllegalStateException("Completa las tres variables ADMIN_BOOTSTRAP.");
        }

        if (usuarios.contarAdministradoresActivos() > 0) {
            return;
        }

        String correoNormalizado = correo.trim().toLowerCase(Locale.ROOT);
        if (clave.length() < 12 || clave.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalStateException("La clave inicial debe tener entre 12 y 72 bytes.");
        }

        if (usuarios.existePorCorreo(correoNormalizado)) {
            throw new IllegalStateException("El correo inicial ya pertenece a otro usuario.");
        }

        usuarios.crearPersonal(
                nombre.trim(), correoNormalizado,
                new BCryptPasswordEncoder().encode(clave), "ADMINISTRADOR", null);
    }
}
