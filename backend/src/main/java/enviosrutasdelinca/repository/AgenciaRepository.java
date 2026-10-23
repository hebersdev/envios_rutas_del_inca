package enviosrutasdelinca.repository;

import enviosrutasdelinca.dto.AgenciaResumenResponse;
import enviosrutasdelinca.dto.AgenciaResponse;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AgenciaRepository {

    private final JdbcClient jdbc;

    public AgenciaRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<AgenciaResumenResponse> listarActivas() {
        return jdbc.sql("""
                SELECT a.id, a.nombre, u.ciudad, u.departamento
                FROM agencias a
                JOIN ubicaciones u ON u.id = a.ubicacion_id
                WHERE a.activo
                ORDER BY u.departamento, u.ciudad, a.nombre
                """)
                .query(AgenciaResumenResponse.class)
                .list();
    }

    public List<AgenciaResponse> listar(Boolean activo) {
        return jdbc.sql("""
                SELECT a.id, a.nombre, u.direccion, u.ciudad, u.departamento, a.activo
                FROM agencias a
                JOIN ubicaciones u ON u.id = a.ubicacion_id
                WHERE (CAST(:activo AS BOOLEAN) IS NULL OR a.activo = :activo)
                ORDER BY u.departamento, u.ciudad, a.nombre
                """)
                .param("activo", activo)
                .query(AgenciaResponse.class)
                .list();
    }

    public Optional<AgenciaResponse> buscarPorId(long id) {
        try {
            return Optional.of(jdbc.sql("""
                    SELECT a.id, a.nombre, u.direccion, u.ciudad, u.departamento, a.activo
                    FROM agencias a
                    JOIN ubicaciones u ON u.id = a.ubicacion_id
                    WHERE a.id = :id
                    """)
                    .param("id", id)
                    .query(AgenciaResponse.class)
                    .single());
        } catch (EmptyResultDataAccessException error) {
            return Optional.empty();
        }
    }

    public Optional<Long> buscarUbicacionId(long id) {
        return jdbc.sql("SELECT ubicacion_id FROM agencias WHERE id = :id")
                .param("id", id)
                .query(Long.class)
                .optional();
    }

    public boolean existeNombre(String nombre, Long idExcluido) {
        Integer cantidad = jdbc.sql("""
                SELECT COUNT(*)
                FROM agencias
                WHERE LOWER(nombre) = LOWER(:nombre)
                  AND (CAST(:idExcluido AS BIGINT) IS NULL OR id <> :idExcluido)
                """)
                .param("nombre", nombre)
                .param("idExcluido", idExcluido)
                .query(Integer.class)
                .single();
        return cantidad > 0;
    }

    public boolean existeAgenciaActivaEnUbicacion(long ubicacionId, Long idExcluido) {
        Integer cantidad = jdbc.sql("""
                SELECT COUNT(*)
                FROM agencias
                WHERE ubicacion_id = :ubicacionId
                  AND activo
                  AND (CAST(:idExcluido AS BIGINT) IS NULL OR id <> :idExcluido)
                """)
                .param("ubicacionId", ubicacionId)
                .param("idExcluido", idExcluido)
                .query(Integer.class)
                .single();
        return cantidad > 0;
    }

    public long crear(String nombre, long ubicacionId) {
        return jdbc.sql("""
                INSERT INTO agencias (nombre, ubicacion_id)
                VALUES (:nombre, :ubicacionId)
                RETURNING id
                """)
                .param("nombre", nombre)
                .param("ubicacionId", ubicacionId)
                .query(Long.class)
                .single();
    }

    public void editar(long id, String nombre, long ubicacionId) {
        jdbc.sql("""
                UPDATE agencias
                SET nombre = :nombre, ubicacion_id = :ubicacionId
                WHERE id = :id
                """)
                .param("id", id)
                .param("nombre", nombre)
                .param("ubicacionId", ubicacionId)
                .update();
    }

    public void cambiarEstado(long id, boolean activo) {
        jdbc.sql("UPDATE agencias SET activo = :activo WHERE id = :id")
                .param("id", id)
                .param("activo", activo)
                .update();
    }

    public long contarUsuariosActivos(long id) {
        return jdbc.sql("""
                SELECT COUNT(*)
                FROM usuarios
                WHERE agencia_id = :id AND activo
                """)
                .param("id", id)
                .query(Long.class)
                .single();
    }

    public long contarCamionesActivos(long id) {
        return jdbc.sql("""
                SELECT COUNT(*)
                FROM camiones
                WHERE (agencia_base_id = :id OR agencia_actual_id = :id)
                  AND activo
                """)
                .param("id", id)
                .query(Long.class)
                .single();
    }
}