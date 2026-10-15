package enviosrutasdelinca.model;

public class Camion {

    private Long id;
    private String placa;
    private Double pesoMaximoKg;
    private Double volumenMaximoM3;
    private Long agenciaBaseId;
    private Long agenciaActualId;
    private String estado;
    private Boolean activo;

    public Camion() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public Double getPesoMaximoKg() {
        return pesoMaximoKg;
    }

    public void setPesoMaximoKg(Double pesoMaximoKg) {
        this.pesoMaximoKg = pesoMaximoKg;
    }

    public Double getVolumenMaximoM3() {
        return volumenMaximoM3;
    }

    public void setVolumenMaximoM3(Double volumenMaximoM3) {
        this.volumenMaximoM3 = volumenMaximoM3;
    }

    public Long getAgenciaBaseId() {
        return agenciaBaseId;
    }

    public void setAgenciaBaseId(Long agenciaBaseId) {
        this.agenciaBaseId = agenciaBaseId;
    }

    public Long getAgenciaActualId() {
        return agenciaActualId;
    }

    public void setAgenciaActualId(Long agenciaActualId) {
        this.agenciaActualId = agenciaActualId;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    
}