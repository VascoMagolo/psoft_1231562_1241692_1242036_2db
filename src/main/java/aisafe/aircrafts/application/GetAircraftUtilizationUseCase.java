package aisafe.aircrafts.application;

import aisafe.aircrafts.application.dtos.UtilizationDataPointResponse;
import aisafe.aircrafts.domain.AircraftNotFoundException;
import aisafe.aircrafts.domain.AircraftRepository;
import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.routes.domain.ScheduledFlight;
import aisafe.routes.domain.ScheduledFlightRepository;
import aisafe.shared.application.UseCase;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@UseCase(readOnly = true)
public class GetAircraftUtilizationUseCase {

    private final AircraftRepository aircraftRepository;
    private final ScheduledFlightRepository scheduledFlightRepository;

    public GetAircraftUtilizationUseCase(AircraftRepository aircraftRepository, ScheduledFlightRepository scheduledFlightRepository) {
        this.aircraftRepository = aircraftRepository;
        this.scheduledFlightRepository = scheduledFlightRepository;
    }

    public List<UtilizationDataPointResponse> execute(String registration, LocalDate startDate, LocalDate endDate) {
        if (!aircraftRepository.existsByRegistrationNumber(new RegistrationNumber(registration))) {
            throw new AircraftNotFoundException("Aircraft not found.");
        }

        OffsetDateTime start = startDate.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime end = endDate.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC).minusNanos(1);

        List<ScheduledFlight> flights = scheduledFlightRepository.findFlightsForUtilization(registration, start, end);

        Map<LocalDate, Double> dailyHours = new TreeMap<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            dailyHours.put(date, 0.0);
        }

        for (ScheduledFlight flight : flights) {
            LocalDate flightDate = flight.getDepartureDateTime().toLocalDate();
            if (dailyHours.containsKey(flightDate)) {
                double hours = Duration.between(flight.getDepartureDateTime(), flight.getArrivalDateTime()).toMinutes() / 60.0;
                dailyHours.put(flightDate, dailyHours.get(flightDate) + hours);
            }
        }

        return dailyHours.entrySet().stream()
                .map(entry -> new UtilizationDataPointResponse(
                        entry.getKey(),
                        entry.getValue(),
                        (entry.getValue() / 24.0) * 100.0
                ))
                .collect(Collectors.toList());
    }
}
