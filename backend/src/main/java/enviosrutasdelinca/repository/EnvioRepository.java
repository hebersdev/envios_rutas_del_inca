package enviosrutasdelinca.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import enviosrutasdelinca.dto.EnvioResponse;
import enviosrutasdelinca.dto.EntregaResponse;
import enviosrutasdelinca.dto.EnvioClienteResponse;
import enviosrutasdelinca.dto.EnvioResumenResponse;
import enviosrutasdelinca.model.Envio;
import enviosrutasdelinca.shared.model.EstadoEnvio;
import enviosrutasdelinca.shared.model.ModalidadEnvio;
import enviosrutasdelinca.shared.util.Filas;

@Repository
public class EnvioRepository {

    private static final String SELECT_DETALLE = """
            SELECT e.id, e.codigo_envio, e.estado, e.modalidad, e.descripcion_paquete,
                   e.peso_kg, e.alto_cm, e.ancho_cm, e.largo_cm,
                   e.peso_volumetrico_kg, e.peso_facturable_kg,
                   e.cliente_id,
                   e.remitente_nombre, e.remitente_documento, e.remitente_telefono,
                   e.destinatario_nombre, e.destinatario_documento, e.destinatario_telefono,
                   e.direccion_entrega, e.costo_envio, e.fecha_registro,
                   ao.id AS origen_id, ao.nombre AS origen_nombre,
                   uo.ciudad AS origen_ciudad, uo.direccion AS origen_direccion,
                   ad.id AS destino_id, ad.nombre AS destino_nombre,
                   ud.ciudad AS destino_ciudad, ud.direccion AS destino_direccion
            FROM envios e
            JOIN agencias ao ON ao.id = e.agencia_origen_id
            JOIN ubicaciones uo ON uo.id = ao.ubicacion_id
            JOIN agencias ad ON ad.id = e.agencia_destino_id
            JOIN ubicaciones ud ON ud.id = ad.ubicacion_id
            """;

    private static final String FILTRO_LISTA = """
            WHERE (
              CAST(:agenciaId AS BIGINT) IS NULL
              OR e.agencia_origen_id = :agenciaId
              OR e.agencia_destino_id = :agenciaId
            )
              AND (CAST(:estado AS VARCHAR) IS NULL OR e.estado = :estado)
              AND (CAST(:patron AS VARCHAR) IS NULL OR e.codigo_envio LIKE :patron)
            """;

    private static final RowMapper<EnvioResponse> MAPPER_DETALLE = (rs, fila) -> new EnvioResponse(
            rs.getLong("id"),
            rs.getString("codigo_envio"),
            EstadoEnvio.valueOf(rs.getString("estado")),
            ModalidadEnvio.valueOf(rs.getString("modalidad")),
            new EnvioResponse.Remitente(
                    rs.getObject("cliente_id", Long.class),
                    rs.getString("remitente_nombre"),
                    rs.getString("remitente_documento"),
                    rs.getString("remitente_telefono")),
            new EnvioResponse.Sede(
                    rs.getLong("origen_id"),
                    rs.getString("origen_nombre"),
                    rs.getString("origen_ciudad"),
                    rs.getString("origen_direccion")),
            new EnvioResponse.Sede(
                    rs.getLong("destino_id"),
                    rs.getString("destino_nombre"),
                    rs.getString("destino_ciudad"),
                    rs.getString("destino_direccion")),
            rs.getString("descripcion_paquete"),
            rs.getBigDecimal("peso_kg"),
            rs.getBigDecimal("alto_cm"),
            rs.getBigDecimal("ancho_cm"),
            rs.getBigDecimal("largo_cm"),
            rs.getBigDecimal("peso_volumetrico_kg"),
            rs.getBigDecimal("peso_facturable_kg"),
            rs.getString("destinatario_nombre"),
            rs.getString("destinatario_documento"),
            rs.getString("destinatario_telefono"),
            rs.getString("direccion_entrega"),
            rs.getBigDecimal("costo_envio"),
            Filas.fecha(rs, "fecha_registro"),
            false);

