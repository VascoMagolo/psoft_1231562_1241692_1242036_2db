package aisafe.airports.application;

import aisafe.airports.application.dtos.AirportGroupResponse;
import aisafe.airports.domain.AirportGroupingData;
import aisafe.airports.domain.AirportRepository;
import aisafe.airports.domain.IataCode;
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

    private AirportGroupingData buildRow(String iata, String country, String region) {
        return new AirportGroupingData(new IataCode(iata), iata + " Airport", region, country);
    }

    @Test
    void ensureGroupByRegionGroupsCorrectly() {
        AirportGroupingData lis = buildRow("LIS", "Portugal", "Europe");
        AirportGroupingData jfk = buildRow("JFK", "USA", "North America");

        when(airportRepository.findAllGroupingByRegion()).thenReturn(List.of(lis, jfk));

        List<AirportGroupResponse> result = listAirportsByRegion.execute("region");

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(g -> g.group().equals("Europe")));
        assertTrue(result.stream().anyMatch(g -> g.group().equals("North America")));
    }

    @Test
    void ensureGroupByCountryGroupsCorrectly() {
        AirportGroupingData lis = buildRow("LIS", "Portugal", "Europe");
        AirportGroupingData opo = buildRow("OPO", "Portugal", "Europe");
        AirportGroupingData jfk = buildRow("JFK", "USA", "North America");

        when(airportRepository.findAllGroupingByCountry()).thenReturn(List.of(lis, opo, jfk));

        List<AirportGroupResponse> result = listAirportsByRegion.execute("country");

        assertEquals(2, result.size());
        AirportGroupResponse portugal = result.stream().filter(g -> g.group().equals("Portugal")).findFirst().orElseThrow();
        assertEquals(2, portugal.airports().size());
    }

    @Test
    void ensureNullRegionFallsBackToUnknown() {
        AirportGroupingData a = buildRow("XXX", "Unknown Country", null);
        when(airportRepository.findAllGroupingByRegion()).thenReturn(List.of(a));

        List<AirportGroupResponse> result = listAirportsByRegion.execute("region");

        assertEquals(1, result.size());
        assertEquals("Unknown", result.get(0).group());
    }
}
