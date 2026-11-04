package enviosrutasdelinca.service;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import enviosrutasdelinca.dto.EntregaResponse;
import enviosrutasdelinca.dto.MotorizadoRepartoResponse;
import enviosrutasdelinca.model.Envio;
import enviosrutasdelinca.model.Usuario;
import enviosrutasdelinca.repository.EnvioRepository;
import enviosrutasdelinca.repository.HistorialEstadoEnvioRepository;
import enviosrutasdelinca.repository.UsuarioRepository;
import enviosrutasdelinca.shared.model.EstadoEnvio;
import enviosrutasdelinca.shared.model.ModalidadEnvio;

@Service
public class EntregaService {
    private final EnvioRepository envios;
    private final UsuarioRepository usuarios;
    private final HistorialEstadoEnvioRepository historial;

    public EntregaService(EnvioRepository envios, UsuarioRepository usuarios, HistorialEstadoEnvioRepository historial) {
        this.envios = envios;
        this.usuarios = usuarios;
        this.historial = historial;
    }

    public List<EntregaResponse> porEntregar(Usuario usuario, EstadoEnvio estado, ModalidadEnvio modalidad) {
        Long sede = sedeConsulta(usuario, "COUNTER");
        if (estado != null && estado != EstadoEnvio.LLEGO_A_AGENCIA_DESTINO && estado != EstadoEnvio.LISTO_PARA_RECOJO) {
            throw invalido("Elige Por recibir o Listos para recojo.");
        }
        return envios.listarEntregas(sede, null, estado == null
                ? List.of("LLEGO_A_AGENCIA_DESTINO", "LISTO_PARA_RECOJO") : List.of(estado.name()), modalidad);
    }

    public List<EntregaResponse> reparto(Usuario usuario) {
        return envios.listarEntregas(sedeConsulta(usuario, "ENCARGADO_LOGISTICA"), null,
                List.of("LLEGO_A_AGENCIA_DESTINO", "EN_REPARTO"), ModalidadEnvio.DOMICILIO);
    }

    public List<MotorizadoRepartoResponse> motorizados(Usuario usuario) {
        return usuarios.listarMotorizados(sedeConsulta(usuario, "ENCARGADO_LOGISTICA"));
    }

    public List<EntregaResponse> misEntregas(Usuario usuario) {
        exigirRol(usuario, "MOTORIZADO");
        return envios.listarEntregas(usuario.agenciaId(), usuario.id(),
                List.of("LLEGO_A_AGENCIA_DESTINO", "EN_REPARTO"), ModalidadEnvio.DOMICILIO);
    }

    @Transactional
    public void listoRecojo(List<Long> ids, Usuario usuario) {
        exigirRol(usuario, "COUNTER");
        procesar(ids, id -> {
            Envio envio = deDestino(id, usuario);
            if (envio.modalidad() != ModalidadEnvio.AGENCIA) {
                throw invalido("Este envío es a domicilio y no se recoge en agencia.");
            }
            exigirLlegada(envio);
            avanzar(envio, EstadoEnvio.LISTO_PARA_RECOJO, usuario,
                    "Listo para recoger en " + envios.nombreAgencia(usuario.agenciaId()));
        });
    }

    @Transactional
    public void asignarMotorizado(List<Long> ids, long motorizadoId, Usuario usuario) {
        exigirRol(usuario, "ENCARGADO_LOGISTICA");
        usuarios.buscarPersonalActivo(motorizadoId)
                .filter(m -> "MOTORIZADO".equals(m.rol()) && Objects.equals(m.agenciaId(), usuario.agenciaId()))
                .orElseThrow(() -> invalido("El motorizado no pertenece a tu sede o está inactivo."));
        procesar(ids, id -> {
            Envio envio = deDestino(id, usuario);
            if (envio.modalidad() != ModalidadEnvio.DOMICILIO) {
                throw invalido("Este envío se recoge en agencia y no se asigna a un motorizado.");
            }
            if (envio.estado() == EstadoEnvio.EN_REPARTO) throw invalido("El envío ya salió a reparto.");
            exigirLlegada(envio);
            if (envios.asignarMotorizado(envio, motorizadoId) == 0) throw conflicto();
        });
    }