    private static final RowMapper<EnvioResumenResponse> MAPPER_RESUMEN = (rs, fila) -> new EnvioResumenResponse(
            rs.getLong("id"),
            rs.getString("codigo_envio"),
            EstadoEnvio.valueOf(rs.getString("estado")),
            ModalidadEnvio.valueOf(rs.getString("modalidad")),
            rs.getBigDecimal("peso_kg"),
            rs.getBigDecimal("costo_envio"),
            Filas.fecha(rs, "fecha_registro"),
            rs.getString("remitente_nombre"),
            rs.getString("destino_nombre"));

    private static final RowMapper<CabeceraSeguimiento> MAPPER_CABECERA = (rs, fila) -> new CabeceraSeguimiento(
            rs.getLong("id"),
            rs.getString("codigo_envio"),
            EstadoEnvio.valueOf(rs.getString("estado")),
            ModalidadEnvio.valueOf(rs.getString("modalidad")),
            rs.getString("origen"),
            rs.getString("destino"));

    private final JdbcClient jdbc;

    public EnvioRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<String> nombreAgenciaActiva(long agenciaId) {
        return jdbc.sql("SELECT nombre FROM agencias WHERE id = :id AND activo")
                .param("id", agenciaId)
                .query(String.class)
                .optional();
    }

    public String nombreAgencia(long id) {
        return jdbc.sql("SELECT nombre FROM agencias WHERE id = :id")
                .param("id", id).query(String.class).single();
    }

    public List<EntregaResponse> listarEntregas(Long sede, Long motorizado,
            List<String> estados, ModalidadEnvio modalidad) {
        return jdbc.sql("""
                SELECT e.id, e.codigo_envio, e.estado, e.modalidad, e.destinatario_nombre,
                       e.destinatario_telefono,
                       CASE WHEN e.modalidad = 'DOMICILIO' THEN e.direccion_entrega END AS direccion_entrega,
                       llegada.fecha AS fecha_llegada, e.motorizado_id, m.nombre AS motorizado_nombre,
                       a.nombre AS sede_destino
                FROM envios e
                JOIN agencias a ON a.id = e.agencia_destino_id
                LEFT JOIN usuarios m ON m.id = e.motorizado_id
                LEFT JOIN LATERAL (
                    SELECT MAX(h.fecha) AS fecha FROM historial_estados_envio h
                    WHERE h.envio_id = e.id AND h.estado = 'LLEGO_A_AGENCIA_DESTINO'
                ) llegada ON TRUE
                WHERE (CAST(:sede AS BIGINT) IS NULL OR e.agencia_destino_id = :sede)
                  AND (CAST(:motorizado AS BIGINT) IS NULL OR e.motorizado_id = :motorizado)
                  AND e.estado IN (:estados)
                  AND (CAST(:modalidad AS VARCHAR) IS NULL OR e.modalidad = :modalidad)
                ORDER BY llegada.fecha ASC NULLS LAST, e.fecha_registro, e.id
                """).param("sede", sede).param("motorizado", motorizado).param("estados", estados)
                .param("modalidad", modalidad == null ? null : modalidad.name())
                .query((rs, fila) -> new EntregaResponse(rs.getLong("id"),
                        rs.getString("codigo_envio"), EstadoEnvio.valueOf(rs.getString("estado")),
                        ModalidadEnvio.valueOf(rs.getString("modalidad")), rs.getString("destinatario_nombre"),
                        rs.getString("destinatario_telefono"), rs.getString("direccion_entrega"),
                        Filas.fecha(rs, "fecha_llegada"), rs.getObject("motorizado_id", Long.class),
                        rs.getString("motorizado_nombre"), rs.getString("sede_destino"))).list();
    }

    public int avanzarEntrega(Envio envio, EstadoEnvio siguiente) {
        return jdbc.sql("""
                UPDATE envios SET estado = :siguiente,
                    fecha_entrega = CASE WHEN :siguiente = 'ENTREGADO' THEN LOCALTIMESTAMP ELSE fecha_entrega END
                WHERE id = :id AND estado = :anterior AND modalidad = :modalidad
                  AND agencia_destino_id = :sede
                  AND motorizado_id IS NOT DISTINCT FROM CAST(:motorizado AS BIGINT)
                """).param("id", envio.id()).param("siguiente", siguiente.name())
                .param("anterior", envio.estado().name()).param("modalidad", envio.modalidad().name())
                .param("sede", envio.agenciaDestinoId()).param("motorizado", envio.motorizadoId()).update();
    }

