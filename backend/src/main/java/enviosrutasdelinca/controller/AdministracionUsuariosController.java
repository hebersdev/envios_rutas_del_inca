package enviosrutasdelinca.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import enviosrutasdelinca.dto.ActualizarRolUsuarioRequest;
import enviosrutasdelinca.dto.UsuarioAdministracionRequest;
import enviosrutasdelinca.dto.UsuarioAdministracionResponse;
import enviosrutasdelinca.service.AdministracionUsuariosService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
public class AdministracionUsuariosController {

    private final AdministracionUsuariosService administracionUsuariosService;

    public AdministracionUsuariosController(AdministracionUsuariosService administracionUsuariosService) {
        this.administracionUsuariosService = administracionUsuariosService;
    }

    @GetMapping
    public List<UsuarioAdministracionResponse> listar() {
        return administracionUsuariosService.listar();
    }

    @PostMapping
    public ResponseEntity<UsuarioAdministracionResponse> crear(
            @Valid @RequestBody UsuarioAdministracionRequest solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(administracionUsuariosService.crear(solicitud));
    }

    @PatchMapping("/{id}/rol")
    public UsuarioAdministracionResponse actualizarRol(
            @PathVariable long id,
            @Valid @RequestBody ActualizarRolUsuarioRequest solicitud,
            Principal principal) {
        return administracionUsuariosService.actualizarRol(id, solicitud, principal.getName());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable long id, Principal principal) {
        administracionUsuariosService.desactivar(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> manejarNoEncontrado(NoSuchElementException error) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", error.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> manejarSolicitudInvalida(IllegalArgumentException error) {
        return ResponseEntity.badRequest().body(Map.of("mensaje", error.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> manejarConflicto(IllegalStateException error) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensaje", error.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> manejarConflictoDeDatos(DataIntegrityViolationException error) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("mensaje", "No se pudo completar la operación por una restricción de datos."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> manejarValidacion(MethodArgumentNotValidException error) {
        FieldError campo = error.getBindingResult().getFieldError();
        String mensaje = campo == null ? "La solicitud no es válida." : campo.getDefaultMessage();
        return ResponseEntity.badRequest().body(Map.of("mensaje", mensaje));
    }
}