package aisafe.routes.domain;

import aisafe.airports.domain.IataCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The interface Route repository.
 */
@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {

    /**
     * Find by origin page.
     *
     * @param origin   the origin
     * @param pageable the pagination information
     * @return the page
     */
    Page<Route> findByOrigin(IataCode origin, Pageable pageable);

    /**
     * Find by destination page.
     *
     * @param destination the destination
     * @param pageable    the pagination information
     * @return the page
     */
    Page<Route> findByDestination(IataCode destination, Pageable pageable);

    /**
     * Find by origin and destination page.
     *
     * @param origin      the origin
     * @param destination the destination
     * @param pageable    the pagination information
     * @return the page
     */
    Page<Route> findByOriginAndDestination(IataCode origin, IataCode destination, Pageable pageable);

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