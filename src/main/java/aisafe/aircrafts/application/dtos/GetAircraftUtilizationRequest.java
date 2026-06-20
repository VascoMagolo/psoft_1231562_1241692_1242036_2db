package aisafe.aircrafts.application.dtos;

import java.time.LocalDate;

public record GetAircraftUtilizationRequest(String registration, LocalDate startDate, LocalDate endDate) {}
