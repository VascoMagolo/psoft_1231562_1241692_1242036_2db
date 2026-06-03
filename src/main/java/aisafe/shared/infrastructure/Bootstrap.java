package aisafe.shared.infrastructure;

import aisafe.aircrafts.domain.*;
import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirportRepository;
import aisafe.airports.domain.Runway;
import aisafe.maintenance.domain.*;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteRepository;
import aisafe.security.domain.Role;
import aisafe.security.domain.User;
import aisafe.security.domain.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Simple initialization component that populates the database with some sample
 * data on application startup.
 */
@Component
public class Bootstrap implements ApplicationRunner {
    private final UserRepository userRepository;
    private final AircraftModelRepository aircraftModelRepository;
    private final AircraftRepository aircraftRepository;
    private final AirportRepository airportRepository;
    private final MaintenancePartRepository maintenancePartRepository;
    private final MaintenanceTemplateRepository maintenanceTemplateRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final RouteRepository routeRepository;
    private final PasswordEncoder passwordEncoder;

    public Bootstrap(UserRepository userRepository, AircraftModelRepository aircraftModelRepository,
            AircraftRepository aircraftRepository,
            AirportRepository airportRepository, MaintenancePartRepository maintenancePartRepository,
            MaintenanceTemplateRepository maintenanceTemplateRepository,
            MaintenanceRecordRepository maintenanceRecordRepository, RouteRepository routeRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.aircraftModelRepository = aircraftModelRepository;
        this.aircraftRepository = aircraftRepository;
        this.airportRepository = airportRepository;
        this.maintenancePartRepository = maintenancePartRepository;
        this.maintenanceTemplateRepository = maintenanceTemplateRepository;
        this.maintenanceRecordRepository = maintenanceRecordRepository;
        this.routeRepository = routeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // bootstrap for user package
        if (userRepository.count() == 0) {
            userRepository.save(new User("admin", passwordEncoder.encode("admin123"), Role.ADMIN));
            userRepository.save(new User("operator", passwordEncoder.encode("operator123"), Role.BACKOFFICE_OPERATOR));
            userRepository.save(new User("atcc", passwordEncoder.encode("atcc123"), Role.ATCC));
            userRepository
                    .save(new User("technician", passwordEncoder.encode("technician123"), Role.MAINTENANCE_TECHNICIAN));
            userRepository
                    .save(new User("supervisor", passwordEncoder.encode("supervisor123"), Role.MAINTENANCE_SUPERVISOR));
        }

        // bootstrap for aircraft package
        if (aircraftModelRepository.count() == 0) {
            aircraftModelRepository.save(new AircraftModel("Airbus A320neo", Manufacturer.AIRBUS, 24210.0, 6570.0,
                    828.0, "images/a320neo.png", 240));
            aircraftModelRepository.save(new AircraftModel("Boeing 737 MAX", Manufacturer.BOEING, 25941.0, 6570.0,
                    839.0, "images/b737max.png", 300));
            aircraftModelRepository.save(new AircraftModel("Embraer E195-E2", Manufacturer.EMBRAER, 13000.0, 4260.0,
                    829.0, "images/e195e2.png", 146));
        }
        if (aircraftRepository.count() == 0) {
            AircraftModel a320neo = aircraftModelRepository.findByModelName("Airbus A320neo").orElseThrow();
            AircraftModel b737max = aircraftModelRepository.findByModelName("Boeing 737 MAX").orElseThrow();
            AircraftModel e195e2 = aircraftModelRepository.findByModelName("Embraer E195-E2").orElseThrow();

            aircraftRepository.save(new Aircraft(AircraftStatus.AVAILABLE, LocalDate.parse("2019-05-25"), a320neo,
                    new RegistrationNumber("CS-TKA"), 180, List.of("WiFi", "In-flight entertainment")),null);
            aircraftRepository.save(new Aircraft(AircraftStatus.UNDER_MAINTENANCE, LocalDate.parse("2020-08-15"),
                    b737max, new RegistrationNumber("CS-TKB"), 200,
                    List.of("WiFi", "In-flight entertainment", "Extra legroom")),null);
            aircraftRepository.save(new Aircraft(AircraftStatus.IN_FLIGHT, LocalDate.parse("2018-03-10"), e195e2,
                    new RegistrationNumber("CS-TKC"), 120, List.of("WiFi")),null);
        }

        // bootstrap for airport package
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

        // bootstrap for routes package
        if (routeRepository.count() == 0) {
            routeRepository.save(new Route("OPO", "LIS", 45, 280.0, 100));
            routeRepository.save(new Route("LIS", "OPO", 45, 280.0, 100));
            routeRepository.save(new Route("LIS", "MAD", 60, 640.0, 120));
            routeRepository.save(new Route("MAD", "LIS", 60, 640.0, 120));
            routeRepository.save(new Route("LIS", "CDG", 135, 1730.0, 150));
            routeRepository.save(new Route("CDG", "LIS", 135, 1730.0, 150));
            routeRepository.save(new Route("LIS", "LHR", 150, 1560.0, 150));
            routeRepository.save(new Route("LHR", "LIS", 150, 1560.0, 150));
            routeRepository.save(new Route("OPO", "MAD", 90, 850.0, 100));
            routeRepository.save(new Route("MAD", "CDG", 120, 1050.0, 120));
            routeRepository.save(new Route("CDG", "LHR", 60, 340.0, 100));
        }

        // bootstrap for maintenance package
        if (maintenanceTemplateRepository.count() == 0) {
            AircraftModel a320neo = aircraftModelRepository.findByModelName("Airbus A320neo").orElseThrow();
            AircraftModel b737max = aircraftModelRepository.findByModelName("Boeing 737 MAX").orElseThrow();
            AircraftModel e195e2 = aircraftModelRepository.findByModelName("Embraer E195-E2").orElseThrow();
            List<AircraftModel> models = List.of(a320neo, b737max, e195e2);
            maintenanceTemplateRepository.save(new MaintenanceTemplate("Inspection to the starter motor",
                    MaintenanceType.INSPECTION, List.of("Airbus A320neo", "Boeing 737 MAX"), List.of("starterMotor inspection"), 300, 365));
        }

        if (maintenancePartRepository.count() == 0) {
            maintenancePartRepository.save(new MaintenancePart("ST-1001", "Starter Motor",
                    "Motor responsible for starting the aircraft's engines.", 300, 15, MaintenanceComponent.ENGINE));
        }

        if (maintenanceRecordRepository.count() == 0) {
            MaintenancePart part = maintenancePartRepository.findByPartNumber("ST-1001").orElseThrow();
            MaintenanceTemplate template = maintenanceTemplateRepository.findByName("Inspection to the starter motor")
                    .orElseThrow();
            Aircraft aircraft1 = aircraftRepository.findByRegistrationNumber(new RegistrationNumber("CS-TKA"))
                    .orElseThrow();
            Aircraft aircraft2 = aircraftRepository.findByRegistrationNumber(new RegistrationNumber("CS-TKB"))
                    .orElseThrow();
            maintenanceRecordRepository.save(new MaintenanceRecord("Simple inspection to the starter motor",
                    LocalDateTime.parse("2024-06-01T10:00:00"), 120, part, "No issues found during the inspection.",
                    template, MaintenanceStatus.PLANNED, aircraft1.getRegistrationNumber().getNumber()));
            maintenanceRecordRepository.save(new MaintenanceRecord("Detailed inspection to the starter motor",
                    LocalDateTime.parse("2024-06-10T14:00:00"), 240, part,
                    "Minor wear detected, replacement recommended within the next 6 months.", template,
                    MaintenanceStatus.PLANNED, aircraft2.getRegistrationNumber().getNumber()));
        }
    }
}
