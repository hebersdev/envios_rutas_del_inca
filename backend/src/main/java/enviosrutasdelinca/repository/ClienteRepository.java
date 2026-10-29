package enviosrutasdelinca.repository;

import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import enviosrutasdelinca.dto.ClienteBusquedaResponse;

@Repository
public class ClienteRepository {

    private final JdbcClient jdbc;

    public ClienteRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<ClienteBusquedaResponse> buscarPorDocumento(String documento) {
        return jdbc.sql("""
                        SELECT id, nombre, correo, telefono, COALESCE(dni, ruc) AS documento
                        FROM usuarios
                        WHERE (dni = :documento OR ruc = :documento)
                          AND rol = 'CLIENTE' AND activo
                        """)
                .param("documento", documento)
                .query(ClienteBusquedaResponse.class)
                .optional();
    }

    public boolean existeClienteActivo(long id) {
        return jdbc.sql("""
                        SELECT EXISTS (
                            SELECT 1 FROM usuarios
                            WHERE id = :id AND rol = 'CLIENTE' AND activo
                        )
                        """)
                .param("id", id)
                .query(Boolean.class)
                .single();
    }
}
