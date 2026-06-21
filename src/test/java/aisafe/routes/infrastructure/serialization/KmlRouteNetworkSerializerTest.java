package aisafe.routes.infrastructure.serialization;

import aisafe.airports.domain.Airport;
import aisafe.airports.domain.Coordinates;
import aisafe.airports.domain.IataCode;
import aisafe.shared.application.ExportedFile;
import aisafe.routes.domain.Route;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import org.mockito.Mockito;
import org.mockito.ArgumentMatchers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KmlRouteNetworkSerializerTest {

    private final KmlRouteNetworkSerializer serializer = new KmlRouteNetworkSerializer();

    @Test
    void ensureKmlSerializationCorrectness() {
        // Arrange
        Route route = new Route("OPO", "LIS", 60, 300.0, 100);
        Airport origin = mock(Airport.class);
        when(origin.getIataCode()).thenReturn(new IataCode("OPO"));
        when(origin.getCoordinates()).thenReturn(new Coordinates(41.2481, -8.6814));
        
        Airport destination = mock(Airport.class);
        when(destination.getIataCode()).thenReturn(new IataCode("LIS"));
        when(destination.getCoordinates()).thenReturn(new Coordinates(38.7742, -9.1342));
        
        Map<String, Airport> airports = Map.of("OPO", origin, "LIS", destination);

        // Act
        ExportedFile result = serializer.serialize(List.of(route), airports);

        // Assert
        assertEquals("application/vnd.google-earth.kml+xml", result.contentType());
        assertEquals("routes.kml", result.fileName());
        
        String kmlContent = new String(result.content());
        assertTrue(kmlContent.contains("Placemark"), "Missing Placemark\n" + kmlContent);
        assertTrue(kmlContent.contains("-8.6814,41.2481"), "Missing first coordinate\n" + kmlContent);
        assertTrue(kmlContent.contains("-9.1342,38.7742"), "Missing second coordinate\n" + kmlContent);
    }

    @Test
    void ensureSupportsCorrectFormat() {
        assertTrue(serializer.supports("kml"));
        assertTrue(serializer.supports("KML"));
        assertFalse(serializer.supports("geojson"));
    }

    @Test
    void ensureSerializeThrowsExceptionWhenAirportMissing() {
        Route route = new Route("OPO", "LIS", 60, 300.0, 100);
        // empty airport map will cause NullPointerException during serialization, wrapped into RuntimeException
        assertThrows(RuntimeException.class, () -> serializer.serialize(List.of(route), Map.of()));
    }

    @Test
    void ensureSerializeThrowsExceptionOnKmlMarshalError() throws Exception {
        Route route = new Route("OPO", "LIS", 60, 300.0, 100);
        Airport origin = mock(Airport.class);
        when(origin.getIataCode()).thenReturn(new IataCode("OPO"));
        when(origin.getCoordinates()).thenReturn(new Coordinates(41.2481, -8.6814));
        Airport destination = mock(Airport.class);
        when(destination.getIataCode()).thenReturn(new IataCode("LIS"));
        when(destination.getCoordinates()).thenReturn(new Coordinates(38.7742, -9.1342));
        Map<String, Airport> airports = Map.of("OPO", origin, "LIS", destination);

        Document realDocument = new Kml().createAndSetDocument();

        try (var mocked = Mockito.mockConstruction(Kml.class, (mock, context) -> {
            when(mock.createAndSetDocument()).thenReturn(realDocument);
            Mockito.doThrow(new RuntimeException("JAXB marshalling error")).when(mock).marshal(ArgumentMatchers.any(OutputStream.class));
        })) {
            RuntimeException ex = assertThrows(RuntimeException.class, () -> serializer.serialize(List.of(route), airports));
            assertEquals("Failed to serialize to KML", ex.getMessage());
        }
    }
}
