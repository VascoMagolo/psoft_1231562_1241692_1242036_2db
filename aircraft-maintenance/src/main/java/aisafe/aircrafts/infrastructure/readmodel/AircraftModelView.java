package aisafe.aircrafts.infrastructure.readmodel;

import aisafe.aircrafts.domain.AircraftModel;
import aisafe.aircrafts.domain.Manufacturer;
import aisafe.aircrafts.infrastructure.replication.AircraftModelReplica;

/**
 * An aircraft model as kept by the read models and sent in the {@code aircraftmodel.*} events:
 * everything but the bytes of its image (only whether it has one).
 */
public record AircraftModelView(
        String modelName,
        Manufacturer manufacturer,
        Double fuelCapacity,
        Double maxRange,
        Double cruisingSpeed,
        Integer maximumSeatingCapacity,
        boolean hasImage
) {
    public static AircraftModelView from(AircraftModel model) {
        return new AircraftModelView(model.getModelName(), model.getManufacturer(), model.getFuelCapacity(),
                model.getMaxRange(), model.getCruisingSpeed(), model.getMaximumSeatingCapacity(), model.getImage() != null);
    }

    public static AircraftModelView from(AircraftModelReplica replica) {
        return new AircraftModelView(replica.modelName(), replica.manufacturer(), replica.fuelCapacity(), replica.maxRange(),
                replica.cruisingSpeed(), replica.maximumSeatingCapacity(), replica.imageBytes() != null);
    }
}
