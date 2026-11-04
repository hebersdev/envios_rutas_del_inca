package enviosrutasdelinca.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import enviosrutasdelinca.dto.*;
import enviosrutasdelinca.service.EntregaService;
import enviosrutasdelinca.shared.model.EstadoEnvio;
import enviosrutasdelinca.shared.model.ModalidadEnvio;
import enviosrutasdelinca.shared.security.UsuarioActual;

@RestController
@RequestMapping("/api/entregas")
public class EntregaController {
    private final EntregaService entregas;
    private final UsuarioActual usuarioActual;

    public EntregaController(EntregaService entregas, UsuarioActual usuarioActual) {
        this.entregas = entregas;
        this.usuarioActual = usuarioActual;
    }

    @GetMapping("/por-entregar")
    public List<EntregaResponse> porEntregar(@RequestParam(required = false) EstadoEnvio estado,
            @RequestParam(required = false) ModalidadEnvio modalidad) {
        return entregas.porEntregar(usuarioActual.obtener(), estado, modalidad);
    }

    @GetMapping("/reparto")
    public List<EntregaResponse> reparto() {
        return entregas.reparto(usuarioActual.obtener());
    }

    @GetMapping("/motorizados")
    public List<MotorizadoRepartoResponse> motorizados() {
        return entregas.motorizados(usuarioActual.obtener());
    }

    @GetMapping("/mis-entregas")
    public List<EntregaResponse> misEntregas() {
        return entregas.misEntregas(usuarioActual.obtener());
    }

    @PostMapping("/listo-recojo")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void listoRecojo(@Valid @RequestBody EnviosEntregaRequest solicitud) {
        entregas.listoRecojo(solicitud.envioIds(), usuarioActual.obtener());
    }

    @PostMapping("/asignar-motorizado")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void asignar(@Valid @RequestBody AsignarMotorizadoRequest solicitud) {
        entregas.asignarMotorizado(solicitud.envioIds(), solicitud.motorizadoId(), usuarioActual.obtener());
    }

    @PostMapping("/salir-a-reparto")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void salir(@Valid @RequestBody EnviosEntregaRequest solicitud) {
        entregas.salirReparto(solicitud.envioIds(), usuarioActual.obtener());
    }

    @PostMapping("/{id}/entregar-en-agencia")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void entregarAgencia(@PathVariable long id, @Valid @RequestBody ConfirmarEntregaRequest solicitud) {
        entregas.entregarEnAgencia(id, solicitud.documento(), usuarioActual.obtener());
    }

    @PostMapping("/{id}/entregar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void entregarDomicilio(@PathVariable long id, @Valid @RequestBody ConfirmarEntregaRequest solicitud) {
        entregas.entregarDomicilio(id, solicitud.documento(), usuarioActual.obtener());
    }
}
