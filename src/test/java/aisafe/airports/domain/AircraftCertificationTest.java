package aisafe.airports.domain;

import aisafe.aircrafts.domain.ModelName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AircraftCertificationTest {

    @Test
    void ensureCertificationIsCreatedSuccessfully() {
        IataCode code = new IataCode("LIS");
        ModelName model = new ModelName("Airbus A320neo");

        AircraftCertification cert = new AircraftCertification(code, model);

        assertEquals(code, cert.getAirportCode());
        assertEquals(model, cert.getAircraftModelName());
    }

    @Test
    void ensureNullAirportCodeThrowsException() {
        assertThrows(InvalidAircraftCertificationException.class,
                () -> new AircraftCertification(null, new ModelName("Airbus A320neo")));
    }

    @Test
    void ensureNullModelNameThrowsException() {
        assertThrows(InvalidAircraftCertificationException.class,
                () -> new AircraftCertification(new IataCode("LIS"), null));
    }
}
