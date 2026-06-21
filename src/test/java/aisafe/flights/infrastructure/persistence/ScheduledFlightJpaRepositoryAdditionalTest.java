package aisafe.flights.infrastructure.persistence;

import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.aircrafts.domain.Manufacturer;
import aisafe.aircrafts.domain.AircraftNotFoundException;
import aisafe.aircrafts.infrastructure.persistence.jpa.SpringDataAircraftRepository;
import aisafe.aircrafts.infrastructure.persistence.jpa.SpringDataAircraftModelRepository;
import aisafe.aircrafts.infrastructure.persistence.jpa.AircraftModelJpaEntity;
import aisafe.aircrafts.infrastructure.persistence.jpa.AircraftJpaEntity;
import aisafe.aircrafts.infrastructure.persistence.jpa.RegistrationNumberJpaEmbeddable;
import aisafe.routes.domain.RouteNotFoundException;
import java.time.LocalDate;
import aisafe.airports.domain.IataCode;
import aisafe.flights.domain.FlightStatus;
import aisafe.flights.domain.ScheduledFlight;
import aisafe.flights.domain.ScheduledFlightRepository;
import aisafe.flights.infrastructure.persistence.jpa.ScheduledFlightJpaEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("jpa")
@Transactional
class ScheduledFlightJpaRepositoryAdditionalTest {

    @Autowired
    private ScheduledFlightRepository domainRepository;

    @Autowired
    private SpringDataAircraftRepository aircraftRepo;

    @Autowired
    private SpringDataAircraftModelRepository modelRepo;

    private void createAircraft(String registration) {
        var model = new AircraftModelJpaEntity();
        model.setModelName("B737");
        model.setManufacturer(Manufacturer.BOEING);
        model.setMaximumSeatingCapacity(200);
        model.setMaxRange(20000.0);
        model.setFuelCapacity(5000.0);
        model.setCruisingSpeed(800.0);
        model = modelRepo.save(model);

        var aircraft = new AircraftJpaEntity();
        aircraft.setRegistrationNumber(new RegistrationNumberJpaEmbeddable(registration));
        aircraft.setModel(model);
        aircraft.setManufacturingDate(LocalDate.now());
        aircraft.setStatus("ACTIVE");
        aircraft.setSeatCapacity(200);
        aircraft.setRange(5000.0);
        aircraftRepo.save(aircraft);
    }

    @Test
    void ensureSaveThrowsAircraftNotFoundExceptionWhenAircraftDoesNotExist() {
        ScheduledFlight flight = new ScheduledFlight(
                null,
                OffsetDateTime.now(),
                OffsetDateTime.now().plusHours(2),
                FlightStatus.SCHEDULED,
                new IataCode("OPO"),
                new IataCode("LIS"),
                new RegistrationNumber("CS-XXX") // Non-existent registration number
        );

        assertThrows(AircraftNotFoundException.class, () -> domainRepository.save(flight));
    }

    @Test
    void ensureSaveThrowsRouteNotFoundExceptionWhenRouteDoesNotExist() {
        createAircraft("CS-TPA");

        ScheduledFlight flight = new ScheduledFlight(
                null,
                OffsetDateTime.now(),
                OffsetDateTime.now().plusHours(2),
                FlightStatus.SCHEDULED,
                new IataCode("XYZ"), // Non-existent airport code
                new IataCode("LIS"),
                new RegistrationNumber("CS-TPA")
        );

        assertThrows(RouteNotFoundException.class, () -> domainRepository.save(flight));
    }

    @Test
    void ensureScheduledFlightJpaEntityCoverage() {
        OffsetDateTime now = OffsetDateTime.now();
        ScheduledFlightJpaEntity entity = new ScheduledFlightJpaEntity();
        entity.setId(99L);
        entity.setDepartureDateTime(now);
        entity.setArrivalDateTime(now.plusHours(1));
        entity.setStatus(FlightStatus.SCHEDULED);
        entity.setVersion(1L);

        assertEquals(99L, entity.getId());
        assertEquals(now, entity.getDepartureDateTime());
        assertEquals(now.plusHours(1), entity.getArrivalDateTime());
        assertEquals(FlightStatus.SCHEDULED, entity.getStatus());
        assertEquals(1L, entity.getVersion());
    }

    @Test
    void ensureDeleteWithNullIdDoesNotThrowAndDoesNothing() {
        ScheduledFlight flight = new ScheduledFlight(
                null,
                OffsetDateTime.now(),
                OffsetDateTime.now().plusHours(2),
                FlightStatus.SCHEDULED,
                new IataCode("OPO"),
                new IataCode("LIS"),
                new RegistrationNumber("CS-TPA")
        );
        assertDoesNotThrow(() -> domainRepository.delete(flight));
    }
}
