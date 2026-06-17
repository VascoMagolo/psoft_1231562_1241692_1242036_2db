package aisafe.routes.application;

import aisafe.airports.domain.Airport;
import aisafe.routes.application.dtos.ExportedFile;
import aisafe.routes.domain.Route;

import java.util.List;
import java.util.Map;

public interface RouteNetworkSerializer {
    ExportedFile serialize(List<Route> routes, Map<String, Airport> airports);
    boolean supports(String format);
}
