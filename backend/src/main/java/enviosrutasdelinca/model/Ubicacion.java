package enviosrutasdelinca.model;

public class Ubicacion {

    private Long id;
    private String direccion;
    private String ciudad;
    private String departamento;

    public Ubicacion() {
    }

    public Ubicacion(Long id, String direccion, String ciudad, String departamento) {
        this.id = id;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.departamento = departamento;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }
}