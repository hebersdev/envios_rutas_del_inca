package enviosrutasdelinca.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import enviosrutasdelinca.dto.CamionResponse;

@Repository
public class CamionRepository {

    private static final String SELECT_CAMIONES = """
            SELECT c.id, c.placa, c.peso_maximo_kg, c.volumen_maximo_m3,
                   c.agencia_base_id, ab.nombre AS agencia_base_nombre,
                   c.agencia_actual_id, aa.nombre AS agencia_actual_nombre,
                   c.estado, c.activo
            FROM camiones c
            JOIN agencias ab ON ab.id = c.agencia_base_id
            JOIN agencias aa ON aa.id = c.agencia_actual_id
            """;

    private final JdbcClient jdbc;

    public CamionRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<CamionResponse> listar(String estado, Long agenciaActualId, Boolean activo) {
        return jdbc.sql(SELECT_CAMIONES + """
                        WHERE (CAST(:estado AS VARCHAR) IS NULL OR c.estado = :estado)
                          AND (CAST(:agenciaActualId AS BIGINT) IS NULL
                               OR c.agencia_actual_id = :agenciaActualId)
                          AND (CAST(:activo AS BOOLEAN) IS NULL OR c.activo = :activo)
                        ORDER BY c.placa
                        """)
                .param("estado", estado)
                .param("agenciaActualId", agenciaActualId)
                .param("activo", activo)
                .query(this::mapear)
                .list();
    }

    public Optional<CamionResponse> buscarPorId(long id) {
        return jdbc.sql(SELECT_CAMIONES + " WHERE c.id = :id")
                .param("id", id)
                .query(this::mapear)
                .optional();
    }

    public Optional<String> buscarEstado(long id) {
        return jdbc.sql("SELECT estado FROM camiones WHERE id = :id")
                .param("id", id)
                .query(String.class)
                .optional();
    }

    public boolean existePlaca(String placa, Long idExcluido) {
        return jdbc.sql("""
                        SELECT EXISTS (
                            SELECT 1 FROM camiones
                            WHERE UPPER(placa) = UPPER(:placa)
                              AND (CAST(:idExcluido AS BIGINT) IS NULL OR id <> :idExcluido)
                        )
                        """)
                .param("placa", placa)
                .param("idExcluido", idExcluido)
                .query(Boolean.class)
                .single();
    }

    public Optional<Long> agenciaActual(long id) {
        return jdbc.sql("SELECT agencia_actual_id FROM camiones WHERE id = :id")
                .param("id", id)
                .query(Long.class)
                .optional();
    }

    public long crear(String placa, java.math.BigDecimal peso, java.math.BigDecimal volumen, long agenciaBaseId) {
        return jdbc.sql("""
                        INSERT INTO camiones (
                            placa, peso_maximo_kg, volumen_maximo_m3,
                            agencia_base_id, agencia_actual_id
                        )
                        VALUES (:placa, :peso, :volumen, :agenciaBaseId, :agenciaBaseId)
                        RETURNING id
                        """)
                .param("placa", placa)
                .param("peso", peso)
                .param("volumen", volumen)
                .param("agenciaBaseId", agenciaBaseId)
                .query(Long.class)
                .single();
    }

    public int editar(long id, String placa, java.math.BigDecimal peso,
            java.math.BigDecimal volumen, long agenciaBaseId) {
        return jdbc.sql("""
                        UPDATE camiones
                        SET placa = :placa,
                            peso_maximo_kg = :peso,
                            volumen_maximo_m3 = :volumen,
                            agencia_base_id = :agenciaBaseId
                        WHERE id = :id
                        """)
                .param("id", id)
                .param("placa", placa)
                .param("peso", peso)
                .param("volumen", volumen)
                .param("agenciaBaseId", agenciaBaseId)
                .update();
    }

    public int cambiarEstado(long id, String estado) {
        return jdbc.sql("UPDATE camiones SET estado = :estado WHERE id = :id")
                .param("id", id)
                .param("estado", estado)
                .update();
    }

    public int cambiarActivo(long id, boolean activo) {
        return jdbc.sql("UPDATE camiones SET activo = :activo WHERE id = :id")
                .param("id", id)
                .param("activo", activo)
                .update();
    }

    private CamionResponse mapear(ResultSet rs, int row) throws SQLException {
        return new CamionResponse(
                rs.getLong("id"),
                rs.getString("placa"),
                rs.getBigDecimal("peso_maximo_kg"),
                rs.getBigDecimal("volumen_maximo_m3"),
                rs.getLong("agencia_base_id"),
                rs.getString("agencia_base_nombre"),
                rs.getLong("agencia_actual_id"),
                rs.getString("agencia_actual_nombre"),
                rs.getString("estado"),
                rs.getBoolean("activo")
        );
    }
}
