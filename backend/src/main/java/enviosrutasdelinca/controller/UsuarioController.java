package enviosrutasdelinca.controller;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import enviosrutasdelinca.dto.InicioSesionRequest;
import enviosrutasdelinca.dto.RegistroUsuarioRequest;
import enviosrutasdelinca.dto.UsuarioSesionResponse;
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
                    .body(Map.of(
                            "mensaje",
                            "La cuenta fue creada correctamente."
                    ));

        } catch (IllegalArgumentException error) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", error.getMessage()));

        } catch (DuplicateKeyException error) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "mensaje",
                            "El correo, DNI o RUC ya está registrado."
                    ));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> iniciarSesion(
            @Valid @RequestBody InicioSesionRequest solicitud,
            HttpServletRequest peticion,
            HttpServletResponse respuesta) {
        try {
            Usuario usuario = usuarioService.iniciarSesion(
                    solicitud.correo(),
                    solicitud.clave()
            );

            var autenticacion =
                    new UsernamePasswordAuthenticationToken(
                            usuario.correo(),
                            null,
                            List.of(new SimpleGrantedAuthority(
                                    "ROLE_" + usuario.rol().toUpperCase(Locale.ROOT)
                            ))
                    );

            HttpSession sesion = peticion.getSession(true);
            peticion.changeSessionId();

            SecurityContext contexto =
                    SecurityContextHolder.createEmptyContext();

            contexto.setAuthentication(autenticacion);
            SecurityContextHolder.setContext(contexto);

            new HttpSessionSecurityContextRepository()
                    .saveContext(contexto, peticion, respuesta);

            sesion.setAttribute("usuarioId", usuario.id());
            sesion.setAttribute("usuarioNombre", usuario.nombre());
            sesion.setAttribute("usuarioRol", usuario.rol());

            return ResponseEntity.ok(
                    Map.of("usuario", new UsuarioSesionResponse(
                            usuario.nombre(),
                            usuario.correo(),
                            usuario.rol().toUpperCase(Locale.ROOT),
                            usuario.agenciaId()
                    ))
            );

        } catch (IllegalStateException error) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("mensaje", error.getMessage()));

        } catch (IllegalArgumentException error) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "mensaje",
                            "Correo o contraseña incorrectos."
                    ));
        }
    }

    @GetMapping("/sesion")
    public ResponseEntity<UsuarioSesionResponse> obtenerSesion(
            Authentication autenticacion,
            HttpServletRequest peticion) {
        if (autenticacion == null
                || !autenticacion.isAuthenticated()
                || autenticacion instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.noContent().build();
        }

        Optional<Usuario> usuario = usuarioService.buscarUsuarioActivo(
                autenticacion.getName()
        );

        if (usuario.isEmpty()) {
            HttpSession sesion = peticion.getSession(false);
            if (sesion != null) {
                sesion.invalidate();
            }
            SecurityContextHolder.clearContext();
            return ResponseEntity.noContent().build();
        }

        Usuario usuarioAutenticado = usuario.get();
        return ResponseEntity.ok(new UsuarioSesionResponse(
                usuarioAutenticado.nombre(),
                usuarioAutenticado.correo(),
                usuarioAutenticado.rol().toUpperCase(Locale.ROOT),
                usuarioAutenticado.agenciaId()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> cerrarSesion(
            HttpServletRequest peticion) {
        HttpSession sesion = peticion.getSession(false);

        if (sesion != null) {
            sesion.invalidate();
        }

        SecurityContextHolder.clearContext();

        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> manejarValidacion(
            MethodArgumentNotValidException error) {
        FieldError campo =
                error.getBindingResult().getFieldError();

        String mensaje = campo == null
                ? "La solicitud no es válida."
                : campo.getDefaultMessage();

        return ResponseEntity.badRequest()
                .body(Map.of("mensaje", mensaje));
    }
}
