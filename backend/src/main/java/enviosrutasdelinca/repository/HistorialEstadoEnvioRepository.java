package enviosrutasdelinca.repository;

import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import enviosrutasdelinca.dto.EventoSeguimientoResponse;
import enviosrutasdelinca.shared.model.EstadoEnvio;
import enviosrutasdelinca.shared.util.Filas;

@Repository
public class HistorialEstadoEnvioRepository {

    private final JdbcClient jdbc;

    public HistorialEstadoEnvioRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public void registrar(long envioId, EstadoEnvio estado, long usuarioId, String comentario) {
        jdbc.sql("""
                        INSERT INTO historial_estados_envio (envio_id, estado, usuario_id, comentario)
                        VALUES (:envioId, :estado, :usuarioId, :comentario)
                        """)
                .param("envioId", envioId)
                .param("estado", estado.name())
                .param("usuarioId", usuarioId)
                .param("comentario", comentario)
                .update();
    }

    public List<EventoSeguimientoResponse> listarEventos(long envioId) {
        return jdbc.sql("""
                        SELECT h.estado, h.fecha, a.nombre AS sede
                        FROM historial_estados_envio h
                        JOIN usuarios u ON u.id = h.usuario_id
                        LEFT JOIN agencias a ON a.id = u.agencia_id
                        WHERE h.envio_id = :envioId
                        ORDER BY h.fecha, h.id
                        """)
                .param("envioId", envioId)
                .query((rs, fila) -> {
                    EstadoEnvio estado = EstadoEnvio.valueOf(rs.getString("estado"));
                    return new EventoSeguimientoResponse(
                            estado,
                            estado.textoCliente(),
                            Filas.fecha(rs, "fecha"),
                            rs.getString("sede"));
                })
                .list();
    }
}
