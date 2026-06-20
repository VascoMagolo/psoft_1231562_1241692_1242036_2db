package aisafe.aircrafts.application.dtos;

public record UpdateAircraftModelImageRequest(String modelName, byte[] image, String imageContentType) {}
