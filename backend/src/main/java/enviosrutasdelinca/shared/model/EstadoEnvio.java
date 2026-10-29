package enviosrutasdelinca.shared.model;

public enum EstadoEnvio {
    REGISTRADO("Recibido en la agencia de origen"),
    ASIGNADO_A_VIAJE("Listo para salir"),
    EN_RUTA("En camino"),
    LLEGO_A_AGENCIA_DESTINO("Llegó a la agencia de destino"),
    LISTO_PARA_RECOJO("Listo para recoger en agencia"),
    EN_REPARTO("En reparto a domicilio"),
    ENTREGADO("Entregado");

    private final String textoCliente;

    EstadoEnvio(String textoCliente) {
        this.textoCliente = textoCliente;
    }

    public String textoCliente() {
        return textoCliente;
    }
}
