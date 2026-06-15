package aisafe.aircrafts.application.dtos;

public record TopUtilizedModelResponse(
        String modelName,
        Long utilizationValue
) {}
