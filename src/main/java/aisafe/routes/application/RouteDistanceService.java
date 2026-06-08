package aisafe.routes.application;

import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportRepository;
import aisafe.airports.domain.Coordinates;
import aisafe.routes.domain.Route;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RouteDistanceService {

    private static final double EARTH_RADIUS_KM = 6371.0;
    private final AirportRepository airportRepository;

    public double calculateDistanceKm(Route route) {
        Airport origin = airportRepository.findByIataCodeCode(route.getOrigin().getCode())
                .orElseThrow(() -> new AirportNotFoundException(route.getOrigin().getCode()));
        Airport destination = airportRepository.findByIataCodeCode(route.getDestination().getCode())
                .orElseThrow(() -> new AirportNotFoundException(route.getDestination().getCode()));

        return calculateDistanceKm(origin.getCoordinates(), destination.getCoordinates());
    }

    private double calculateDistanceKm(Coordinates origin, Coordinates destination) {
        double latDistance = Math.toRadians(destination.getLatitude() - origin.getLatitude());
        double lonDistance = Math.toRadians(destination.getLongitude() - origin.getLongitude());
        double originLatitude = Math.toRadians(origin.getLatitude());
        double destinationLatitude = Math.toRadians(destination.getLatitude());

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(originLatitude) * Math.cos(destinationLatitude)
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round(EARTH_RADIUS_KM * c * 100.0) / 100.0;
    }
}
