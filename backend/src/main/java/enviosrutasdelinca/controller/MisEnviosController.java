package enviosrutasdelinca.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import enviosrutasdelinca.dto.EnvioClienteResponse;
import enviosrutasdelinca.dto.PaginaResponse;
import enviosrutasdelinca.service.MisEnviosService;
import enviosrutasdelinca.shared.model.EstadoEnvio;
import enviosrutasdelinca.shared.security.UsuarioActual;

@RestController
@RequestMapping("/api/mis-envios")
public class MisEnviosController {
    private final MisEnviosService envios;
    private final UsuarioActual usuarioActual;

    public MisEnviosController(MisEnviosService envios, UsuarioActual usuarioActual) {
        this.envios = envios;
        this.usuarioActual = usuarioActual;
    }

    @GetMapping
    public ResponseEntity<PaginaResponse<EnvioClienteResponse>> listar(@RequestParam(required = false) EstadoEnvio estado,
            @RequestParam(required = false) Boolean anteriores,
            @RequestParam(defaultValue = "1") int pagina) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(envios.listar(usuarioActual.obtener(), estado, anteriores, pagina));
    }
}
