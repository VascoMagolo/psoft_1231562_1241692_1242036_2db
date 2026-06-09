package aisafe.aircrafts.application.dtos;

import java.time.LocalDate;

public record UtilizationDataPointResponse(
    LocalDate date,
    Double flightHours,
    Double utilizationRatePercentage
) {}
