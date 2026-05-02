package aisafe;

import aisafe.aircrafts.domain.AircraftModel;
import aisafe.aircrafts.domain.AircraftModelRepository;
import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirportRepository;
import aisafe.model.valueObject.Runway;
import aisafe.security.domain.Role;
import aisafe.security.domain.User;
import aisafe.security.domain.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class Bootstrap implements ApplicationRunner {
    private final UserRepository userRepository;
    private final AircraftModelRepository aircraftModelRepository;
    private final AirportRepository airportRepository;
    private final PasswordEncoder passwordEncoder;

    public Bootstrap(UserRepository userRepository, AircraftModelRepository aircraftModelRepository,
                     AirportRepository airportRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.aircraftModelRepository = aircraftModelRepository;
        this.airportRepository = airportRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (aircraftModelRepository.count() == 0) {
            aircraftModelRepository.save(new AircraftModel("Boeing 737 MAX", "Boeing", 25941.0, 6570.0, 839.0, "images/b737max.png"));
        }
        if (userRepository.count() == 0) {
            userRepository.save(new User("admin", passwordEncoder.encode("admin123"), Role.ADMIN));
            userRepository.save(new User("operator", passwordEncoder.encode("operator123"), Role.BACKOFFICE_OPERATOR));
            userRepository.save(new User("atcc", passwordEncoder.encode("atcc123"), Role.ATCC));
        }
        if (airportRepository.count() == 0) {
            airportRepository.save(new Airport("LIS", "Humberto Delgado Airport", "Lisbon", "Portugal",
                    "Southern Europe", "Europe/Lisbon", 38.7742, -9.1342,
                    List.of(new Runway("03/21", 3805, "030/210"), new Runway("17/35", 2400, "170/350"))));

            airportRepository.save(new Airport("OPO", "Francisco de Sá Carneiro Airport", "Porto", "Portugal",
                    "Southern Europe", "Europe/Lisbon", 41.2481, -8.6814,
                    List.of(new Runway("17/35", 3480, "170/350"))));

            airportRepository.save(new Airport("MAD", "Adolfo Suárez Madrid-Barajas Airport", "Madrid", "Spain",
                    "Southern Europe", "Europe/Madrid", 40.4936, -3.5668,
                    List.of(new Runway("14L/32R", 4100, "140/320"), new Runway("18L/36R", 3500, "180/360"))));

            airportRepository.save(new Airport("CDG", "Charles de Gaulle Airport", "Paris", "France",
                    "Western Europe", "Europe/Paris", 49.0097, 2.5479,
                    List.of(new Runway("08L/26R", 4215, "080/260"), new Runway("09R/27L", 4200, "090/270"))));

            airportRepository.save(new Airport("LHR", "Heathrow Airport", "London", "United Kingdom",
                    "Northern Europe", "Europe/London", 51.4775, -0.4614,
                    List.of(new Runway("09L/27R", 3902, "090/270"), new Runway("09R/27L", 3658, "090/270"))));
        }
    }
}
