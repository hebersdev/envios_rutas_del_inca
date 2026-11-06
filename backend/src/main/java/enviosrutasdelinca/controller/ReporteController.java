package enviosrutasdelinca.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import enviosrutasdelinca.service.ReporteExcelService;
import enviosrutasdelinca.shared.security.UsuarioActual;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private static final MediaType TIPO_EXCEL = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final ReporteExcelService reportes;
    private final UsuarioActual usuarioActual;

    public ReporteController(ReporteExcelService reportes, UsuarioActual usuarioActual) {
        this.reportes = reportes;
        this.usuarioActual = usuarioActual;
    }

    @GetMapping(value = "/excel", produces =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> descargar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        byte[] archivo = reportes.generar(usuarioActual.obtener(), desde, hasta);
        String nombre = "reportes-" + desde + "-" + hasta + ".xlsx";
        return ResponseEntity.ok()
                .contentType(TIPO_EXCEL)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(nombre).build().toString())
                .body(archivo);
    }
}
