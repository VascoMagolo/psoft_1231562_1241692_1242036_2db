package aisafe.routes.application.dtos;

public record ExportedFile(byte[] content, String contentType, String fileName) {}
