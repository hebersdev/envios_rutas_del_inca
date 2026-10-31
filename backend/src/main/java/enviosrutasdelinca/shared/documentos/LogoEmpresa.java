package enviosrutasdelinca.shared.documentos;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class LogoEmpresa {

    private final BufferedImage imagen;

    public LogoEmpresa() {
        try (var entrada = new ClassPathResource("logo-rutas-del-inca.png").getInputStream()) {
            BufferedImage original = ImageIO.read(entrada);
            if (original == null) throw new IOException("El logo no es una imagen válida.");
            imagen = enNegro(original);
        } catch (IOException error) {
            throw new IllegalStateException("No se pudo cargar el logo de Rutas del Inca.", error);
        }
    }

    public BufferedImage imagen() {
        return imagen;
    }

    private BufferedImage enNegro(BufferedImage original) {
        int izquierda = original.getWidth();
        int superior = original.getHeight();
        int derecha = -1;
        int inferior = -1;
        for (int y = 0; y < original.getHeight(); y++) {
            for (int x = 0; x < original.getWidth(); x++) {
                if ((original.getRGB(x, y) >>> 24) >= 128) {
                    izquierda = Math.min(izquierda, x);
                    superior = Math.min(superior, y);
                    derecha = Math.max(derecha, x);
                    inferior = Math.max(inferior, y);
                }
            }
        }
        if (derecha < izquierda) throw new IllegalStateException("El logo está vacío.");
        var resultado = new BufferedImage(derecha - izquierda + 1, inferior - superior + 1,
                BufferedImage.TYPE_BYTE_BINARY);
        for (int y = 0; y < resultado.getHeight(); y++) {
            for (int x = 0; x < resultado.getWidth(); x++) {
                boolean visible = (original.getRGB(x + izquierda, y + superior) >>> 24) >= 128;
                resultado.setRGB(x, y, visible ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
            }
        }
        return resultado;
    }
}
