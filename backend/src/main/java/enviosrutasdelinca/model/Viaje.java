package enviosrutasdelinca.model;

import java.time.LocalDateTime;

public class Viaje {

    private Long id;
    private Long camionId;
    private Long agenciaOrigenId;
    private Long agenciaDestinoId;
    private String estado;
    private LocalDateTime fechaSalida;
    private LocalDateTime fechaLlegada;
    private Long encargadoSalidaId;
    private Long encargadoLlegadaId;
    private LocalDateTime fechaCreacion;

    public Viaje() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCamionId() {
        return camionId;
    }

    public void setCamionId(Long camionId) {
        this.camionId = camionId;
    }

    public Long getAgenciaOrigenId() {
        return agenciaOrigenId;
    }

    public void setAgenciaOrigenId(Long agenciaOrigenId) {
        this.agenciaOrigenId = agenciaOrigenId;
    }

    public Long getAgenciaDestinoId() {
        return agenciaDestinoId;
    }

    public void setAgenciaDestinoId(Long agenciaDestinoId) {
        this.agenciaDestinoId = agenciaDestinoId;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(LocalDateTime fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public LocalDateTime getFechaLlegada() {
        return fechaLlegada;
    }

    public void setFechaLlegada(LocalDateTime fechaLlegada) {
        this.fechaLlegada = fechaLlegada;
    }

    public Long getEncargadoSalidaId() {
        return encargadoSalidaId;
    }

    public void setEncargadoSalidaId(Long encargadoSalidaId) {
        this.encargadoSalidaId = encargadoSalidaId;
    }

    public Long getEncargadoLlegadaId() {
        return encargadoLlegadaId;
    }

    public void setEncargadoLlegadaId(Long encargadoLlegadaId) {
        this.encargadoLlegadaId = encargadoLlegadaId;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    
}