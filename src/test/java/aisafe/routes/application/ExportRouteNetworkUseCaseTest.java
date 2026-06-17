package aisafe.routes.application;

import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirportRepository;
import aisafe.airports.domain.IataCode;
import aisafe.routes.application.dtos.ExportedFile;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportRouteNetworkUseCaseTest {

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private AirportRepository airportRepository;

    @Mock
    private RouteNetworkSerializer serializer;

    private ExportRouteNetworkUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ExportRouteNetworkUseCase(routeRepository, airportRepository, List.of(serializer));
    }

    @Test
    void ensureExportSuccess() {
        // Arrange
        String format = "geojson";
        Route route = new Route("OPO", "LIS", 60, 300.0, 100);
        when(routeRepository.findAllActive()).thenReturn(List.of(route));
        
        Airport origin = mock(Airport.class);
        when(origin.getIataCode()).thenReturn(new IataCode("OPO"));
        Airport destination = mock(Airport.class);
        when(destination.getIataCode()).thenReturn(new IataCode("LIS"));
        
        when(airportRepository.findByIataCode(new IataCode("OPO"))).thenReturn(Optional.of(origin));
        when(airportRepository.findByIataCode(new IataCode("LIS"))).thenReturn(Optional.of(destination));
        
        when(serializer.supports(format)).thenReturn(true);
        ExportedFile expectedFile = new ExportedFile(new byte[]{1, 2, 3}, "application/geo+json", "routes.geojson");
        when(serializer.serialize(any(), any())).thenReturn(expectedFile);

        // Act
        ExportedFile result = useCase.execute(format);

        // Assert
        assertEquals(expectedFile, result);
        verify(routeRepository).findAllActive();
        verify(airportRepository, times(2)).findByIataCode(any());
        verify(serializer).serialize(any(), any());
    }

    @Test
    void ensureThrowsExceptionForUnsupportedFormat() {
        // Arrange
        when(serializer.supports("invalid")).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> useCase.execute("invalid"));
    }
}
