package aisafe.airports.application;

import aisafe.airports.application.dtos.AirportGroupResponse;
import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirportRepository;
import aisafe.airports.domain.Runway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListAirportsByRegionUseCaseTest {

    @Mock
    private AirportRepository airportRepository;

    @InjectMocks
    private ListAirportsByRegionUseCase listAirportsByRegion;

    private Airport buildAirport(String iata, String country, String region) {
        Airport a = new Airport(iata, iata + " Airport", "City", country, region, "UTC",
                0.0, 0.0, List.of(new Runway("01/19", 2500, "010/190")));
        return a;
    }

    @Test
    void ensureGroupByRegionGroupsCorrectly() {
        Airport lis = buildAirport("LIS", "Portugal", "Europe");
        Airport jfk = buildAirport("JFK", "USA", "North America");

        when(airportRepository.findAll()).thenReturn(List.of(lis, jfk));

        List<AirportGroupResponse> result = listAirportsByRegion.execute("region");

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(g -> g.group().equals("Europe")));
        assertTrue(result.stream().anyMatch(g -> g.group().equals("North America")));
    }

    @Test
    void ensureGroupByCountryGroupsCorrectly() {
        Airport lis = buildAirport("LIS", "Portugal", "Europe");
        Airport opo = buildAirport("OPO", "Portugal", "Europe");
        Airport jfk = buildAirport("JFK", "USA", "North America");

        when(airportRepository.findAll()).thenReturn(List.of(lis, opo, jfk));

        List<AirportGroupResponse> result = listAirportsByRegion.execute("country");

        assertEquals(2, result.size());
        AirportGroupResponse portugal = result.stream().filter(g -> g.group().equals("Portugal")).findFirst().orElseThrow();
        assertEquals(2, portugal.airports().size());
    }

    @Test
    void ensureNullRegionFallsBackToUnknown() {
        Airport a = buildAirport("XXX", "Unknown Country", null);
        when(airportRepository.findAll()).thenReturn(List.of(a));

        List<AirportGroupResponse> result = listAirportsByRegion.execute("region");

        assertEquals(1, result.size());
        assertEquals("Unknown", result.get(0).group());
    }
}
