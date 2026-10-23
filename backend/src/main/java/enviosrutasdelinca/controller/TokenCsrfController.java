package enviosrutasdelinca.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class TokenCsrfController {

    @GetMapping("/api/csrf")
    public Map<String, String> obtenerToken(CsrfToken token) {
        return Map.of(
                "token", token.getToken(),
                "headerName", token.getHeaderName()
        );
    }
}