    @Transactional
    public void salirReparto(List<Long> ids, Usuario usuario) {
        exigirRol(usuario, "MOTORIZADO");
        procesar(ids, id -> {
            Envio envio = delMotorizado(id, usuario);
            exigirLlegada(envio);
            avanzar(envio, EstadoEnvio.EN_REPARTO, usuario, "Salió a reparto con " + usuario.nombre());
        });
    }

    @Transactional
    public void entregarEnAgencia(long id, String documento, Usuario usuario) {
        exigirRol(usuario, "COUNTER");
        Envio envio = deDestino(id, usuario);
        if (envio.modalidad() != ModalidadEnvio.AGENCIA || envio.estado() != EstadoEnvio.LISTO_PARA_RECOJO) {
            throw noEncontrado();
        }
        entregar(envio, documento, usuario);
    }

    @Transactional
    public void entregarDomicilio(long id, String documento, Usuario usuario) {
        exigirRol(usuario, "MOTORIZADO");
        Envio envio = delMotorizado(id, usuario);
        if (envio.estado() != EstadoEnvio.EN_REPARTO) throw noEncontrado();
        entregar(envio, documento, usuario);
    }

    private void entregar(Envio envio, String documento, Usuario usuario) {
        if (!Objects.equals(documento, envio.destinatarioDocumento())) {
            throw invalido("El documento no coincide con el del destinatario.");
        }
        avanzar(envio, EstadoEnvio.ENTREGADO, usuario,
                "Entregado a " + envio.destinatarioNombre() + " (documento " + documento + ")");
    }

    private void avanzar(Envio envio, EstadoEnvio estado, Usuario usuario, String comentario) {
        if (envios.avanzarEntrega(envio, estado) == 0) throw conflicto();
        historial.registrar(envio.id(), estado, usuario.id(), comentario);
    }

    private Envio deDestino(long id, Usuario usuario) {
        return envios.buscarPorId(id).filter(e -> Objects.equals(e.agenciaDestinoId(), usuario.agenciaId()))
                .orElseThrow(EntregaService::noEncontrado);
    }

    private Envio delMotorizado(long id, Usuario usuario) {
        Envio envio = deDestino(id, usuario);
        if (!Objects.equals(envio.motorizadoId(), usuario.id()) || envio.modalidad() != ModalidadEnvio.DOMICILIO) {
            throw noEncontrado();
        }
        return envio;
    }

    private Long sedeConsulta(Usuario usuario, String rol) {
        exigirRol(usuario, rol);
        return usuario.agenciaId();
    }

    private void exigirRol(Usuario usuario, String rol) {
        if (!rol.equals(usuario.rol()) || usuario.agenciaId() == null || !Boolean.TRUE.equals(usuario.activo())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para esta acción.");
        }
    }

    private void exigirLlegada(Envio envio) {
        if (envio.estado() != EstadoEnvio.LLEGO_A_AGENCIA_DESTINO) throw conflicto();
    }

    private void procesar(List<Long> ids, Consumer<Long> operacion) {
        if (ids == null || ids.isEmpty() || ids.size() > 100
                || ids.stream().anyMatch(id -> id == null || id < 1) || ids.stream().distinct().count() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona entre 1 y 100 envíos sin repetir.");
        }
        for (long id : ids.stream().sorted().toList()) {
            try {
                operacion.accept(id);
            } catch (ResponseStatusException error) {
                throw new ResponseStatusException(error.getStatusCode(), "Envío " + id + ": " + error.getReason());
            }
        }
    }

    private static ResponseStatusException noEncontrado() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el envío.");
    }

    private static ResponseStatusException conflicto() {
        return new ResponseStatusException(HttpStatus.CONFLICT,
                "El envío cambió mientras realizabas la operación. Actualiza la lista e inténtalo nuevamente.");
    }

    private static ResponseStatusException invalido(String mensaje) {
        return new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, mensaje);
    }
}
