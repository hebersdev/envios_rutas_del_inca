package enviosrutasdelinca.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UbicacionRepository {

    private final JdbcClient jdbc;

    public UbicacionRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Long> buscarId(String direccion, String ciudad) {
        return jdbc.sql("""
                SELECT id
                FROM ubicaciones
                WHERE direccion = :direccion AND ciudad = :ciudad
                """)
                .param("direccion", direccion)
                .param("ciudad", ciudad)
                .query(Long.class)
                .optional();
    }

    public long crear(String direccion, String ciudad, String departamento) {
        return jdbc.sql("""
                INSERT INTO ubicaciones (direccion, ciudad, departamento)
                VALUES (:direccion, :ciudad, :departamento)
                ON CONFLICT (direccion, ciudad)
                DO UPDATE SET direccion = EXCLUDED.direccion
                RETURNING id
                """)
                .param("direccion", direccion)
                .param("ciudad", ciudad)
                .param("departamento", departamento)
                .query(Long.class)
                .single();
    }
}
