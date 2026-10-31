package enviosrutasdelinca.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import enviosrutasdelinca.dto.ClienteBusquedaResponse;
import enviosrutasdelinca.repository.ClienteRepository;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public ClienteBusquedaResponse buscarPorDocumento(String documento) {
        String limpio = documento == null ? "" : documento.trim();

        if (!limpio.matches("\\d{8}|\\d{11}")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Ingresa un DNI de 8 dígitos o un RUC de 11.");
        }

        return clienteRepository.buscarPorDocumento(limpio)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No hay un cliente registrado con ese documento. Pídele que cree su cuenta."));
    }
}