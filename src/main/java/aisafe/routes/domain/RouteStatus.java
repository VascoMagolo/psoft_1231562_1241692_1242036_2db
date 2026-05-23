package aisafe.routes.domain;

/**
 * Represents the operational status of a Route.
 */
public enum RouteStatus {
    /**
     * The route is currently operational and in use.
     */
    ACTIVE,
    /**
     * The route is not currently in use.
     */
    INACTIVE,
    /**
     * The route is temporarily unavailable.
     */
    SUSPENDED
}
