package enviosrutasdelinca.controller;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import enviosrutasdelinca.dto.CotizacionResponse;
import enviosrutasdelinca.service.CotizacionService;
import enviosrutasdelinca.shared.model.ModalidadEnvio;

@RestController
@RequestMapping("/api/cotizaciones")
public class CotizacionController {

    private final CotizacionService cotizacionService;

    public CotizacionController(CotizacionService cotizacionService) {
        this.cotizacionService = cotizacionService;
    }

    @GetMapping
    public CotizacionResponse cotizar(
            @RequestParam long origenId,
            @RequestParam long destinoId,
            @RequestParam BigDecimal pesoKg,
            @RequestParam ModalidadEnvio modalidad) {
        return cotizacionService.cotizar(origenId, destinoId, pesoKg, modalidad);
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
