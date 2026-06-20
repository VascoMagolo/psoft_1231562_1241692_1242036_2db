package aisafe.shared.application.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BulkImportRowError {
    private final int rowNumber;
    private final String data;
    private final String errorMessage;
}