    public int asignarMotorizado(Envio envio, long motorizado) {
        return jdbc.sql("""
                UPDATE envios SET motorizado_id = :nuevo
                WHERE id = :id AND estado = 'LLEGO_A_AGENCIA_DESTINO' AND modalidad = 'DOMICILIO'
                  AND agencia_destino_id = :sede
                  AND motorizado_id IS NOT DISTINCT FROM CAST(:anterior AS BIGINT)
                  AND EXISTS (SELECT 1 FROM usuarios u WHERE u.id = :nuevo
                      AND u.rol = 'MOTORIZADO' AND u.activo AND u.agencia_id = :sede)
                """).param("id", envio.id()).param("sede", envio.agenciaDestinoId())
                .param("nuevo", motorizado).param("anterior", envio.motorizadoId()).update();
    }

    public Optional<Long> intentarCrear(NuevoEnvio datos) {
        return jdbc.sql("""
                        INSERT INTO envios (
                            codigo_envio, cliente_id, counter_recepcion_id, modalidad,
                            descripcion_paquete, peso_kg, alto_cm, ancho_cm, largo_cm,
                            peso_volumetrico_kg, peso_facturable_kg,
                            remitente_nombre, remitente_documento, remitente_telefono,
                            destinatario_nombre, destinatario_documento, destinatario_telefono,
                            direccion_entrega, agencia_origen_id, agencia_destino_id, costo_envio
                        )
                        VALUES (
                            :codigo, :clienteId, :counterId, :modalidad,
                            :descripcion, :peso, :alto, :ancho, :largo,
                            :pesoVolumetrico, :pesoFacturable,
                            :remNombre, :remDocumento, :remTelefono,
                            :destNombre, :destDocumento, :destTelefono,
                            :direccion, :origenId, :destinoId, :costo
                        )
                        ON CONFLICT (codigo_envio) DO NOTHING
                        RETURNING id
                        """)
                .param("codigo", datos.codigoEnvio())
                .param("clienteId", datos.clienteId())
                .param("counterId", datos.counterId())
                .param("modalidad", datos.modalidad().name())
                .param("descripcion", datos.descripcionPaquete())
                .param("peso", datos.pesoKg())
                .param("alto", datos.altoCm())
                .param("ancho", datos.anchoCm())
                .param("largo", datos.largoCm())
                .param("pesoVolumetrico", datos.pesoVolumetricoKg())
                .param("pesoFacturable", datos.pesoFacturableKg())
                .param("remNombre", datos.remitenteNombre())
                .param("remDocumento", datos.remitenteDocumento())
                .param("remTelefono", datos.remitenteTelefono())
                .param("destNombre", datos.destinatarioNombre())
                .param("destDocumento", datos.destinatarioDocumento())
                .param("destTelefono", datos.destinatarioTelefono())
                .param("direccion", datos.direccionEntrega())
                .param("origenId", datos.agenciaOrigenId())
                .param("destinoId", datos.agenciaDestinoId())
                .param("costo", datos.costoEnvio())
                .query(Long.class)
                .optional();
    }

    public Optional<EnvioResponse> buscarDetalle(long id, Long agenciaId) {
        return jdbc.sql(SELECT_DETALLE + """
                        WHERE e.id = :id
                          AND (CAST(:agenciaId AS BIGINT) IS NULL
                               OR e.agencia_origen_id = :agenciaId
                               OR e.agencia_destino_id = :agenciaId)
                        """)
                .param("id", id)
                .param("agenciaId", agenciaId)
                .query(MAPPER_DETALLE)
                .optional();
    }

