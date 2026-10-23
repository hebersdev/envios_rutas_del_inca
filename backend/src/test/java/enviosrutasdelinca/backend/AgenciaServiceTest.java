package enviosrutasdelinca.backend;

import enviosrutasdelinca.dto.AgenciaRequest;
import enviosrutasdelinca.dto.AgenciaResponse;
import enviosrutasdelinca.repository.AgenciaRepository;
import enviosrutasdelinca.repository.UbicacionRepository;
import enviosrutasdelinca.service.AgenciaService;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AgenciaServiceTest {

    private final AgenciaRepository agenciaRepository = mock(AgenciaRepository.class);
    private final UbicacionRepository ubicacionRepository = mock(UbicacionRepository.class);
    private final AgenciaService agenciaService =
            new AgenciaService(agenciaRepository, ubicacionRepository);

    @Test
    void noDesactivaUnaSedeConUsuariosActivos() {
        when(agenciaRepository.buscarPorId(7))
                .thenReturn(Optional.of(new AgenciaResponse(
                        7L, "Agencia Lima", "Av. Central 100", "Lima", "Lima", true
                )));
        when(agenciaRepository.contarUsuariosActivos(7)).thenReturn(1L);

        assertThrows(IllegalStateException.class, () -> agenciaService.cambiarEstado(7, false));

        verify(agenciaRepository, never()).cambiarEstado(7, false);
    }

    @Test
    void noCreaUnaSedeConNombreDuplicado() {
        when(agenciaRepository.existeNombre("Agencia Lima", null)).thenReturn(true);
        AgenciaRequest solicitud = new AgenciaRequest(
                "Agencia Lima", "Av. Central 100", "Lima", "Lima"
        );

        assertThrows(IllegalStateException.class, () -> agenciaService.crear(solicitud));

        verify(ubicacionRepository, never()).buscarId("Av. Central 100", "Lima");
    }
}
