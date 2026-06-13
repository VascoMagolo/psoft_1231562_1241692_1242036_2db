package aisafe.airports.infrastructure.persistence.jpa;

import aisafe.airports.domain.*;

import java.util.List;
import java.util.stream.Collectors;

public class AirportMapper {

    public static Airport toDomain(AirportJpaEntity entity) {
        if (entity == null) return null;

        List<Runway> runways = entity.getRunways().stream()
                .map(r -> new Runway(r.getName(), r.getLength(), r.getOrientation()))
                .collect(Collectors.toList());

        Airport airport = new Airport(
                entity.getIataCode(),
                entity.getName(),
                entity.getCity(),
                entity.getCountry(),
                entity.getRegion(),
                entity.getTimezone(),
                entity.getLatitude(),
                entity.getLongitude(),
                runways
        );

        List<Contact> contacts = entity.getContacts().stream()
                .map(c -> new Contact(ContactType.valueOf(c.getType()), c.getValue(), c.getDescription()))
                .collect(Collectors.toList());

        List<Service> services = entity.getServices().stream()
                .map(Service::new)
                .collect(Collectors.toList());

        List<Terminal> terminals = entity.getTerminals().stream()
                .map(Terminal::new)
                .collect(Collectors.toList());

        List<Gate> gates = entity.getGates().stream()
                .map(Gate::new)
                .collect(Collectors.toList());

        airport.setStatus(AirportStatus.valueOf(entity.getStatus()));
        airport.updateDetails(entity.getOperationalHours(), contacts, entity.getImagePath(), services, terminals, gates);

        return airport;
    }

    public static AirportJpaEntity toJpa(Airport domain) {
        if (domain == null) return null;

        AirportJpaEntity entity = new AirportJpaEntity();
        entity.setIataCode(domain.getIataCode().getCode());
        entity.setName(domain.getName());
        entity.setCity(domain.getCity());
        entity.setCountry(domain.getCountry());
        entity.setRegion(domain.getRegion());
        entity.setTimezone(domain.getTimezone());
        entity.setLatitude(domain.getCoordinates().getLatitude());
        entity.setLongitude(domain.getCoordinates().getLongitude());
        entity.setStatus(domain.getStatus().name());
        entity.setImagePath(domain.getImagePath());
        entity.setOperationalHours(domain.getOperationalHours());

        entity.setRunways(domain.getRunways().stream()
                .map(r -> new RunwayEmbeddable(r.getName(), r.getLength(), r.getOrientation()))
                .collect(Collectors.toList()));

        entity.setContacts(domain.getContacts().stream()
                .map(c -> new ContactEmbeddable(c.getType().name(), c.getValue(), c.getDescription()))
                .collect(Collectors.toList()));

        entity.setServices(domain.getServices().stream()
                .map(Service::getDescription)
                .collect(Collectors.toList()));

        entity.setTerminals(domain.getTerminals().stream()
                .map(Terminal::getName)
                .collect(Collectors.toList()));

        entity.setGates(domain.getGates().stream()
                .map(Gate::getIdentifier)
                .collect(Collectors.toList()));

        return entity;
    }
}
