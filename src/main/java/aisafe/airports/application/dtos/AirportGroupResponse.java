package aisafe.airports.application.dtos;

import java.util.List;

/**
 * Response DTO representing a group of airports, categorized by a specific attribute (e.g., country, region).
 * @param group the name of the group
 * @param airports the list of airports that belong to this group
 */
public record AirportGroupResponse(String group, List<AirportResponse> airports) {}
