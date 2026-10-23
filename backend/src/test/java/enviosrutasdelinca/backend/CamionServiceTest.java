package enviosrutasdelinca.backend;

import enviosrutasdelinca.model.Usuario;
import enviosrutasdelinca.repository.AgenciaRepository;
import enviosrutasdelinca.repository.CamionRepository;
import enviosrutasdelinca.repository.UsuarioRepository;
import enviosrutasdelinca.service.CamionService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CamionServiceTest {

    private final CamionRepository camionRepository = mock(CamionRepository.class);
    private final AgenciaRepository agenciaRepository = mock(AgenciaRepository.class);
    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final CamionService servicio = new CamionService(
            camionRepository, agenciaRepository, usuarioRepository);

    @Test
    void limitaElListadoDelEncargadoASuSedeAunqueSoliciteOtra() {
        when(usuarioRepository.buscarPorCorreo("logistica@ejemplo.com"))
                .thenReturn(Optional.of(usuarioLogistica()));
        when(camionRepository.listar("DISPONIBLE", 3L, true))
                .thenReturn(List.of());

        servicio.listar("DISPONIBLE", 9L, true, "logistica@ejemplo.com");

        verify(camionRepository).listar("DISPONIBLE", 3L, true);
        verify(camionRepository, never()).listar("DISPONIBLE", 9L, true);
    }

    @Test
    void noPermiteCambiarElEstadoDeUnCamionEnViaje() {
        when(camionRepository.buscarEstado(7)).thenReturn(Optional.of("EN_VIAJE"));
        when(camionRepository.agenciaActual(7)).thenReturn(Optional.of(3L));
        when(camionRepository.buscarPorId(7)).thenReturn(Optional.of(
                new enviosrutasdelinca.dto.CamionResponse(
                        7L, "ABC-123", null, null,
                        3L, "Base", 3L, "Actual", "EN_VIAJE", true)));
        when(usuarioRepository.buscarPorCorreo("logistica@ejemplo.com"))
                .thenReturn(Optional.of(usuarioLogistica()));

        assertThrows(
                IllegalStateException.class,
                () -> servicio.cambiarEstado(
                        7, "DISPONIBLE", "logistica@ejemplo.com")
        );

        verify(camionRepository, never()).cambiarEstado(7, "DISPONIBLE");
    }

    private Usuario usuarioLogistica() {
        return new Usuario(
                5L, "Encargado", "logistica@ejemplo.com", "hash",
                null, null, null, "ENCARGADO_LOGISTICA", 3L,
                true, null
        );
    }
}
