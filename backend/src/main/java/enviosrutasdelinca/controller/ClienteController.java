package enviosrutasdelinca.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import enviosrutasdelinca.dto.ClienteBusquedaResponse;
import enviosrutasdelinca.service.ClienteService;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping("/buscar")
    public ClienteBusquedaResponse buscar(@RequestParam String documento) {
        return clienteService.buscarPorDocumento(documento);
    }
}
