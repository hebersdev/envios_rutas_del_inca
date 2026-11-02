package enviosrutasdelinca.controller;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import enviosrutasdelinca.dto.EnvioPendienteResponse;
import enviosrutasdelinca.dto.ViajeDetalleResponse;
import enviosrutasdelinca.dto.ViajeResponse;
import enviosrutasdelinca.service.AsignacionViajeService;
import enviosrutasdelinca.service.ViajeService;
import enviosrutasdelinca.shared.model.EstadoViaje;
import enviosrutasdelinca.shared.security.UsuarioActual;

@RestController
@RequestMapping("/api/viajes")
public class ViajeController {

    private final ViajeService viajes;
    private final AsignacionViajeService asignacion;
    private final UsuarioActual usuarioActual;

    public ViajeController(ViajeService viajes, AsignacionViajeService asignacion, UsuarioActual usuarioActual) {
        this.viajes = viajes;
        this.asignacion = asignacion;
        this.usuarioActual = usuarioActual;
    }

    @GetMapping
    public List<ViajeResponse> listar(@RequestParam(required = false) EstadoViaje estado) {
        return viajes.listar(usuarioActual.obtener(), estado);
    }

    @GetMapping("/{id}")
    public ViajeDetalleResponse obtener(@PathVariable long id) {
        return viajes.obtener(id, usuarioActual.obtener());
    }

    @GetMapping("/pendientes")
    public List<EnvioPendienteResponse> pendientes() {
        return viajes.pendientes(usuarioActual.obtener());
    }

    @PostMapping("/asignar-pendientes")
    public Map<String, Integer> asignar() {
        var usuario = usuarioActual.obtener();
        return Map.of("asignados", asignacion.asignar(viajes.sedeCoordinador(usuario), usuario.id()));
    }

    @PostMapping("/{id}/despachar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void despachar(@PathVariable long id) {
        viajes.despachar(id, usuarioActual.obtener());
    }

    @PostMapping("/{id}/llegada")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void llegada(@PathVariable long id) {
        viajes.llegada(id, usuarioActual.obtener());
    }

    @PostMapping("/{id}/finalizar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void finalizar(@PathVariable long id) {
        viajes.finalizar(id, usuarioActual.obtener());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelar(@PathVariable long id) {
        viajes.cancelar(id, usuarioActual.obtener());
    }
}
