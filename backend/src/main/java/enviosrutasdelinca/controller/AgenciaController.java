package enviosrutasdelinca.controller;

import enviosrutasdelinca.dto.AgenciaRequest;
import enviosrutasdelinca.dto.AgenciaResponse;
import enviosrutasdelinca.dto.AgenciaResumenResponse;
import enviosrutasdelinca.dto.CambioEstadoRequest;
import enviosrutasdelinca.service.AgenciaService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/agencias")
public class AgenciaController {

    private final AgenciaService agenciaService;

    public AgenciaController(AgenciaService agenciaService) {
        this.agenciaService = agenciaService;
    }

    @GetMapping("/activas")
    public List<AgenciaResumenResponse> listarActivas() {
        return agenciaService.listarActivas();
    }

    @GetMapping
    public List<AgenciaResponse> listar(@RequestParam(required = false) Boolean activo) {
        return agenciaService.listar(activo);
    }

    @GetMapping("/{id}")
    public AgenciaResponse buscarPorId(@PathVariable long id) {
        return agenciaService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<AgenciaResponse> crear(@Valid @RequestBody AgenciaRequest solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(agenciaService.crear(solicitud));
    }

    @PutMapping("/{id}")
    public AgenciaResponse editar(
            @PathVariable long id,
            @Valid @RequestBody AgenciaRequest solicitud) {
        return agenciaService.editar(id, solicitud);
    }

    @PatchMapping("/{id}/estado")
    public AgenciaResponse cambiarEstado(
            @PathVariable long id,
            @Valid @RequestBody CambioEstadoRequest solicitud) {
        return agenciaService.cambiarEstado(id, solicitud.activo());
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

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> manejarConflictoDeDatos(
            DataIntegrityViolationException error) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("mensaje", "Ya existe una sede con ese nombre o dirección"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> manejarValidacion(
            MethodArgumentNotValidException error) {
        FieldError campo = error.getBindingResult().getFieldError();
        String mensaje = campo == null
                ? "La solicitud no es válida."
                : campo.getDefaultMessage();
        return ResponseEntity.badRequest().body(Map.of("mensaje", mensaje));
    }
}
