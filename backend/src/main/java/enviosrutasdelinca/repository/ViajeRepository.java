package enviosrutasdelinca.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import enviosrutasdelinca.dto.ViajeResponse;
import enviosrutasdelinca.model.CargaEnvio;
import enviosrutasdelinca.shared.model.EstadoViaje;
import enviosrutasdelinca.shared.util.Filas;
import java.math.BigDecimal;

@Repository
public class ViajeRepository {

    private static final String SELECT_VIAJE = """
            SELECT v.*, c.placa, c.peso_maximo_kg, c.volumen_maximo_m,
                   ao.nombre AS origen, ad.nombre AS destino,
                   COALESCE(carga.peso, 0) AS peso, COALESCE(carga.volumen, 0) AS volumen,
                   COALESCE(carga.cantidad, 0) AS cantidad, COALESCE(carga.sin_medidas, false) AS sin_medidas
            FROM viajes v
            JOIN camiones c ON c.id = v.camion_id
            JOIN agencias ao ON ao.id = v.agencia_origen_id
            JOIN agencias ad ON ad.id = v.agencia_destino_id
            LEFT JOIN LATERAL (
                SELECT SUM(e.peso_kg) AS peso,
                       SUM(COALESCE(e.alto_cm * e.ancho_cm * e.largo_cm / 1000000, 0)) AS volumen,
                       COUNT(*) AS cantidad,
                       BOOL_OR(e.alto_cm IS NULL OR e.ancho_cm IS NULL OR e.largo_cm IS NULL) AS sin_medidas
                FROM envios e WHERE e.viaje_id = v.id
            ) carga ON true
            """;
    private static final String SELECT_ENVIO = """
            SELECT e.id, e.codigo_envio, e.agencia_destino_id, a.nombre AS destino,
                   e.peso_kg, e.alto_cm, e.ancho_cm, e.largo_cm
            FROM envios e JOIN agencias a ON a.id = e.agencia_destino_id
            """;

    private final JdbcClient jdbc;

    public ViajeRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public void bloquearSede(long agenciaId) {
        jdbc.sql("SELECT pg_advisory_xact_lock(:id)")
                .param("id", agenciaId).query((rs, fila) -> true).single();
    }

    public List<ViajeResponse> listar(Long agenciaId, EstadoViaje estado) {
        return jdbc.sql(SELECT_VIAJE + """
                WHERE (CAST(:sede AS BIGINT) IS NULL OR v.agencia_origen_id = :sede OR v.agencia_destino_id = :sede)
                  AND (CAST(:estado AS VARCHAR) IS NULL OR v.estado = :estado)
                ORDER BY v.fecha_creacion DESC, v.id DESC
                """)
                .param("sede", agenciaId).param("estado", estado == null ? null : estado.name())
                .query(this::mapearViaje).list();
    }

    public Optional<ViajeResponse> buscar(long id, boolean bloquear) {
        return jdbc.sql(SELECT_VIAJE + " WHERE v.id = :id" + (bloquear ? " FOR UPDATE OF v" : ""))
                .param("id", id).query(this::mapearViaje).optional();
    }

    public List<CargaEnvio> pendientes(long agenciaId) {
        return jdbc.sql(SELECT_ENVIO + """
                WHERE e.agencia_origen_id = :sede AND e.estado = 'REGISTRADO' AND e.viaje_id IS NULL
                ORDER BY e.fecha_registro, e.id
                FOR UPDATE OF e
                """)
                .param("sede", agenciaId).query(this::mapearEnvio).list();
    }

    public List<CargaEnvio> envios(long viajeId) {
        return jdbc.sql(SELECT_ENVIO + " WHERE e.viaje_id = :id ORDER BY e.fecha_registro, e.id")
                .param("id", viajeId).query(this::mapearEnvio).list();
    }

    public List<ViajeResponse> programados(long origenId, long destinoId) {
        return jdbc.sql(SELECT_VIAJE + """
                WHERE v.agencia_origen_id = :origen AND v.agencia_destino_id = :destino
                  AND v.estado = 'PROGRAMADO' AND c.activo AND c.estado = 'DISPONIBLE'
                  AND c.agencia_actual_id = :origen
                ORDER BY v.fecha_creacion, v.id FOR UPDATE OF v
                """)
                .param("origen", origenId).param("destino", destinoId).query(this::mapearViaje).list();
    }

