package aisafe.airports.application.dtos;

import java.util.List;

public record AirportGroupResponse(String group, List<AirportResponse> airports) {}
