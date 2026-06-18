package aisafe.shared.application;

public record ExportedFile(byte[] content, String contentType, String fileName) {}
