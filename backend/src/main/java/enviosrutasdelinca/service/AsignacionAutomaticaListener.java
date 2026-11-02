package enviosrutasdelinca.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import enviosrutasdelinca.shared.model.AsignacionSolicitada;

@Component
public class AsignacionAutomaticaListener {

    private static final Logger LOG = LoggerFactory.getLogger(AsignacionAutomaticaListener.class);
    private final AsignacionViajeService asignacion;

    public AsignacionAutomaticaListener(AsignacionViajeService asignacion) {
        this.asignacion = asignacion;
    }

    @TransactionalEventListener
    public void alLiberarCapacidad(AsignacionSolicitada evento) {
        try {
            asignacion.asignar(evento.agenciaId(), evento.usuarioId());
        } catch (RuntimeException error) {
            LOG.error("No se pudo asignar los pendientes de la sede {}. Se conserva el registro original.",
                    evento.agenciaId(), error);
        }
    }
}
