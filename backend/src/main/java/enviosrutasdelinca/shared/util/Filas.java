package enviosrutasdelinca.shared.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public final class Filas {

    private Filas() {
    }

    public static LocalDateTime fecha(ResultSet rs, String columna) throws SQLException {
        return rs.getObject(columna, LocalDateTime.class);
    }
}
