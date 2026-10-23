package enviosrutasdelinca.backend;

import enviosrutasdelinca.model.Tarifa;
import enviosrutasdelinca.repository.TarifaRepository;
import enviosrutasdelinca.service.CotizacionService;
import enviosrutasdelinca.shared.model.ModalidadEnvio;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CotizacionServiceTest {

    private final TarifaRepository repositorio = mock(TarifaRepository.class);
    private final CotizacionService servicio = new CotizacionService(repositorio);

    @Test
    void calculaRecargoSoloParaEntregaADomicilio() {
        when(repositorio.rutaDisponible(1, 4)).thenReturn(true);
        when(repositorio.buscarTarifaActiva(new BigDecimal("3.50")))
                .thenReturn(Optional.of(tarifa()));

        var domicilio = servicio.cotizar(
                1, 4, new BigDecimal("3.50"), ModalidadEnvio.DOMICILIO);
        var agencia = servicio.cotizar(
                1, 4, new BigDecimal("3.50"), ModalidadEnvio.AGENCIA);

        assertEquals(new BigDecimal("23.00"), domicilio.total());
        assertEquals(new BigDecimal("8.00"), domicilio.recargoDomicilio());
        assertEquals(new BigDecimal("15.00"), agencia.total());
        assertEquals(new BigDecimal("0.00"), agencia.recargoDomicilio());
    }

    @Test
    void devuelveTarifaDelRangoCuandoElPesoCoincideConElLimiteSuperior() {
        BigDecimal pesoLimite = new BigDecimal("5.00");
        when(repositorio.rutaDisponible(1, 4)).thenReturn(true);
        when(repositorio.buscarTarifaActiva(pesoLimite))
                .thenReturn(Optional.of(tarifa()));

        var cotizacion = servicio.cotizar(1, 4, pesoLimite, ModalidadEnvio.AGENCIA);

        assertEquals(new BigDecimal("0.00"), cotizacion.rangoPesoMinKg());
        assertEquals(new BigDecimal("5.00"), cotizacion.rangoPesoMaxKg());
    }

    @Test
    void rechazaPesosConMasDeDosDecimales() {
        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.cotizar(
                        1, 4, new BigDecimal("3.501"), ModalidadEnvio.AGENCIA)
        );
    }

    @Test
    void devuelve404CuandoLaRutaNoEstaDisponible() {
        when(repositorio.rutaDisponible(1, 4)).thenReturn(false);

        assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> servicio.cotizar(
                        1, 4, new BigDecimal("3.50"), ModalidadEnvio.AGENCIA)
        );
    }

    private Tarifa tarifa() {
        return new Tarifa(
            1L,
                new BigDecimal("0.00"),
                new BigDecimal("5.00"),
                new BigDecimal("15.00"),
                new BigDecimal("8.00"),
                true
        );
    }
}
