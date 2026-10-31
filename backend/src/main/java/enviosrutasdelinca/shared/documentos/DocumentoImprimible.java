package enviosrutasdelinca.shared.documentos;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.imageio.metadata.IIOMetadataNode;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;

public final class DocumentoImprimible {

    public static final int DPI = 300;
    private static final FontRenderContext METRICAS = new FontRenderContext(null, false, false);
    private final float ancho;
    private final float interlineado;
    private final List<Consumer<Graphics2D>> trazos = new ArrayList<>();
    private float finContenido;

    public DocumentoImprimible(float anchoMm) {
        this(anchoMm, 1.35f);
    }

    public DocumentoImprimible(float anchoMm, float interlineado) {
        ancho = puntos(anchoMm);
        this.interlineado = interlineado;
    }

    public static float puntos(float milimetros) {
        return milimetros * 72 / 25.4f;
    }

    public float ancho() {
        return ancho;
    }

    public float finContenido() {
        return finContenido;
    }

    public float texto(String contenido, float x, float y, float espacio, float tamanio, boolean negrita) {
        return texto(contenido, x, y, espacio, tamanio, negrita, false);
    }

    public float centrado(String contenido, float x, float y, float espacio, float tamanio, boolean negrita) {
        return texto(contenido, x, y, espacio, tamanio, negrita, true);
    }

    private float texto(String contenido, float x, float y, float espacio, float tamanio,
            boolean negrita, boolean centrar) {
        Font fuente = new Font(Font.SANS_SERIF, negrita ? Font.BOLD : Font.PLAIN, 1).deriveFont(tamanio);
        float altoLinea = tamanio * interlineado;
        float ascenso = fuente.getLineMetrics("Ág", METRICAS).getAscent();
        for (String linea : envolver(contenido, fuente, espacio)) {
            float inicio = centrar ? x + (espacio - medir(linea, fuente)) / 2 : x;
            float base = y + ascenso;
            trazos.add(g -> {
                g.setColor(Color.BLACK);
                g.setFont(fuente);
                g.drawString(linea, inicio, base);
            });
            y += altoLinea;
        }
        finContenido = Math.max(finContenido, y);
        return y;
    }

    public void linea(float x, float y, float longitud, boolean discontinua) {
        trazos.add(g -> {
            g.setColor(Color.BLACK);
            g.setStroke(discontinua ? new BasicStroke(0.6f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    10, new float[] {3, 3}, 0) : new BasicStroke(0.7f));
            g.draw(new java.awt.geom.Line2D.Float(x, y, x + longitud, y));
        });
        finContenido = Math.max(finContenido, y);
    }

