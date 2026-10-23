package enviosrutasdelinca.model;

import java.time.LocalDateTime;

public class Agencia {

    private Long id;
    private String nombre;
    private Long ubicacionId;
    private Boolean activo;
    private LocalDateTime fechaCreacion;

    public Agencia() {
    }

    public Agencia(Long id, String nombre, Long ubicacionId, Boolean activo, LocalDateTime fechaCreacion) {
        this.id = id;
        this.nombre = nombre;
        this.ubicacionId = ubicacionId;
        this.activo = activo;
        this.fechaCreacion = fechaCreacion;
    }

    // getters y setters
}
