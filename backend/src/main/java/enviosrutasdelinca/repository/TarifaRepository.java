package enviosrutasdelinca.repository;

import java.math.BigDecimal;
import java.util.*;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import enviosrutasdelinca.dto.TarifaResponse;
import enviosrutasdelinca.model.Tarifa;

@Repository
public class TarifaRepository {

    private final JdbcClient jdbc;

    public TarifaRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<TarifaResponse> listar(Boolean activo) {
        return jdbc.sql("""
                        SELECT id, rango_peso_min_kg, rango_peso_max_kg, precio_base,
                               recargo_domicilio, activo
                        FROM tarifas
                        WHERE CAST(:activo AS BOOLEAN) IS NULL OR activo = :activo
                        ORDER BY rango_peso_min_kg
                        """)
                .param("activo", activo)
                .query(TarifaResponse.class)
                .list();
    }

    public Optional<TarifaResponse> buscarPorId(long id) {
        return jdbc.sql("""
                        SELECT id, rango_peso_min_kg, rango_peso_max_kg, precio_base,
                               recargo_domicilio, activo
                        FROM tarifas WHERE id = :id
                        """)
                .param("id", id)
                .query(TarifaResponse.class)
                .optional();
    }

    public Optional<Tarifa> buscarTarifaActiva(
            BigDecimal pesoKg) {
        return jdbc.sql("""
                        SELECT id, rango_peso_min_kg, rango_peso_max_kg,
                               precio_base, recargo_domicilio, activo
                        FROM tarifas
                        WHERE activo
                          AND :pesoKg > rango_peso_min_kg
                          AND :pesoKg <= rango_peso_max_kg
                        """)
                .param("pesoKg", pesoKg)
                .query(Tarifa.class)
                .optional();
    }

    public boolean rutaDisponible(long origenId, long destinoId) {
        return jdbc.sql("""
                        SELECT EXISTS (
                            SELECT 1 FROM agencias ao
                            JOIN agencias ad ON ad.id = :destinoId
                            WHERE ao.id = :origenId
                              AND ao.id <> ad.id
                              AND ao.activo AND ad.activo
                        )
                        """)
                .param("origenId", origenId)
                .param("destinoId", destinoId)
                .query(Boolean.class)
                .single();
    }

    public Optional<BigDecimal> pesoMaximoActivo() {
        return jdbc.sql("""
                        SELECT MAX(rango_peso_max_kg)
                        FROM tarifas
                        WHERE activo
                        """)
                .query(BigDecimal.class)
                .optional();
    }

    public long crear(TarifaRequestData datos) {
        return jdbc.sql("""
                        INSERT INTO tarifas (
                            rango_peso_min_kg, rango_peso_max_kg,
                            precio_base, recargo_domicilio
                        )
                        VALUES (
                            :minKg, :maxKg, :precio, :recargo
                        )
                        RETURNING id
                        """)
                .param("minKg", datos.minKg())
                .param("maxKg", datos.maxKg())
                .param("precio", datos.precio())
                .param("recargo", datos.recargo())
                .query(Long.class)
                .single();
    }

    public int editar(long id, TarifaRequestData datos) {
        return jdbc.sql("""
                        UPDATE tarifas
                        SET rango_peso_min_kg = :minKg,
                            rango_peso_max_kg = :maxKg,
                            precio_base = :precio,
                            recargo_domicilio = :recargo
                        WHERE id = :id
                        """)
                .param("id", id)
                .param("minKg", datos.minKg())
                .param("maxKg", datos.maxKg())
                .param("precio", datos.precio())
                .param("recargo", datos.recargo())
                .update();
    }

    public int cambiarEstado(long id, boolean activo) {
        return jdbc.sql("UPDATE tarifas SET activo = :activo WHERE id = :id")
                .param("id", id)
                .param("activo", activo)
                .update();
    }

    public Optional<Boolean> estaActiva(long id) {
        return jdbc.sql("SELECT activo FROM tarifas WHERE id = :id")
                .param("id", id)
                .query(Boolean.class)
                .optional();
    }

    public boolean existeCruce(
            BigDecimal minKg,
            BigDecimal maxKg,
            long idExcluido) {
        return jdbc.sql("""
                        SELECT EXISTS (
                            SELECT 1 FROM tarifas
                            WHERE activo
                              AND id <> :idExcluido
                              AND rango_peso_min_kg < :maxKg
                              AND rango_peso_max_kg > :minKg
                        )
                        """)
                .param("minKg", minKg)
                .param("maxKg", maxKg)
                .param("idExcluido", idExcluido)
                .query(Boolean.class)
                .single();
    }

    public record TarifaRequestData(
            BigDecimal minKg,
            BigDecimal maxKg,
            BigDecimal precio,
            BigDecimal recargo) {
    }
}
