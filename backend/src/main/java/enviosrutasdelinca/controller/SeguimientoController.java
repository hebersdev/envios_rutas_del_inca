package enviosrutasdelinca.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import enviosrutasdelinca.dto.SeguimientoResponse;
import enviosrutasdelinca.service.SeguimientoService;

@RestController
@RequestMapping("/api/seguimiento")
public class SeguimientoController {

    private final SeguimientoService seguimientoService;

    public SeguimientoController(SeguimientoService seguimientoService) {
        this.seguimientoService = seguimientoService;
    }

    @GetMapping("/{codigo}")
    public SeguimientoResponse consultar(@PathVariable String codigo) {
        return seguimientoService.consultar(codigo);
    }
}
