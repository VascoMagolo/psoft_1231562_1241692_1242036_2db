package aisafe.shared.infrastructure.readmodel;

/**
 * An entity of another service that this service keeps in its read model, instead of asking the
 * owner for it on every request: the read model receives the owner's events
 * ({@code <source>.events}, routing keys {@code <entity>.*}), and asks the owner for all of them
 * (a snapshot) on startup and from time to time ({@link SnapshotClient}). The view is the event's
 * {@code data}, as the owner publishes it.
 */
public interface ExternalProjection {

    /** The owner's name for the entity ({@code aircraft}, {@code route}...): routing keys and read model type. */
    String entity();

    /** The service that owns it ({@code aircraft-maintenance}...). */
    String source();
}
