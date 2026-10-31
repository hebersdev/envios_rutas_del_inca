package enviosrutasdelinca.service;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import enviosrutasdelinca.shared.model.AsignacionSolicitada;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import enviosrutasdelinca.dto.CotizacionResponse;
import enviosrutasdelinca.dto.EnvioRequest;
import enviosrutasdelinca.dto.EnvioResponse;
import enviosrutasdelinca.dto.EnvioResumenResponse;
import enviosrutasdelinca.dto.PaginaResponse;
import enviosrutasdelinca.model.Usuario;
import enviosrutasdelinca.repository.ClienteRepository;
import enviosrutasdelinca.repository.EnvioRepository;
import enviosrutasdelinca.repository.EnvioRepository.NuevoEnvio;
import enviosrutasdelinca.repository.HistorialEstadoEnvioRepository;
import enviosrutasdelinca.shared.model.EstadoEnvio;
import enviosrutasdelinca.shared.model.ModalidadEnvio;

@Service
public class EnvioService {

    private static final int INTENTOS_CODIGO = 10;
    private static final int TAMANIO_PAGINA = 10;
    private static final String ROL_COUNTER = "COUNTER";
    private static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";

    private final EnvioRepository envioRepository;
    private final HistorialEstadoEnvioRepository historialRepository;
    private final ClienteRepository clienteRepository;
    private final CotizacionService cotizacionService;
    private final ApplicationEventPublisher eventos;
    private final GeneradorCodigoEnvio generadorCodigo;

    public EnvioService(
            EnvioRepository envioRepository,
            HistorialEstadoEnvioRepository historialRepository,
            ClienteRepository clienteRepository,
            CotizacionService cotizacionService,
            ApplicationEventPublisher eventos,
            GeneradorCodigoEnvio generadorCodigo) {
        this.envioRepository = envioRepository;
        this.historialRepository = historialRepository;
        this.clienteRepository = clienteRepository;
        this.cotizacionService = cotizacionService;
        this.eventos = eventos;
        this.generadorCodigo = generadorCodigo;
    }

    @Transactional
    public EnvioResponse registrar(EnvioRequest solicitud, Usuario counter) {
        Long origenId = counter.agenciaId();
        if (!ROL_COUNTER.equals(counter.rol()) || origenId == null) {
            throw sinPermiso();
        }

        String nombreOrigen = envioRepository.nombreAgenciaActiva(origenId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNPROCESSABLE_CONTENT, "Tu sede no está activa."));

        if (solicitud.clienteId() != null) {
            if (!clienteRepository.existeClienteActivo(solicitud.clienteId())) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No se encontró al cliente o está inactivo.");
            }
        }

        if (Objects.equals(origenId, solicitud.agenciaDestinoId())) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_CONTENT, "El origen y el destino no pueden ser iguales.");
        }

        envioRepository.nombreAgenciaActiva(solicitud.agenciaDestinoId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNPROCESSABLE_CONTENT, "La sede de destino no existe o está inactiva."));

        if (solicitud.modalidad() == ModalidadEnvio.DOMICILIO && solicitud.direccionEntrega() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Indica la dirección de entrega.");
        }

        CotizacionResponse cotizacion = cotizacionService.cotizar(
                origenId,
                solicitud.agenciaDestinoId(),
                solicitud.pesoKg(),
                solicitud.altoCm(),
                solicitud.anchoCm(),
                solicitud.largoCm(),
                solicitud.modalidad());

        Long envioId = null;
        for (int intento = 0; intento < INTENTOS_CODIGO && envioId == null; intento++) {
            envioId = envioRepository.intentarCrear(new NuevoEnvio(
                    generadorCodigo.generar(),
                    solicitud.clienteId(),
                    counter.id(),
                    solicitud.modalidad(),
                    solicitud.descripcionPaquete(),
                    solicitud.pesoKg(),
                    solicitud.altoCm(),
                    solicitud.anchoCm(),
                    solicitud.largoCm(),
                    cotizacion.pesoVolumetricoKg(),
                    cotizacion.pesoFacturableKg(),
                    solicitud.remitenteNombre(),
                    solicitud.remitenteDocumento(),
                    solicitud.remitenteTelefono(),
                    solicitud.destinatarioNombre(),
                    solicitud.destinatarioDocumento(),
                    solicitud.destinatarioTelefono(),
                    solicitud.direccionEntrega(),
                    origenId,
                    solicitud.agenciaDestinoId(),
                    cotizacion.total())).orElse(null);
        }
        if (envioId == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo generar el código de envío. Inténtalo nuevamente.");
        }

        historialRepository.registrar(
                envioId,
                EstadoEnvio.REGISTRADO,
                counter.id(),
                "Envío registrado en " + nombreOrigen);

        eventos.publishEvent(new AsignacionSolicitada(origenId, counter.id()));

        return envioRepository.buscarDetalle(envioId, null)
                .map(e -> e.conPuedeCambiarEstado(false))
                .orElseThrow(() -> new IllegalStateException("No se pudo leer el envío recién creado."));
    }

    public EnvioResponse obtener(long id, Usuario usuario) {
        EnvioResponse base = envioRepository.buscarDetalle(id, filtroSede(usuario))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No se encontró el envío."));
        return base.conPuedeCambiarEstado(false);
    }

    public PaginaResponse<EnvioResumenResponse> listar(
            Usuario usuario,
            EstadoEnvio estado,
            String codigo,
            int pagina) {
        Long agenciaId = filtroSede(usuario);
        int paginaActual = Math.max(pagina, 1);
        String patron = armarPatron(codigo);

        long total = envioRepository.contar(agenciaId, estado, patron);
        List<EnvioResumenResponse> contenido = envioRepository.listar(
                agenciaId,
                estado,
                patron,
                TAMANIO_PAGINA,
                (paginaActual - 1) * TAMANIO_PAGINA);

        int totalPaginas = (int) Math.ceil(total / (double) TAMANIO_PAGINA);
        return new PaginaResponse<>(contenido, paginaActual, TAMANIO_PAGINA, total, totalPaginas);
    }

    public EnvioResponse cambiarEstado(long envioId, EstadoEnvio nuevoEstado, Usuario counter) {
        if (!ROL_COUNTER.equals(counter.rol()) || counter.agenciaId() == null) throw sinPermiso();
        envioRepository.buscarPorId(envioId)
                .filter(e -> Objects.equals(e.agenciaDestinoId(), counter.agenciaId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el envío."));
        throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                "Gestiona la entrega desde Por entregar. Para domicilio utiliza el flujo de Reparto.");
    }

    private Long filtroSede(Usuario usuario) {
        if (ROL_ADMINISTRADOR.equals(usuario.rol())) {
            return null;
        }
        if (!ROL_COUNTER.equals(usuario.rol()) || usuario.agenciaId() == null) {
            throw sinPermiso();
        }
        return usuario.agenciaId();
    }

    private String armarPatron(String codigo) {
        if (codigo == null) {
            return null;
        }
        String limpio = codigo.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9-]", "");
        return limpio.isEmpty() ? null : "%" + limpio + "%";
    }

    private ResponseStatusException sinPermiso() {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para esta acción.");
    }
}
