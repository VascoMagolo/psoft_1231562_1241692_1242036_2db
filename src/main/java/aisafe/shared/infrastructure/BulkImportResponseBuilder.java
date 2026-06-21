package aisafe.shared.infrastructure;

import aisafe.shared.application.dtos.BulkImportResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class BulkImportResponseBuilder {

    private BulkImportResponseBuilder() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static <T> ResponseEntity<Map<String, Object>> buildResponse(BulkImportResult<T> result) {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("totalProcessed", result.getTotalRowsProcessed());
        responseBody.put("successfulCount", result.getSuccessfulImports().size());
        responseBody.put("errorCount", result.getErrors().size());
        
        if (!result.getErrors().isEmpty()) {
            responseBody.put("errors", result.getErrors());
        }

        if (result.isFullySuccessful()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
        } else if (result.hasAnySuccess()) {
            return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(responseBody);
        } else {
            // Nothing was successfully imported, but we processed rows -> 400 Bad Request
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
        }
    }
}