    public void casilla(float x, float y, float lado) {
        trazos.add(g -> {
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(0.7f));
            g.draw(new java.awt.geom.Rectangle2D.Float(x, y, lado, lado));
        });
        finContenido = Math.max(finContenido, y + lado);
    }

    public void marco(float x, float y, float anchoMarco, float alto, float radio) {
        trazos.add(g -> {
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(0.6f));
            g.draw(new java.awt.geom.RoundRectangle2D.Float(x, y, anchoMarco, alto, radio, radio));
        });
        finContenido = Math.max(finContenido, y + alto);
    }

    public void banda(String titulo, float x, float y, float anchoBanda, float alto) {
        Font fuente = new Font(Font.SANS_SERIF, Font.BOLD, 1).deriveFont(6.5f);
        float ascenso = fuente.getLineMetrics(titulo, METRICAS).getAscent();
        trazos.add(g -> {
            g.setColor(Color.BLACK);
            g.fill(new java.awt.geom.Rectangle2D.Float(x, y, anchoBanda, alto));
            g.setColor(Color.WHITE);
            g.setFont(fuente);
            g.drawString(titulo, x + 6, y + (alto - 6.5f) / 2 + ascenso - 1);
        });
        finContenido = Math.max(finContenido, y + alto);
    }

    public void logo(BufferedImage imagen, float x, float y, float anchoMaximo, float altoMaximo) {
        float escala = Math.min(anchoMaximo / imagen.getWidth(), altoMaximo / imagen.getHeight());
        float anchoLogo = imagen.getWidth() * escala;
        float altoLogo = imagen.getHeight() * escala;
        trazos.add(g -> g.drawImage(imagen, Math.round(x + (anchoMaximo - anchoLogo) / 2),
                Math.round(y), Math.round(anchoLogo), Math.round(altoLogo), null));
        finContenido = Math.max(finContenido, y + altoLogo);
    }

    public byte[] pdf(float altoMm) throws IOException, DocumentException {
        float alto = puntos(altoMm);
        validarAlto(alto);
        var salida = new ByteArrayOutputStream();
        var documento = new Document(new Rectangle(ancho, alto), 0, 0, 0, 0);
        try {
            var escritor = PdfWriter.getInstance(documento, salida);
            documento.addTitle("Rutas del Inca");
            documento.addCreator("Rutas del Inca");
            documento.open();
            Graphics2D grafico = escritor.getDirectContent().createGraphicsShapes(ancho, alto);
            try {
                dibujar(grafico, alto);
            } finally {
                grafico.dispose();
            }
        } finally {
            documento.close();
        }
        return salida.toByteArray();
    }

    public byte[] png(float altoMm) throws IOException {
        float alto = puntos(altoMm);
        validarAlto(alto);
        float escala = DPI / 72f;
        var imagen = new BufferedImage(Math.round(ancho * escala), Math.round(alto * escala),
                BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D grafico = imagen.createGraphics();
        try {
            grafico.scale(escala, escala);
            dibujar(grafico, alto);
        } finally {
            grafico.dispose();
        }
        var salida = new ByteArrayOutputStream();
        var escritor = ImageIO.getImageWritersByFormatName("png").next();
        try (var destino = ImageIO.createImageOutputStream(salida)) {
            escritor.setOutput(destino);
            var parametros = escritor.getDefaultWriteParam();
            var tipo = javax.imageio.ImageTypeSpecifier.createFromRenderedImage(imagen);
            var metadatos = escritor.getDefaultImageMetadata(tipo, parametros);
            var raiz = new IIOMetadataNode("javax_imageio_png_1.0");
            var resolucion = new IIOMetadataNode("pHYs");
            String pixelesPorMetro = String.valueOf(Math.round(DPI / 0.0254));
            resolucion.setAttribute("pixelsPerUnitXAxis", pixelesPorMetro);
            resolucion.setAttribute("pixelsPerUnitYAxis", pixelesPorMetro);
            resolucion.setAttribute("unitSpecifier", "meter");
            raiz.appendChild(resolucion);
            metadatos.mergeTree("javax_imageio_png_1.0", raiz);
            escritor.write(null, new javax.imageio.IIOImage(imagen, null, metadatos), parametros);
        } finally {
            escritor.dispose();
        }
        return salida.toByteArray();
    }

    private void dibujar(Graphics2D grafico, float alto) {
        grafico.setColor(Color.WHITE);
        grafico.fill(new java.awt.geom.Rectangle2D.Float(0, 0, ancho, alto));
        grafico.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        grafico.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        for (var trazo : trazos) trazo.accept(grafico);
    }

    private void validarAlto(float alto) {
        if (finContenido > alto) throw new IllegalStateException("El contenido excede el tamaño de impresión.");
    }

    private List<String> envolver(String contenido, Font fuente, float espacio) {
        List<String> lineas = new ArrayList<>();
        String limpio = contenido == null || contenido.isBlank() ? "—" : contenido.strip();
        for (String parrafo : limpio.split("\\R", -1)) {
            StringBuilder linea = new StringBuilder();
            for (String palabra : parrafo.split("\\s+")) {
                if (!linea.isEmpty() && medir(linea + " " + palabra, fuente) <= espacio) {
                    linea.append(' ').append(palabra);
                    continue;
                }
                if (!linea.isEmpty()) {
                    lineas.add(linea.toString());
                    linea.setLength(0);
                }
                for (int i = 0; i < palabra.length();) {
                    int codigo = palabra.codePointAt(i);
                    String caracter = new String(Character.toChars(codigo));
                    if (!linea.isEmpty() && medir(linea + caracter, fuente) > espacio) {
                        lineas.add(linea.toString());
                        linea.setLength(0);
                    }
                    linea.append(caracter);
                    i += Character.charCount(codigo);
                }
            }
            lineas.add(linea.toString());
        }
        return lineas;
    }

    private float medir(String texto, Font fuente) {
        return (float) fuente.getStringBounds(texto, METRICAS).getWidth();
    }
}
