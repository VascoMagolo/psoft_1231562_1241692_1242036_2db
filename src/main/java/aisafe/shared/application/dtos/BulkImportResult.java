package aisafe.shared.application.dtos;

import lombok.Getter;
import java.util.ArrayList;
import java.util.List;

@Getter
public class BulkImportResult<T> {
    private final List<T> successfulImports = new ArrayList<>();
    private final List<BulkImportRowError> errors = new ArrayList<>();
    private int totalRowsProcessed = 0;

    public void addSuccess(T item) {
        successfulImports.add(item);
        totalRowsProcessed++;
    }

    public void addError(int rowNumber, String data, String errorMessage) {
        errors.add(new BulkImportRowError(rowNumber, data, errorMessage));
        totalRowsProcessed++;
    }

    public boolean isFullySuccessful() {
        return errors.isEmpty() && totalRowsProcessed > 0;
    }
    
    public boolean hasAnySuccess() {
        return !successfulImports.isEmpty();
    }
}
