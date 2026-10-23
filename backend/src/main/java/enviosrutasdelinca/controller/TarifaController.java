package enviosrutasdelinca.controller;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import enviosrutasdelinca.dto.CambioEstadoRequest;
import enviosrutasdelinca.dto.TarifaRequest;
import enviosrutasdelinca.dto.TarifaResponse;
import enviosrutasdelinca.service.TarifaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tarifas")
public class TarifaController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TarifaController.class);

    private final TarifaService tarifaService;

    public TarifaController(TarifaService tarifaService) {
        this.tarifaService = tarifaService;
    }

    @GetMapping
    public List<TarifaResponse> listar(
            @RequestParam(required = false) Boolean activo) {
        return tarifaService.listar(activo);
    }

    @PostMapping
    public ResponseEntity<TarifaResponse> crear(
            @Valid @RequestBody TarifaRequest solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tarifaService.crear(solicitud));
    }

    @PutMapping("/{id}")
    public TarifaResponse editar(
            @PathVariable long id,
            @Valid @RequestBody TarifaRequest solicitud) {
        return tarifaService.editar(id, solicitud);
    }

    @PatchMapping("/{id}/estado")
    public TarifaResponse cambiarEstado(
            @PathVariable long id,
            @Valid @RequestBody CambioEstadoRequest solicitud) {
        return tarifaService.cambiarEstado(id, solicitud.activo());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> manejarNoEncontrada(
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
    public ResponseEntity<Map<String, String>> manejarConflictoDatos(
            DataIntegrityViolationException error) {
        LOGGER.error("No se pudo guardar la tarifa debido a una restricción de base de datos.", error);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("mensaje", "No se pudo guardar la tarifa por un conflicto de datos."));
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
