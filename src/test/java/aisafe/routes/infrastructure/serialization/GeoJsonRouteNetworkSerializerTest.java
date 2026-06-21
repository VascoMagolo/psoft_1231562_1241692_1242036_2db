package aisafe.routes.infrastructure.serialization;

import aisafe.airports.domain.Airport;
import aisafe.airports.domain.Coordinates;
import aisafe.airports.domain.IataCode;
import aisafe.shared.application.ExportedFile;
import aisafe.routes.domain.Route;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GeoJsonRouteNetworkSerializerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GeoJsonRouteNetworkSerializer serializer = new GeoJsonRouteNetworkSerializer();

    @Test
    void ensureGeoJsonSerializationCorrectness() throws Exception {
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
        assertEquals("application/geo+json", result.contentType());
        assertEquals("routes.geojson", result.fileName());
        
        JsonNode node = objectMapper.readTree(result.content());
        assertEquals("FeatureCollection", node.get("type").asText());
        JsonNode feature = node.get("features").get(0);
        assertEquals("Feature", feature.get("type").asText());
        assertEquals("LineString", feature.get("geometry").get("type").asText());
        
        JsonNode coords = feature.get("geometry").get("coordinates");
        assertEquals(-8.6814, coords.get(0).get(0).asDouble());
        assertEquals(41.2481, coords.get(0).get(1).asDouble());
        assertEquals(-9.1342, coords.get(1).get(0).asDouble());
        assertEquals(38.7742, coords.get(1).get(1).asDouble());
    }

    @Test
    void ensureSupportsCorrectFormat() {
        assertTrue(serializer.supports("geojson"));
        assertTrue(serializer.supports("GEOJSON"));
        assertFalse(serializer.supports("kml"));
    }

    @Test
    void ensureSerializeThrowsExceptionWhenAirportMissing() {
        Route route = new Route("OPO", "LIS", 60, 300.0, 100);
        // empty airport map will cause NullPointerException during serialization, wrapped into RuntimeException
        assertThrows(RuntimeException.class, () -> serializer.serialize(List.of(route), Map.of()));
    }

    @Test
    void ensureSerializeThrowsExceptionOnJacksonError() throws Exception {
        java.lang.reflect.Field field = GeoJsonRouteNetworkSerializer.class.getDeclaredField("objectMapper");
        field.setAccessible(true);
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        ObjectMapper realMapper = new ObjectMapper();
        when(mockMapper.createObjectNode()).thenReturn(realMapper.createObjectNode());
        when(mockMapper.writerWithDefaultPrettyPrinter()).thenThrow(new RuntimeException("Jackson error"));
        
        GeoJsonRouteNetworkSerializer localSerializer = new GeoJsonRouteNetworkSerializer();
        field.set(localSerializer, mockMapper);

        Route route = new Route("OPO", "LIS", 60, 300.0, 100);
        Airport origin = mock(Airport.class);
        when(origin.getIataCode()).thenReturn(new IataCode("OPO"));
        when(origin.getCoordinates()).thenReturn(new Coordinates(41.2481, -8.6814));
        Airport destination = mock(Airport.class);
        when(destination.getIataCode()).thenReturn(new IataCode("LIS"));
        when(destination.getCoordinates()).thenReturn(new Coordinates(38.7742, -9.1342));
        Map<String, Airport> airports = Map.of("OPO", origin, "LIS", destination);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> localSerializer.serialize(List.of(route), airports));
        assertEquals("Failed to serialize to GeoJSON", ex.getMessage());
    }
}