    public List<CamionDisponible> camionesLibres(long agenciaId) {
        return jdbc.sql("""
                SELECT c.id, c.placa, c.peso_maximo_kg, c.volumen_maximo_m
                FROM camiones c
                WHERE c.agencia_actual_id = :sede AND c.activo AND c.estado = 'DISPONIBLE'
                  AND NOT EXISTS (SELECT 1 FROM viajes v WHERE v.camion_id = c.id
                                  AND v.estado IN ('PROGRAMADO', 'EN_RUTA'))
                ORDER BY c.peso_maximo_kg, c.volumen_maximo_m, c.id
                FOR UPDATE OF c
                """)
                .param("sede", agenciaId)
                .query((rs, fila) -> new CamionDisponible(rs.getLong("id"), rs.getString("placa"),
                        rs.getBigDecimal("peso_maximo_kg"), rs.getBigDecimal("volumen_maximo_m")))
                .list();
    }

    public boolean cabeEnLaFlota(CargaEnvio envio) {
        return jdbc.sql("""
                SELECT EXISTS (SELECT 1 FROM camiones WHERE activo
                    AND peso_maximo_kg * :limite >= :peso AND volumen_maximo_m * :limite >= :volumen)
                """)
                .param("limite", enviosrutasdelinca.model.CapacidadCarga.LIMITE_OCUPACION)
                .param("peso", envio.pesoKg()).param("volumen", envio.volumenM())
                .query(Boolean.class).single();
    }

    public long crear(long camionId, long origenId, long destinoId) {
        return jdbc.sql("""
                INSERT INTO viajes (camion_id, agencia_origen_id, agencia_destino_id)
                VALUES (:camion, :origen, :destino) RETURNING id
                """)
                .param("camion", camionId).param("origen", origenId).param("destino", destinoId)
                .query(Long.class).single();
    }

    public int asignar(long envioId, long viajeId) {
        return jdbc.sql("""
                UPDATE envios SET viaje_id = :viaje, estado = 'ASIGNADO_A_VIAJE'
                WHERE id = :id AND estado = 'REGISTRADO' AND viaje_id IS NULL
                """).param("id", envioId).param("viaje", viajeId).update();
    }

    public List<Long> cambiarEnvios(long viajeId, String estado, boolean liberar) {
        return jdbc.sql("""
                UPDATE envios SET estado = :estado,
                    viaje_id = CASE WHEN :liberar THEN NULL ELSE viaje_id END
                WHERE viaje_id = :viaje RETURNING id
                """)
                .param("estado", estado).param("liberar", liberar).param("viaje", viajeId)
                .query(Long.class).list();
    }

    public void despachar(long id, long usuarioId) {
        jdbc.sql("""
                UPDATE viajes SET estado = 'EN_RUTA', fecha_salida = now(), encargado_salida_id = :usuario
                WHERE id = :id
                """).param("id", id).param("usuario", usuarioId).update();
    }

    public void registrarLlegada(long id, long usuarioId) {
        jdbc.sql("""
                UPDATE viajes SET estado = 'LLEGADO', fecha_llegada = now(), encargado_llegada_id = :usuario
                WHERE id = :id
                """).param("id", id).param("usuario", usuarioId).update();
    }

    public void finalizar(long id) {
        jdbc.sql("UPDATE viajes SET estado = 'FINALIZADO' WHERE id = :id").param("id", id).update();
    }

    public void eliminar(long id) {
        jdbc.sql("DELETE FROM viajes WHERE id = :id").param("id", id).update();
    }

    public void actualizarCamion(long id, String estado, long agenciaId) {
        jdbc.sql("UPDATE camiones SET estado = :estado, agencia_actual_id = :sede WHERE id = :id")
                .param("id", id).param("estado", estado).param("sede", agenciaId).update();
    }

    private CargaEnvio mapearEnvio(ResultSet rs, int fila) throws SQLException {
        return new CargaEnvio(rs.getLong("id"), rs.getString("codigo_envio"),
                rs.getLong("agencia_destino_id"), rs.getString("destino"), rs.getBigDecimal("peso_kg"),
                rs.getBigDecimal("alto_cm"), rs.getBigDecimal("ancho_cm"), rs.getBigDecimal("largo_cm"));
    }

    private ViajeResponse mapearViaje(ResultSet rs, int fila) throws SQLException {
        return new ViajeResponse(rs.getLong("id"), rs.getLong("camion_id"), rs.getString("placa"),
                rs.getLong("agencia_origen_id"), rs.getString("origen"),
                rs.getLong("agencia_destino_id"), rs.getString("destino"),
                EstadoViaje.valueOf(rs.getString("estado")), rs.getBigDecimal("peso_maximo_kg"),
                rs.getBigDecimal("volumen_maximo_m"), rs.getBigDecimal("peso"), rs.getBigDecimal("volumen"),
                rs.getLong("cantidad"), rs.getBoolean("sin_medidas"),
                Filas.fecha(rs, "fecha_salida"), Filas.fecha(rs, "fecha_llegada"));
    }

    public record CamionDisponible(long id, String placa, BigDecimal pesoMaximoKg, BigDecimal volumenMaximoM) {
    }
}
