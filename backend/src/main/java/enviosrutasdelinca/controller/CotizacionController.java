package enviosrutasdelinca.controller;

import java.math.BigDecimal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
            @RequestParam(required = false) BigDecimal altoCm,
            @RequestParam(required = false) BigDecimal anchoCm,
            @RequestParam(required = false) BigDecimal largoCm,
            @RequestParam ModalidadEnvio modalidad) {
        return cotizacionService.cotizar(
                origenId, destinoId, pesoKg, altoCm, anchoCm, largoCm, modalidad);
    }
}