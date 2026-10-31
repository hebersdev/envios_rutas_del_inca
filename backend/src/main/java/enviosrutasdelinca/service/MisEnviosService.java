package enviosrutasdelinca.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import enviosrutasdelinca.dto.EnvioClienteResponse;
import enviosrutasdelinca.dto.PaginaResponse;
import enviosrutasdelinca.model.Usuario;
import enviosrutasdelinca.repository.EnvioRepository;
import enviosrutasdelinca.shared.model.EstadoEnvio;

@Service
public class MisEnviosService {
    private static final int TAMANIO = 20;
    private final EnvioRepository envios;

    public MisEnviosService(EnvioRepository envios) {
        this.envios = envios;
    }

    @Transactional(readOnly = true)
    public PaginaResponse<EnvioClienteResponse> listar(Usuario usuario, EstadoEnvio estado, int pagina) {
        return listar(usuario, estado, null, pagina);
    }

    @Transactional(readOnly = true)
    public PaginaResponse<EnvioClienteResponse> listar(
            Usuario usuario, EstadoEnvio estado, Boolean anteriores, int pagina) {
        if (!"CLIENTE".equals(usuario.rol()) || !Boolean.TRUE.equals(usuario.activo())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esta opción es para clientes registrados.");
        }
        if (pagina < 1 || pagina > Integer.MAX_VALUE / TAMANIO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El número de página no es válido.");
        }
        String documento = usuario.dni() != null ? usuario.dni() : usuario.ruc();
        long total = envios.contarDelCliente(usuario.id(), documento, estado, anteriores);
        return new PaginaResponse<>(envios.listarDelCliente(
                usuario.id(), documento, estado, anteriores, TAMANIO, (pagina - 1) * TAMANIO),
                pagina, TAMANIO, total, (int) Math.ceil(total / (double) TAMANIO));
    }
}
