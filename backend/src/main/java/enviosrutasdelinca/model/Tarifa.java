package enviosrutasdelinca.model;

public class Tarifa {

    private Long id;
    private Long agenciaOrigenId;
    private Long agenciaDestinoId;
    private Double rangoPesoMinKg;
    private Double rangoPesoMaxKg;
    private Double precioBase;
    private Double recargoDomicilio;
    private Boolean activo;

    public Tarifa() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Double getRangoPesoMinKg() {
        return rangoPesoMinKg;
    }

    public void setRangoPesoMinKg(Double rangoPesoMinKg) {
        this.rangoPesoMinKg = rangoPesoMinKg;
    }

    public Double getRangoPesoMaxKg() {
        return rangoPesoMaxKg;
    }

    public void setRangoPesoMaxKg(Double rangoPesoMaxKg) {
        this.rangoPesoMaxKg = rangoPesoMaxKg;
    }

    public Double getPrecioBase() {
        return precioBase;
    }

    public void setPrecioBase(Double precioBase) {
        this.precioBase = precioBase;
    }

    public Double getRecargoDomicilio() {
        return recargoDomicilio;
    }

    public void setRecargoDomicilio(Double recargoDomicilio) {
        this.recargoDomicilio = recargoDomicilio;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    
}