package enviosrutasdelinca.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class ReporteRepository {

    private final JdbcClient jdbc;

    public ReporteRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public String nombreSede(long sedeId) {
        return jdbc.sql("SELECT nombre FROM agencias WHERE id = :sede")
                .param("sede", sedeId).query(String.class).single();
    }

    public List<Object[]> envios(long sedeId, LocalDateTime desde, LocalDateTime hasta) {
        return jdbc.sql("""
                SELECT e.codigo_envio, e.fecha_registro::date AS dia, e.estado,
                       e.modalidad, destino.nombre AS destino
                FROM envios e
                JOIN agencias destino ON destino.id = e.agencia_destino_id
                WHERE e.agencia_origen_id = :sede
                  AND e.fecha_registro >= :desde AND e.fecha_registro < :hasta
                ORDER BY e.fecha_registro, e.id
                """)
                .param("sede", sedeId).param("desde", desde).param("hasta", hasta)
                .query((rs, fila) -> new Object[] {
                        rs.getString("codigo_envio"), rs.getObject("dia", LocalDate.class),
                        rs.getString("estado"), rs.getString("modalidad"), rs.getString("destino")
                }).list();
    }

    public List<Object[]> ingresos(long sedeId, LocalDateTime desde, LocalDateTime hasta) {
        return jdbc.sql("""
                SELECT e.codigo_envio, e.fecha_registro::date AS dia,
                       date_trunc('month', e.fecha_registro)::date AS mes,
                       e.modalidad, destino.nombre AS destino, e.costo_envio
                FROM envios e
                JOIN agencias destino ON destino.id = e.agencia_destino_id
                WHERE e.agencia_origen_id = :sede
                  AND e.fecha_registro >= :desde AND e.fecha_registro < :hasta
                ORDER BY e.fecha_registro, e.id
                """)
                .param("sede", sedeId).param("desde", desde).param("hasta", hasta)
                .query((rs, fila) -> new Object[] {
                        rs.getString("codigo_envio"), rs.getObject("dia", LocalDate.class),
                        rs.getObject("mes", LocalDate.class), rs.getString("modalidad"),
                        rs.getString("destino"), rs.getBigDecimal("costo_envio")
                }).list();
    }

    public List<Object[]> viajes(long sedeId, LocalDateTime desde, LocalDateTime hasta) {
        return jdbc.sql("""
                SELECT e.codigo_envio, v.id AS viaje_id, c.placa,
                       origen.nombre AS origen, destino.nombre AS destino, v.estado,
                       v.fecha_salida, v.fecha_llegada, e.peso_kg, c.peso_maximo_kg,
                       CASE WHEN e.alto_cm IS NOT NULL AND e.ancho_cm IS NOT NULL
                                  AND e.largo_cm IS NOT NULL
                            THEN e.alto_cm * e.ancho_cm * e.largo_cm / 1000000
                       END AS volumen_m3, c.volumen_maximo_m
                FROM viajes v
                JOIN camiones c ON c.id = v.camion_id
                JOIN agencias origen ON origen.id = v.agencia_origen_id
                JOIN agencias destino ON destino.id = v.agencia_destino_id
                JOIN envios e ON e.viaje_id = v.id
                WHERE (v.agencia_origen_id = :sede OR v.agencia_destino_id = :sede)
                  AND v.fecha_creacion >= :desde AND v.fecha_creacion < :hasta
                ORDER BY v.fecha_creacion, v.id, e.codigo_envio
                """)
                .param("sede", sedeId).param("desde", desde).param("hasta", hasta)
                .query((rs, fila) -> new Object[] {
                        rs.getString("codigo_envio"), rs.getLong("viaje_id"),
                        rs.getString("placa"), rs.getString("origen"), rs.getString("destino"),
                        rs.getString("estado"), rs.getObject("fecha_salida", LocalDateTime.class),
                        rs.getObject("fecha_llegada", LocalDateTime.class),
                        rs.getBigDecimal("peso_kg"), rs.getBigDecimal("peso_maximo_kg"),
                        rs.getBigDecimal("volumen_m3"), rs.getBigDecimal("volumen_maximo_m")
                }).list();
    }

    public List<Object[]> entregas(long sedeId, LocalDateTime desde, LocalDateTime hasta) {
        return jdbc.sql("""
                SELECT e.codigo_envio, e.fecha_registro, e.fecha_entrega,
                       e.estado, e.modalidad, origen.nombre AS origen,
                       CASE WHEN e.fecha_entrega IS NOT NULL
                            THEN ROUND(EXTRACT(EPOCH FROM
                                (e.fecha_entrega - e.fecha_registro)) / 3600, 2)
                       END AS horas_entrega
                FROM envios e
                JOIN agencias origen ON origen.id = e.agencia_origen_id
                WHERE e.agencia_destino_id = :sede
                  AND e.fecha_registro >= :desde AND e.fecha_registro < :hasta
                ORDER BY e.fecha_registro, e.id
                """)
                .param("sede", sedeId).param("desde", desde).param("hasta", hasta)
                .query((rs, fila) -> new Object[] {
                        rs.getString("codigo_envio"),
                        rs.getObject("fecha_registro", LocalDateTime.class),
                        rs.getObject("fecha_entrega", LocalDateTime.class),
                        rs.getString("estado"), rs.getString("modalidad"),
                        rs.getString("origen"), rs.getBigDecimal("horas_entrega")
                }).list();
    }

    public List<Object[]> motorizados(long sedeId, LocalDateTime desde, LocalDateTime hasta) {
        return jdbc.sql("""
                SELECT e.codigo_envio, u.id AS motorizado_id, u.nombre AS motorizado,
                       e.fecha_entrega
                FROM envios e
                JOIN usuarios u ON u.id = e.motorizado_id
                WHERE e.agencia_destino_id = :sede AND e.modalidad = 'DOMICILIO'
                  AND e.estado = 'ENTREGADO'
                  AND e.fecha_entrega >= :desde AND e.fecha_entrega < :hasta
                ORDER BY e.fecha_entrega, e.codigo_envio
                """)
                .param("sede", sedeId).param("desde", desde).param("hasta", hasta)
                .query((rs, fila) -> new Object[] {
                        rs.getString("codigo_envio"), rs.getLong("motorizado_id"),
                        rs.getString("motorizado"),
                        rs.getObject("fecha_entrega", LocalDateTime.class)
                }).list();
    }
}
