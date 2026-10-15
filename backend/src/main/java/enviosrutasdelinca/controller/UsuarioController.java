
package enviosrutasdelinca.controller;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import enviosrutasdelinca.model.Usuario;
import enviosrutasdelinca.service.UsuarioService;
import jakarta.servlet.http.HttpSession;

@Controller
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
    }

    @PostMapping("/login")
    public String iniciarSesion(
            @RequestParam String correo,
            @RequestParam String clave,
            HttpSession session,
            Model model) {

        try {
            Usuario usuario =
                    usuarioService.iniciarSesion(correo, clave);

            // Evitar reutilizar una sesión anterior
            session.invalidate();

            // Crear una sesión nueva
            HttpSession nuevaSesion =
                    obtenerNuevaSesion(session);

            nuevaSesion.setAttribute("usuarioId", usuario.getId());
            nuevaSesion.setAttribute("usuarioNombre", usuario.getNombre());
            nuevaSesion.setAttribute("usuarioRol", usuario.getRol());

            return "redirect:/dashboard";

        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    private HttpSession obtenerNuevaSesion(HttpSession sesionAnterior) {
        throw new UnsupportedOperationException(
                "S"
        );
    }
}
