package aisafe.routes.application.dtos;

import java.util.List;

public record AlternativeRouteResponse(
        List<String> airportPath,
        Integer numberOfStops
) {}
