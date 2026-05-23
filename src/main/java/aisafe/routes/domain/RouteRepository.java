package aisafe.routes.domain;

import aisafe.airports.domain.IataCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The interface Route repository.
 */
@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {

    /**
     * Find by origin list.
     *
     * @param origin the origin
     * @return the list
     */
    List<Route> findByOrigin(IataCode origin);

    /**
     * Find by destination list.
     *
     * @param destination the destination
     * @return the list
     */
    List<Route> findByDestination(IataCode destination);

    /**
     * Find by origin and destination list.
     *
     * @param origin      the origin
     * @param destination the destination
     * @return the list
     */
    List<Route> findByOriginAndDestination(IataCode origin, IataCode destination);

    /**
     * Exists by origin and destination boolean.
     *
     * @param origin      the origin
     * @param destination the destination
     * @return the boolean
     */
    boolean existsByOriginAndDestination(IataCode origin, IataCode destination);

    /**
     * Retrieves routes that match either the specified origin or destination IATA code.
     *
     * @param origin      the origin IATA code to search for
     * @param destination the destination IATA code to search for
     * @return a list of matching routes
     */
    List<Route> findByOriginOrDestination(IataCode origin, IataCode destination);
}
