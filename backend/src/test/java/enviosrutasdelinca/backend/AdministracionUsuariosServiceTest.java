package enviosrutasdelinca.backend;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import enviosrutasdelinca.dto.ActualizarRolUsuarioRequest;
import enviosrutasdelinca.dto.AgenciaResponse;
import enviosrutasdelinca.dto.UsuarioAdministracionRequest;
import enviosrutasdelinca.dto.UsuarioAdministracionResponse;
import enviosrutasdelinca.repository.AgenciaRepository;
import enviosrutasdelinca.repository.UsuarioRepository;
import enviosrutasdelinca.service.AdministracionUsuariosService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdministracionUsuariosServiceTest {

    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final AgenciaRepository agenciaRepository = mock(AgenciaRepository.class);
    private final AdministracionUsuariosService servicio =
            new AdministracionUsuariosService(usuarioRepository, agenciaRepository);

    @Test
    void noCreaUsuarioOperativoEnSedeInactiva() {
        when(usuarioRepository.existePorCorreo("counter@example.com")).thenReturn(false);
        when(agenciaRepository.buscarPorId(4)).thenReturn(Optional.of(
                new AgenciaResponse(4L, "Sede cerrada", "Av. Central 10", "Lima", "Lima", false)
        ));
        UsuarioAdministracionRequest solicitud = new UsuarioAdministracionRequest(
                "Counter", "counter@example.com", "clave-segura-123", "COUNTER", 4L
        );

        assertThrows(IllegalArgumentException.class, () -> servicio.crear(solicitud));

        verify(usuarioRepository, never()).crearPersonal(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void noPermiteDegradarAlUltimoAdministrador() {
        when(usuarioRepository.buscarPorCorreo("admin@example.com"))
                .thenReturn(Optional.empty());
        when(usuarioRepository.buscarPersonalActivo(1)).thenReturn(Optional.of(
                new UsuarioAdministracionResponse(
                        1L, "Admin", "admin@example.com", null, "Sin sede", "ADMINISTRADOR", true
                )
        ));
        when(usuarioRepository.contarAdministradoresActivos()).thenReturn(1L);

        assertThrows(IllegalStateException.class, () -> servicio.actualizarRol(
                1,
                new ActualizarRolUsuarioRequest("COUNTER", 4L),
                "otro-admin@example.com"
        ));

        verify(usuarioRepository, never()).actualizarRol(1, "COUNTER", 4L);
    }

    @Test
    void noPermiteDesactivarLaPropiaCuenta() {
        when(usuarioRepository.buscarPersonalActivo(1)).thenReturn(Optional.of(
                new UsuarioAdministracionResponse(
                        1L, "Admin", "admin@example.com", null, "Sin sede", "ADMINISTRADOR", true
                )
        ));

        assertThrows(IllegalStateException.class, () -> servicio.desactivar(1, "admin@example.com"));

        verify(usuarioRepository, never()).desactivar(1);
    }
}