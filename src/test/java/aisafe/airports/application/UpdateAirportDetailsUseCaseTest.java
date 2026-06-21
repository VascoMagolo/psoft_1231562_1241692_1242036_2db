package aisafe.airports.application;

import aisafe.airports.application.dtos.AirportResponse;
import aisafe.airports.application.dtos.UpdateAirportDetailsRequest;
import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportRepository;
import aisafe.airports.domain.ContactType;
import aisafe.airports.domain.IataCode;
import aisafe.airports.domain.Runway;
import aisafe.shared.domain.ConcurrencyException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateAirportDetailsUseCaseTest {

    @Mock
    private AirportRepository airportRepository;

    @InjectMocks
    private UpdateAirportDetailsUseCase updateAirportDetails;

    private Airport buildAirport(String iataCode) {
        return new Airport(iataCode, "Lisbon Airport", "Lisbon", "Portugal", "Europe",
                "Europe/Lisbon", 38.77, -9.13,
                List.of(new Runway("03/21", 3000, "030/210")));
    }

    @Test
    void ensureAirportDetailsAreUpdatedSuccessfully() {
        Airport airport = buildAirport("LIS");
        IataCode code = new IataCode("LIS");
        when(airportRepository.findByIataCode(code)).thenReturn(Optional.of(airport));
        when(airportRepository.findVersionFor(code)).thenReturn(0L);

        UpdateAirportDetailsRequest request = new UpdateAirportDetailsRequest(
                "06:00-23:00", null, null, null, null);

        AirportResponse result = updateAirportDetails.execute("LIS", request, 0L);

        assertNotNull(result);
        verify(airportRepository).save(any(Airport.class));
    }

    @Test
    void ensureExceptionWhenAirportNotFound() {
        when(airportRepository.findByIataCode(new IataCode("XXX"))).thenReturn(Optional.empty());

        UpdateAirportDetailsRequest request = new UpdateAirportDetailsRequest(
                "06:00-23:00", null, null, null, null);

        assertThrows(AirportNotFoundException.class, () -> updateAirportDetails.execute("XXX", request, 0L));
        verify(airportRepository, never()).save(any());
    }

    @Test
    void ensureConcurrencyExceptionWhenVersionMismatches() {
        Airport airport = buildAirport("LIS");
        IataCode code = new IataCode("LIS");
        when(airportRepository.findByIataCode(code)).thenReturn(Optional.of(airport));
        when(airportRepository.findVersionFor(code)).thenReturn(3L);

        UpdateAirportDetailsRequest request = new UpdateAirportDetailsRequest(
                "06:00-23:00", null, null, null, null);

        assertThrows(ConcurrencyException.class, () -> updateAirportDetails.execute("LIS", request, 0L));
        verify(airportRepository, never()).save(any());
    }

    @Test
    void ensureAirportDetailsAreUpdatedWithAllFieldsAndNullVersion() {
        Airport airport = buildAirport("LIS");
        IataCode code = new IataCode("LIS");
        when(airportRepository.findByIataCode(code)).thenReturn(Optional.of(airport));
        when(airportRepository.findVersionFor(code)).thenReturn(0L);

        UpdateAirportDetailsRequest.ContactRequest contact = new UpdateAirportDetailsRequest.ContactRequest(
                ContactType.EMAIL, "info@lisbonairport.com", "General Information");
        UpdateAirportDetailsRequest request = new UpdateAirportDetailsRequest(
                "06:00-23:00", List.of(contact), List.of("Wifi"), List.of("T1"), List.of("A1"));

        AirportResponse result = updateAirportDetails.execute("LIS", request, null);

        assertNotNull(result);
        verify(airportRepository).save(any(Airport.class));
        verify(airportRepository).findVersionFor(code);
    }

    @Test
    void ensureUpdateAirportDetailsRequestAndContactRequestWorks() {
        UpdateAirportDetailsRequest.ContactRequest contact = new UpdateAirportDetailsRequest.ContactRequest(
                ContactType.EMAIL, "info@lisbonairport.com", "General Information");

        UpdateAirportDetailsRequest request = new UpdateAirportDetailsRequest(
                "06:00-23:00", List.of(contact), List.of("Wifi"), List.of("T1"), List.of("A1"));

        assertEquals("06:00-23:00", request.operationalHours());
        assertEquals(1, request.contacts().size());
        assertEquals(ContactType.EMAIL, request.contacts().get(0).type());
        assertEquals("info@lisbonairport.com", request.contacts().get(0).value());
        assertEquals("General Information", request.contacts().get(0).description());
        assertEquals(List.of("Wifi"), request.services());
        assertEquals(List.of("T1"), request.terminals());
        assertEquals(List.of("A1"), request.gates());

        // Cover equals, hashCode, toString for complete DTO coverage
        assertNotNull(contact.toString());
        assertEquals(contact, new UpdateAirportDetailsRequest.ContactRequest(
                ContactType.EMAIL, "info@lisbonairport.com", "General Information"));
        assertEquals(contact.hashCode(), new UpdateAirportDetailsRequest.ContactRequest(
                ContactType.EMAIL, "info@lisbonairport.com", "General Information").hashCode());

        assertNotNull(request.toString());
        assertEquals(request, new UpdateAirportDetailsRequest(
                "06:00-23:00", List.of(contact), List.of("Wifi"), List.of("T1"), List.of("A1")));
        assertEquals(request.hashCode(), new UpdateAirportDetailsRequest(
                "06:00-23:00", List.of(contact), List.of("Wifi"), List.of("T1"), List.of("A1")).hashCode());
    }
}
