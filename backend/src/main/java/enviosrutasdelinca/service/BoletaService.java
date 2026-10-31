package enviosrutasdelinca.service;

import java.io.IOException;
import com.lowagie.text.DocumentException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import enviosrutasdelinca.dto.EnvioResponse;
import enviosrutasdelinca.shared.documentos.DocumentoImprimible;
import enviosrutasdelinca.shared.documentos.FormatoEnvio;
import enviosrutasdelinca.shared.documentos.LogoEmpresa;

@Service
public class BoletaService {

    public static final float ANCHO_MM = 80;
    private static final float MARGEN = 12;
    private final LogoEmpresa logo;

    public BoletaService(LogoEmpresa logo) {
        this.logo = logo;
    }

    public byte[] generar(EnvioResponse envio) {
        DocumentoImprimible documento = disenar(envio);
        float altoMm = (documento.finContenido() + MARGEN) * 25.4f / 72;
        try {
            return documento.pdf(altoMm);
        } catch (IOException | DocumentException error) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo generar la boleta. Inténtalo nuevamente.", error);
        }
    }

    public DocumentoImprimible disenar(EnvioResponse envio) {
        var d = new DocumentoImprimible(ANCHO_MM);
        float ancho = d.ancho() - MARGEN * 2;
        d.logo(logo.imagen(), MARGEN, 10, ancho, 58);
        float y = d.centrado("RUTAS DEL INCA S.A.C.", MARGEN, 73, ancho, 9, true);
        y = d.centrado("BOLETA DE ENVÍO", MARGEN, y + 3, ancho, 10, true);
        y = d.centrado(envio.origen().nombre() + " · " + envio.origen().ciudad(),
                MARGEN, y + 5, ancho, 7.5f, false);
        y = d.centrado(envio.origen().direccion(), MARGEN, y + 2, ancho, 7, false);
        d.linea(MARGEN, y + 8, ancho, true);
        y = d.centrado("N.º DE SEGUIMIENTO", MARGEN, y + 17, ancho, 7, true);
        y = d.centrado(envio.codigoEnvio(), MARGEN, y + 3, ancho, 21, true);
        y = d.centrado(envio.fechaRegistro().format(FormatoEnvio.FECHA), MARGEN, y + 3, ancho, 8, false);
        y = seccion(d, "RUTA Y ENTREGA", y + 10, ancho);
        y = campo(d, "ORIGEN", envio.origen().nombre() + " · " + envio.origen().ciudad(), y, ancho);
        y = campo(d, "DESTINO", envio.destino().nombre() + " · " + envio.destino().ciudad(), y, ancho);
        y = campo(d, "MODALIDAD", FormatoEnvio.modalidad(envio), y, ancho);
        y = campo(d, "DIRECCIÓN DE ENTREGA", FormatoEnvio.direccion(envio), y, ancho);
        y = seccion(d, "REMITENTE", y + 4, ancho);
        y = d.texto(envio.remitente().nombre(), MARGEN, y, ancho, 9, true);
        y = d.texto("DNI / RUC: " + FormatoEnvio.texto(envio.remitente().documento()),
                MARGEN, y + 3, ancho, 8, false);
        y = d.texto("Teléfono: " + FormatoEnvio.texto(envio.remitente().telefono()),
                MARGEN, y + 2, ancho, 8, false);
        y = seccion(d, "DESTINATARIO", y + 10, ancho);
        y = d.texto(envio.destinatarioNombre(), MARGEN, y, ancho, 9, true);
        y = d.texto("DNI / RUC: " + FormatoEnvio.texto(envio.destinatarioDocumento()),
                MARGEN, y + 3, ancho, 8, false);
        y = d.texto("Teléfono: " + FormatoEnvio.texto(envio.destinatarioTelefono()),
                MARGEN, y + 2, ancho, 8, false);
        d.linea(MARGEN, y + 4, ancho, false);
        y = d.texto("TOTAL DEL SERVICIO", MARGEN, y + 12, ancho, 8, true);
        y = d.centrado(FormatoEnvio.moneda(envio.costoEnvio()), MARGEN, y + 4, ancho, 20, true);
        d.linea(MARGEN, y + 9, ancho, true);
        y = d.centrado("Conserva tu código para consultar el seguimiento en nuestra página web.",
                MARGEN + 5, y + 19, ancho - 10, 7.5f, false);
        d.centrado("Gracias por confiar en Rutas del Inca", MARGEN, y + 10, ancho, 8, true);
        return d;
    }

    private float seccion(DocumentoImprimible d, String titulo, float y, float ancho) {
        d.linea(MARGEN, y, ancho, true);
        return d.texto(titulo, MARGEN, y + 8, ancho, 8, true) + 6;
    }

    private float campo(DocumentoImprimible d, String etiqueta, String valor, float y, float ancho) {
        y = d.texto(etiqueta, MARGEN, y, ancho, 6.5f, true);
        return d.texto(valor, MARGEN, y + 2, ancho, 8.5f, false) + 7;
    }
}
