package aisafe.routes.infrastructure.serialization;

import aisafe.airports.domain.Airport;
import aisafe.routes.application.RouteNetworkSerializer;
import aisafe.shared.application.ExportedFile;
import aisafe.routes.domain.Route;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

@Component
public class KmlRouteNetworkSerializer implements RouteNetworkSerializer {

    @Override
    public ExportedFile serialize(List<Route> routes, Map<String, Airport> airports) {
        Kml kml = new Kml();
        Document document = kml.createAndSetDocument().withName("Route Network");

        for (Route route : routes) {
            Airport origin = airports.get(route.getOrigin().getCode());
            Airport destination = airports.get(route.getDestination().getCode());

            Placemark placemark = document.createAndAddPlacemark()
                    .withName(route.getOrigin().getCode() + " - " + route.getDestination().getCode());
            
            placemark.createAndSetLineString()
                    .addToCoordinates(origin.getCoordinates().getLongitude(), origin.getCoordinates().getLatitude())
                    .addToCoordinates(destination.getCoordinates().getLongitude(), destination.getCoordinates().getLatitude());
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            kml.marshal(out);
            return new ExportedFile(out.toByteArray(), "application/vnd.google-earth.kml+xml", "routes.kml");
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize to KML", e);
        }
    }

    @Override
    public boolean supports(String format) {
        return "kml".equalsIgnoreCase(format);
    }
}
