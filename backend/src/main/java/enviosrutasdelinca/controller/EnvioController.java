package enviosrutasdelinca.controller;


import enviosrutasdelinca.dto.CambioEstadoEnvioRequest;
import enviosrutasdelinca.dto.EnvioRequest;
import enviosrutasdelinca.dto.EnvioResponse;
import enviosrutasdelinca.dto.EnvioResumenResponse;
import enviosrutasdelinca.dto.PaginaResponse;
import enviosrutasdelinca.service.BoletaService;
import enviosrutasdelinca.service.EnvioService;
import enviosrutasdelinca.service.EtiquetaService;
import enviosrutasdelinca.shared.model.EstadoEnvio;
import enviosrutasdelinca.shared.security.UsuarioActual;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/envios")
public class EnvioController {

    private final EnvioService envioService;
    private final BoletaService boletaService;
    private final UsuarioActual usuarioActual;
    private final EtiquetaService etiquetaService;

    public EnvioController(
            EnvioService envioService,
            BoletaService boletaService,
            UsuarioActual usuarioActual,
            EtiquetaService etiquetaService) {
        this.envioService = envioService;
        this.boletaService = boletaService;
        this.usuarioActual = usuarioActual;
        this.etiquetaService = etiquetaService;
    }

    @PostMapping
    public ResponseEntity<EnvioResponse> registrar(@Valid @RequestBody EnvioRequest solicitud) {
        EnvioResponse envio = envioService.registrar(solicitud, usuarioActual.obtener());
        return ResponseEntity.status(HttpStatus.CREATED).body(envio);
    }

    @GetMapping
    public PaginaResponse<EnvioResumenResponse> listar(
            @RequestParam(required = false) EstadoEnvio estado,
            @RequestParam(required = false) String codigo,
            @RequestParam(defaultValue = "1") int pagina) {
        return envioService.listar(usuarioActual.obtener(), estado, codigo, pagina);
    }

    @GetMapping("/{id}")
    public EnvioResponse obtener(@PathVariable long id) {
        return envioService.obtener(id, usuarioActual.obtener());
    }

    @PatchMapping("/{id}/estado")
    public EnvioResponse cambiarEstado(
            @PathVariable long id,
            @Valid @RequestBody CambioEstadoEnvioRequest solicitud) {
        return envioService.cambiarEstado(id, solicitud.estado(), usuarioActual.obtener());
    }

    @GetMapping("/{id}/boleta")
    public ResponseEntity<byte[]> boleta(@PathVariable long id) {
        EnvioResponse envio = envioService.obtener(id, usuarioActual.obtener());
        return documento(boletaService.generar(envio), MediaType.APPLICATION_PDF,
                "boleta-" + envio.codigoEnvio() + ".pdf");
    }

    @GetMapping(value = "/{id}/etiqueta", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> etiqueta(@PathVariable long id) {
        EnvioResponse envio = envioService.obtener(id, usuarioActual.obtener());
        return documento(etiquetaService.generarPng(envio), MediaType.IMAGE_PNG,
                "etiqueta-" + envio.codigoEnvio() + ".png");
    }

    @GetMapping(value = "/{id}/etiqueta.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> etiquetaPdf(@PathVariable long id) {
        EnvioResponse envio = envioService.obtener(id, usuarioActual.obtener());
        return documento(etiquetaService.generarPdf(envio), MediaType.APPLICATION_PDF,
                "etiqueta-" + envio.codigoEnvio() + ".pdf");
    }

    private ResponseEntity<byte[]> documento(byte[] contenido, MediaType tipo, String nombre) {
        return ResponseEntity.ok().contentType(tipo)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(nombre).build().toString())
                .body(contenido);
    }
}
