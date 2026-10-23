package enviosrutasdelinca.repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import enviosrutasdelinca.model.Usuario;
import enviosrutasdelinca.dto.UsuarioAdministracionResponse;

@Repository
public class UsuarioRepository {

    private final JdbcTemplate jdbcTemplate;

    public UsuarioRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean existePorCorreo(String correo) {
        return existe(
                "SELECT COUNT(*) FROM usuarios WHERE correo = ?",
                correo
        );
    }

    public boolean existePorDni(String dni) {
        return existe(
                "SELECT COUNT(*) FROM usuarios WHERE dni = ?",
                dni
        );
    }

    public boolean existePorRuc(String ruc) {
        return existe(
                "SELECT COUNT(*) FROM usuarios WHERE ruc = ?",
                ruc
        );
    }

    public void guardar(Usuario usuario) {
        String sql = """
            INSERT INTO usuarios
                (nombre, correo, clave, telefono, dni, ruc,
                 rol, agencia_id, activo)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        jdbcTemplate.update(
                sql,
                usuario.nombre(),
                usuario.correo(),
                usuario.clave(),
                usuario.telefono(),
                usuario.dni(),
                usuario.ruc(),
                usuario.rol(),
                usuario.agenciaId(),
                usuario.activo()
        );
    }

    public List<UsuarioAdministracionResponse> listarPersonalActivo() {
        String sql = """
            SELECT u.id, u.nombre, u.correo, u.rol, u.agencia_id,
                   COALESCE(a.nombre, 'Sin sede') AS sede, u.activo
            FROM usuarios u
            LEFT JOIN agencias a ON a.id = u.agencia_id
            WHERE u.activo AND u.rol <> 'CLIENTE'
            ORDER BY LOWER(u.nombre), u.id
            """;

        return jdbcTemplate.query(sql, this::mapearUsuarioAdministracion);
    }

    public Optional<UsuarioAdministracionResponse> buscarPersonalActivo(long id) {
        String sql = """
            SELECT u.id, u.nombre, u.correo, u.rol, u.agencia_id,
                   COALESCE(a.nombre, 'Sin sede') AS sede, u.activo
            FROM usuarios u
            LEFT JOIN agencias a ON a.id = u.agencia_id
            WHERE u.id = ? AND u.activo AND u.rol <> 'CLIENTE'
            """;

        return jdbcTemplate.query(sql, this::mapearUsuarioAdministracion, id)
                .stream()
                .findFirst();
    }

    public long crearPersonal(String nombre, String correo, String clave, String rol, Long agenciaId) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO usuarios (nombre, correo, clave, rol, agencia_id, activo)
                VALUES (?, ?, ?, ?, ?, TRUE)
                RETURNING id
                """, Long.class, nombre, correo, clave, rol, agenciaId);
    }

    public int actualizarRol(long id, String rol, Long agenciaId) {
        return jdbcTemplate.update(
                "UPDATE usuarios SET rol = ?, agencia_id = ? WHERE id = ? AND activo = TRUE",
                rol,
                agenciaId,
                id
        );
    }

    public int desactivar(long id) {
        return jdbcTemplate.update(
                "UPDATE usuarios SET activo = FALSE WHERE id = ? AND activo = TRUE",
                id
        );
    }

    public long contarAdministradoresActivos() {
        Long cantidad = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM usuarios WHERE activo = TRUE AND rol = 'ADMINISTRADOR'",
                Long.class
        );
        return cantidad == null ? 0 : cantidad;
    }

    private UsuarioAdministracionResponse mapearUsuarioAdministracion(
            java.sql.ResultSet rs,
            int numeroFila) throws java.sql.SQLException {
        return new UsuarioAdministracionResponse(
                rs.getLong("id"),
                rs.getString("nombre"),
                rs.getString("correo"),
                rs.getObject("agencia_id", Long.class),
                rs.getString("sede"),
                rs.getString("rol"),
                rs.getBoolean("activo")
        );
    }

    public Optional<Usuario> buscarPorCorreo(String correo) {
        String sql = """
            SELECT id, nombre, correo, clave, telefono,
                   dni, ruc, rol, agencia_id, activo, fecha_registro
            FROM usuarios
            WHERE correo = ?
            """;

        return jdbcTemplate.query(sql, (rs, numeroFila) -> {
            Timestamp fecha = rs.getTimestamp("fecha_registro");

            return new Usuario(
                    rs.getLong("id"),
                    rs.getString("nombre"),
                    rs.getString("correo"),
                    rs.getString("clave"),
                    rs.getString("telefono"),
                    rs.getString("dni"),
                    rs.getString("ruc"),
                    rs.getString("rol"),
                    rs.getObject("agencia_id", Long.class),
                    rs.getBoolean("activo"),
                    fecha != null ? fecha.toLocalDateTime() : null
            );
        }, correo).stream().findFirst();
    }

    private boolean existe(String sql, String valor) {
        Integer cantidad = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                valor
        );

        return cantidad != null && cantidad > 0;
    }
}