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
public class EtiquetaService {

    public static final float ANCHO_MM = 100;
    public static final float ALTO_MM = 90;
    private static final float MARGEN = 10;
    private static final float INICIO_RECEPCION = 129;
    private final LogoEmpresa logo;

    public EtiquetaService(LogoEmpresa logo) {
        this.logo = logo;
    }

    public byte[] generarPng(EnvioResponse envio) {
        try {
            return disenar(envio).png(ALTO_MM);
        } catch (IOException error) {
            throw fallo(error);
        }
    }

    public byte[] generarPdf(EnvioResponse envio) {
        try {
            return disenar(envio).pdf(ALTO_MM);
        } catch (IOException | DocumentException error) {
            throw fallo(error);
        }
    }

    public DocumentoImprimible disenar(EnvioResponse envio) {
        for (float tamanio = 9; tamanio >= 6; tamanio -= 0.5f) {
            DocumentoImprimible documento = construir(envio, tamanio);
            if (documento != null) return documento;
        }
        throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                "Los datos del destinatario exceden el espacio de la etiqueta de 100 × 90 mm.");
    }

    private DocumentoImprimible construir(EnvioResponse envio, float tamanio) {
        var d = new DocumentoImprimible(ANCHO_MM, 1.2f);
        float ancho = d.ancho() - MARGEN * 2;
        d.marco(4, 4, d.ancho() - 8, DocumentoImprimible.puntos(ALTO_MM) - 8, 7);
        d.logo(logo.imagen(), MARGEN, 11, 62, 31);
        float xCodigo = MARGEN + 72;
        d.marco(xCodigo, 8, ancho - 72, 36, 4);
        d.centrado("CÓDIGO DE ENVÍO", xCodigo + 3, 11, ancho - 78, 6, true);
        d.centrado(envio.codigoEnvio(), xCodigo + 3, 20, ancho - 78, 18, true);
        d.banda("01  DESTINATARIO", MARGEN, 49, ancho, 12);
        float x = MARGEN + 5;
        float espacio = ancho - 10;
        float y = d.texto(envio.destinatarioNombre(), x, 65, espacio, tamanio + 1, true);
        float secundario = Math.max(6, tamanio - 1);
        float documento = d.texto("DNI / RUC: " + FormatoEnvio.texto(envio.destinatarioDocumento()),
                x, y + 3, espacio / 2, secundario, false);
        float telefono = d.texto("TEL.: " + FormatoEnvio.texto(envio.destinatarioTelefono()),
                x + espacio / 2, y + 3, espacio / 2, secundario, false);
        y = Math.max(documento, telefono);
        y = d.texto(FormatoEnvio.modalidad(envio) + ": " + FormatoEnvio.direccion(envio)
                + " · " + envio.destino().ciudad(), x, y + 3, espacio, tamanio, false);
        if (y + 4 > INICIO_RECEPCION) return null;
        agregarRecepcion(d, ancho);
        return d;
    }

    private void agregarRecepcion(DocumentoImprimible d, float ancho) {
        d.banda("02  RECEPCIÓN · COMPLETAR A MANO", MARGEN, INICIO_RECEPCION, ancho, 12);
        float x = MARGEN + 5;
        float espacio = ancho - 10;
        renglon(d, "Nombres:", x, 146, espacio);
        renglon(d, "Domicilio:", x, 162, espacio);
        renglon(d, "DNI:", x, 178, espacio);
        float mitad = (espacio - 10) / 2;
        renglon(d, "Fecha:", x, 194, mitad);
        renglon(d, "Hora:", x + mitad + 10, 194, mitad);
        String[] relaciones = {"TITULAR", "FAMILIAR", "EMPLEADO", "VIGILANTE"};
        float xCasillas = x + espacio * 0.51f;
        float columna = espacio * 0.49f / 2;
        d.texto("RECIBE COMO:", xCasillas, 208, espacio * 0.49f, 5.7f, true);
        for (int i = 0; i < relaciones.length; i++) {
            float inicio = xCasillas + i % 2 * columna;
            float y = 219 + i / 2 * 16;
            d.casilla(inicio, y, 8);
            d.texto(relaciones[i], inicio + 11, y, columna - 11, 5.7f, false);
        }
        float anchoFirma = espacio * 0.46f;
        d.linea(x, 235, anchoFirma, false);
        d.centrado("FIRMA Y/O SELLO", x, 239, anchoFirma, 6, true);
    }

    private void renglon(DocumentoImprimible d, String etiqueta, float x, float y, float ancho) {
        d.texto(etiqueta, x, y, 39, 7, false);
        d.linea(x + 40, y + 10, ancho - 40, false);
    }

    private ResponseStatusException fallo(Exception error) {
        return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "No se pudo generar la etiqueta. Inténtalo nuevamente.", error);
    }
}
