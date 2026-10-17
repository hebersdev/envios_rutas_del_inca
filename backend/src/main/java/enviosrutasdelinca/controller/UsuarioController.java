
package enviosrutasdelinca.controller;

import java.util.Map;
import java.util.List;
import java.util.Locale;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import enviosrutasdelinca.model.Usuario;
import enviosrutasdelinca.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/registro")
    public ResponseEntity<Map<String, String>> registrar(
            @Valid @RequestBody RegistroUsuarioRequest solicitud) {
        try {
            usuarioService.registrarUsuario(solicitud);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("mensaje", "La cuenta fue creada correctamente."));
        } catch (IllegalArgumentException error) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", error.getMessage()));
        } catch (DuplicateKeyException error) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensaje", "El correo, DNI o RUC ya está registrado."));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> iniciarSesion(
            @Valid @RequestBody InicioSesionRequest solicitud,
            HttpServletRequest peticion,
            HttpServletResponse respuesta) {
        try {
            Usuario usuario = usuarioService.iniciarSesion(solicitud.correo(), solicitud.clave());
            HttpSession sesion = peticion.getSession(true);
            peticion.changeSessionId();

            var autenticacion = new UsernamePasswordAuthenticationToken(
                usuario.getCorreo(),
                null,
                List.of(new SimpleGrantedAuthority(
                    "ROLE_" + usuario.getRol().toUpperCase(Locale.ROOT)))
            );
            SecurityContext contexto = SecurityContextHolder.createEmptyContext();
            contexto.setAuthentication(autenticacion);
            SecurityContextHolder.setContext(contexto);
            new HttpSessionSecurityContextRepository().saveContext(contexto, peticion, respuesta);

            sesion.setAttribute("usuarioId", usuario.getId());
            sesion.setAttribute("usuarioNombre", usuario.getNombre());
            sesion.setAttribute("usuarioRol", usuario.getRol());

            Map<String, String> datosUsuario = Map.of(
                    "nombre", usuario.getNombre(),
                    "correo", usuario.getCorreo(),
                    "rol", usuario.getRol()
            );
            return ResponseEntity.ok(Map.of("usuario", datosUsuario));
        } catch (RuntimeException error) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensaje", "Correo o contraseña incorrectos."));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> cerrarSesion(HttpServletRequest peticion) {
        HttpSession sesion = peticion.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> manejarValidacion(MethodArgumentNotValidException error) {
        FieldError campo = error.getBindingResult().getFieldError();
        String mensaje = campo == null ? "La solicitud no es válida." : campo.getDefaultMessage();
        return ResponseEntity.badRequest().body(Map.of("mensaje", mensaje));
    }
}