    public Optional<Envio> buscarPorId(long id) {
        return jdbc.sql("""
                        SELECT id, codigo_envio, cliente_id, counter_recepcion_id, modalidad,
                               descripcion_paquete, peso_kg, alto_cm, ancho_cm, largo_cm,
                               destinatario_nombre, destinatario_documento, destinatario_telefono,
                               direccion_entrega, agencia_origen_id, agencia_destino_id,
                               costo_envio, estado, viaje_id, motorizado_id,
                               fecha_registro, fecha_entrega
                        FROM envios WHERE id = :id
                        """)
                .param("id", id)
                .query((rs, fila) -> new Envio(
                        rs.getLong("id"),
                        rs.getString("codigo_envio"),
                        rs.getObject("cliente_id", Long.class),
                        rs.getLong("counter_recepcion_id"),
                        ModalidadEnvio.valueOf(rs.getString("modalidad")),
                        rs.getString("descripcion_paquete"),
                        rs.getBigDecimal("peso_kg"),
                        rs.getBigDecimal("alto_cm"),
                        rs.getBigDecimal("ancho_cm"),
                        rs.getBigDecimal("largo_cm"),
                        rs.getString("destinatario_nombre"),
                        rs.getString("destinatario_documento"),
                        rs.getString("destinatario_telefono"),
                        rs.getString("direccion_entrega"),
                        rs.getLong("agencia_origen_id"),
                        rs.getLong("agencia_destino_id"),
                        rs.getBigDecimal("costo_envio"),
                        EstadoEnvio.valueOf(rs.getString("estado")),
                        rs.getObject("viaje_id", Long.class),
                        rs.getObject("motorizado_id", Long.class),
                        Filas.fecha(rs, "fecha_registro"),
                        Filas.fecha(rs, "fecha_entrega")))
                .optional();
    }

    public List<EnvioResumenResponse> listar(
            Long agenciaId,
            EstadoEnvio estado,
            String patronCodigo,
            int limite,
            int desplazamiento) {
        return jdbc.sql("""
                        SELECT e.id, e.codigo_envio, e.estado, e.modalidad, e.peso_kg,
                               e.costo_envio, e.fecha_registro,
                               e.remitente_nombre AS remitente_nombre,
                               ad.nombre AS destino_nombre
                        FROM envios e
                        JOIN agencias ad ON ad.id = e.agencia_destino_id
                        """ + FILTRO_LISTA + """
                        ORDER BY e.fecha_registro DESC, e.id DESC
                        LIMIT :limite OFFSET :desplazamiento
                        """)
                .param("agenciaId", agenciaId)
                .param("estado", estado == null ? null : estado.name())
                .param("patron", patronCodigo)
                .param("limite", limite)
                .param("desplazamiento", desplazamiento)
                .query(MAPPER_RESUMEN)
                .list();
    }

    public long contar(Long agenciaId, EstadoEnvio estado, String patronCodigo) {
        return jdbc.sql("SELECT COUNT(*) FROM envios e " + FILTRO_LISTA)
                .param("agenciaId", agenciaId)
                .param("estado", estado == null ? null : estado.name())
                .param("patron", patronCodigo)
                .query(Long.class)
                .single();
    }

    public List<EnvioClienteResponse> listarDelCliente(
            long clienteId, String documento, EstadoEnvio estado, Boolean anteriores,
            int limite, int desplazamiento) {
        return jdbc.sql("""
                SELECT e.codigo_envio, e.estado, e.modalidad, ao.nombre AS origen, ad.nombre AS destino,
                       e.fecha_registro, e.fecha_entrega, e.costo_envio,
                       e.descripcion_paquete, e.peso_kg, e.destinatario_nombre, e.direccion_entrega
                FROM envios e
                JOIN agencias ao ON ao.id = e.agencia_origen_id
                JOIN agencias ad ON ad.id = e.agencia_destino_id
                WHERE (e.cliente_id = :clienteId
                       OR (e.cliente_id IS NULL AND e.remitente_documento = :documento))
                  AND (CAST(:estado AS VARCHAR) IS NULL OR e.estado = :estado)
                  AND (CAST(:anteriores AS BOOLEAN) IS NULL
                       OR (e.estado = 'ENTREGADO') = :anteriores)
                ORDER BY e.fecha_registro DESC, e.id DESC
                LIMIT :limite OFFSET :desplazamiento
                """).param("clienteId", clienteId).param("documento", documento)
                .param("estado", estado == null ? null : estado.name())
                .param("anteriores", anteriores)
                .param("limite", limite).param("desplazamiento", desplazamiento)
                .query((rs, fila) -> new EnvioClienteResponse(rs.getString("codigo_envio"),
                        EstadoEnvio.valueOf(rs.getString("estado")), ModalidadEnvio.valueOf(rs.getString("modalidad")),
                        rs.getString("origen"), rs.getString("destino"), Filas.fecha(rs, "fecha_registro"),
                        Filas.fecha(rs, "fecha_entrega"), rs.getBigDecimal("costo_envio"),
                        rs.getString("descripcion_paquete"), rs.getBigDecimal("peso_kg"),
                        rs.getString("destinatario_nombre"), rs.getString("direccion_entrega"))).list();
    }

