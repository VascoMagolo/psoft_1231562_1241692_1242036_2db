package aisafe.routes.application.dtos;

public record SearchRoutesRequest(String origin, String destination, int pageNumber, int pageSize) {}
