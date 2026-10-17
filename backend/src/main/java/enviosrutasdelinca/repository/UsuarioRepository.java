package enviosrutasdelinca.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import enviosrutasdelinca.model.Usuario;

@Repository
public class UsuarioRepository {

    private final JdbcTemplate jdbcTemplate;

    public UsuarioRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean existePorCorreo(String correo) {

        String sql = """
                SELECT COUNT(*)
                FROM usuarios
                WHERE correo = ?
                """;

        Integer cantidad = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                correo
        );

        return cantidad != null && cantidad > 0;
    }

        public boolean existePorDni(String dni) {
                Integer cantidad = jdbcTemplate.queryForObject(
                                "SELECT COUNT(*) FROM usuarios WHERE dni = ?",
                                Integer.class,
                                dni
                );

                return cantidad != null && cantidad > 0;
        }

        public boolean existePorRuc(String ruc) {
                Integer cantidad = jdbcTemplate.queryForObject(
                                "SELECT COUNT(*) FROM usuarios WHERE ruc = ?",
                                Integer.class,
                                ruc
                );

                return cantidad != null && cantidad > 0;
        }

    public void guardar(Usuario usuario) {

        String sql = """
                INSERT INTO usuarios
                                (nombre, correo, clave, telefono, dni, ruc, rol, agencia_id, activo)
                                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                sql,
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getClave(),
                usuario.getTelefono(),
                usuario.getDni(),
                usuario.getRuc(),
                usuario.getRol(),
                usuario.getAgenciaId(),
                usuario.getActivo()
        );
    }

        public Optional<Usuario> buscarPorCorreo(String correo) {

        String sql = """
                SELECT id, nombre, correo, clave, telefono,
                       dni, ruc, rol, agencia_id, activo, fecha_registro
                FROM usuarios
                WHERE correo = ?
                """;

        List<Usuario> usuarios = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    Usuario usuario = new Usuario();

                    usuario.setId(rs.getLong("id"));
                    usuario.setNombre(rs.getString("nombre"));
                    usuario.setCorreo(rs.getString("correo"));
                    usuario.setClave(rs.getString("clave"));
                    usuario.setTelefono(rs.getString("telefono"));
                    usuario.setDni(rs.getString("dni"));
                    usuario.setRuc(rs.getString("ruc"));
                    usuario.setRol(rs.getString("rol"));

                    Long agenciaId = rs.getObject(
                            "agencia_id", Long.class
                    );
                    usuario.setAgenciaId(agenciaId);

                    usuario.setActivo(rs.getBoolean("activo"));

                    java.sql.Timestamp fecha =
                            rs.getTimestamp("fecha_registro");

                    if (fecha != null) {
                        usuario.setFechaRegistro(
                                fecha.toLocalDateTime()
                        );
                    }

                    return usuario;
                },
                correo
        );

        return usuarios.stream().findFirst();
    }
}