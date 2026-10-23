package enviosrutasdelinca.backend;

import enviosrutasdelinca.dto.TarifaRequest;
import enviosrutasdelinca.dto.TarifaResponse;
import enviosrutasdelinca.repository.TarifaRepository;
import enviosrutasdelinca.service.TarifaService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TarifaServiceTest {

    private final TarifaRepository repositorio = mock(TarifaRepository.class);
    private final TarifaService servicio = new TarifaService(repositorio);

    @Test
    void rechazaRangoDondeElMaximoNoSuperaAlMinimo() {
        TarifaRequest solicitud = solicitud("5.00", "5.00");

        assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> servicio.crear(solicitud)
        );

        verify(repositorio, never()).crear(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rechazaRangosActivosQueSeCruzan() {
        TarifaRequest solicitud = solicitud("4.00", "8.00");
        when(repositorio.existeCruce(
                new BigDecimal("4.00"), new BigDecimal("8.00"), 0))
                .thenReturn(true);

        assertThrows(IllegalStateException.class, () -> servicio.crear(solicitud));

        verify(repositorio, never()).crear(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void permiteRangosQueSoloSeTocanEnElLimite() {
        TarifaRequest solicitud = solicitud("5.00", "20.00");
        TarifaResponse creada = tarifaResponse();
        when(repositorio.existeCruce(
                new BigDecimal("5.00"), new BigDecimal("20.00"), 0))
                .thenReturn(false);
        when(repositorio.crear(org.mockito.ArgumentMatchers.any())).thenReturn(9L);
        when(repositorio.buscarPorId(9)).thenReturn(Optional.of(creada));

        servicio.crear(solicitud);

        verify(repositorio).crear(org.mockito.ArgumentMatchers.any());
    }

    private TarifaRequest solicitud(String min, String max) {
        return new TarifaRequest(
                new BigDecimal(min),
                new BigDecimal(max),
                new BigDecimal("15.00"),
                BigDecimal.ZERO
        );
    }

    private TarifaResponse tarifaResponse() {
        return new TarifaResponse(
                9L, new BigDecimal("5.00"), new BigDecimal("20.00"),
                new BigDecimal("15.00"), BigDecimal.ZERO, true
        );
    }
}
