package enviosrutasdelinca.controller;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import enviosrutasdelinca.dto.CambioEstadoCamionRequest;
import enviosrutasdelinca.dto.CambioEstadoRequest;
import enviosrutasdelinca.dto.CamionRequest;
import enviosrutasdelinca.dto.CamionResponse;
import enviosrutasdelinca.service.CamionService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/camiones")
public class CamionController {

    private final CamionService camionService;

    public CamionController(CamionService camionService) {
        this.camionService = camionService;
    }

    @GetMapping
    public List<CamionResponse> listar(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Long agenciaActualId,
            @RequestParam(required = false) Boolean activo,
            Authentication autenticacion) {
        return camionService.listar(
                estado, agenciaActualId, activo, autenticacion.getName());
    }

    @PostMapping
    public ResponseEntity<CamionResponse> crear(
            @Valid @RequestBody CamionRequest solicitud,
            Authentication autenticacion) {
        exigirAdministrador(autenticacion);
        return ResponseEntity.status(HttpStatus.CREATED).body(camionService.crear(solicitud));
    }

    @PutMapping("/{id}")
    public CamionResponse editar(
            @PathVariable long id,
            @Valid @RequestBody CamionRequest solicitud,
            Authentication autenticacion) {
        exigirAdministrador(autenticacion);
        return camionService.editar(id, solicitud);
    }

    @PatchMapping("/{id}/estado")
    public CamionResponse cambiarEstado(
            @PathVariable long id,
            @Valid @RequestBody CambioEstadoCamionRequest solicitud,
            Authentication autenticacion) {
        return camionService.cambiarEstado(id, solicitud.estado(), autenticacion.getName());
    }

    @PatchMapping("/{id}/activo")
    public CamionResponse cambiarActivo(
            @PathVariable long id,
            @Valid @RequestBody CambioEstadoRequest solicitud,
            Authentication autenticacion) {
        exigirAdministrador(autenticacion);
        return camionService.cambiarActivo(id, solicitud.activo());
    }

    private void exigirAdministrador(Authentication autenticacion) {
        boolean administrador = autenticacion.getAuthorities().stream()
                .anyMatch(autoridad -> "ROLE_ADMINISTRADOR".equals(autoridad.getAuthority()));
        if (!administrador) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "No tienes permiso para esta acción.");
        }
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> manejarNoEncontrado(
            NoSuchElementException error) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("mensaje", error.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> manejarConflicto(
            IllegalStateException error) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("mensaje", error.getMessage()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> manejarEstado(
            ResponseStatusException error) {
        return ResponseEntity.status(error.getStatusCode())
                .body(Map.of("mensaje", error.getReason() == null
                        ? "La solicitud no es válida."
                        : error.getReason()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> manejarSolicitudInvalida(
            IllegalArgumentException error) {
        return ResponseEntity.badRequest().body(Map.of("mensaje", error.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> manejarConflictoDatos() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("mensaje", "No se pudo guardar el camión por un conflicto de datos."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> manejarValidacion(
            MethodArgumentNotValidException error) {
        FieldError campo = error.getBindingResult().getFieldError();
        return ResponseEntity.badRequest().body(Map.of(
                "mensaje",
                campo == null ? "La solicitud no es válida." : campo.getDefaultMessage()
        ));
    }
}