    public long contarDelCliente(long clienteId, String documento, EstadoEnvio estado, Boolean anteriores) {
        return jdbc.sql("""
                SELECT COUNT(*) FROM envios e
                WHERE (e.cliente_id = :clienteId
                       OR (e.cliente_id IS NULL AND e.remitente_documento = :documento))
                  AND (CAST(:estado AS VARCHAR) IS NULL OR e.estado = :estado)
                  AND (CAST(:anteriores AS BOOLEAN) IS NULL
                       OR (e.estado = 'ENTREGADO') = :anteriores)
                """).param("clienteId", clienteId).param("documento", documento)
                .param("estado", estado == null ? null : estado.name())
                .param("anteriores", anteriores)
                .query(Long.class).single();
    }

    public int cambiarEstado(long envioId, EstadoEnvio nuevoEstado) {
        return jdbc.sql("UPDATE envios SET estado = :estado WHERE id = :id")
                .param("id", envioId)
                .param("estado", nuevoEstado.name())
                .update();
    }

    public int cambiarEstadoConFechaEntrega(long envioId, EstadoEnvio nuevoEstado) {
        return jdbc.sql("""
                        UPDATE envios
                        SET estado = :estado,
                            fecha_entrega = CASE WHEN :estado = 'ENTREGADO' THEN now() ELSE fecha_entrega END
                        WHERE id = :id
                        """)
                .param("id", envioId)
                .param("estado", nuevoEstado.name())
                .update();
    }

    public Optional<CabeceraSeguimiento> buscarCabeceraSeguimiento(String codigo) {
        return jdbc.sql("""
                        SELECT e.id, e.codigo_envio, e.estado, e.modalidad,
                               uo.ciudad AS origen, ud.ciudad AS destino
                        FROM envios e
                        JOIN agencias ao ON ao.id = e.agencia_origen_id
                        JOIN ubicaciones uo ON uo.id = ao.ubicacion_id
                        JOIN agencias ad ON ad.id = e.agencia_destino_id
                        JOIN ubicaciones ud ON ud.id = ad.ubicacion_id
                        WHERE e.codigo_envio = :codigo
                        """)
                .param("codigo", codigo)
                .query(MAPPER_CABECERA)
                .optional();
    }

    public record NuevoEnvio(
            String codigoEnvio,
            Long clienteId,
            Long counterId,
            ModalidadEnvio modalidad,
            String descripcionPaquete,
            BigDecimal pesoKg,
            BigDecimal altoCm,
            BigDecimal anchoCm,
            BigDecimal largoCm,
            BigDecimal pesoVolumetricoKg,
            BigDecimal pesoFacturableKg,
            String remitenteNombre,
            String remitenteDocumento,
            String remitenteTelefono,
            String destinatarioNombre,
            String destinatarioDocumento,
            String destinatarioTelefono,
            String direccionEntrega,
            Long agenciaOrigenId,
            Long agenciaDestinoId,
            BigDecimal costoEnvio) {
    }

    public record CabeceraSeguimiento(
            Long id,
            String codigoEnvio,
            EstadoEnvio estado,
            ModalidadEnvio modalidad,
            String origen,
            String destino) {
    }
}
