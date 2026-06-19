package aisafe.routes.application;

import aisafe.routes.application.dtos.CreateRouteRequest;
import aisafe.shared.application.UseCase;
import aisafe.shared.application.dtos.BulkImportResult;
import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;

@UseCase
@RequiredArgsConstructor
public class ImportRoutesUseCase {

    private final CreateRouteUseCase createRouteUseCase;

    public BulkImportResult<String> execute(MultipartFile file, String createdBy) {
        BulkImportResult<String> result = new BulkImportResult<>();

        try (Reader reader = new InputStreamReader(file.getInputStream());
             CSVReader csvReader = new CSVReader(reader)) {

            String[] header = csvReader.readNext();
            if (header == null) {
                result.addError(0, "File", "CSV file is empty");
                return result;
            }

            int originIndex = -1, destIndex = -1, timeIndex = -1, rangeIndex = -1, capacityIndex = -1;
            for (int i = 0; i < header.length; i++) {
                String col = header[i].trim().toLowerCase();
                switch (col) {
                    case "originiatacode": originIndex = i; break;
                    case "destinationiatacode": destIndex = i; break;
                    case "estimatedflighttime": timeIndex = i; break;
                    case "minimumrange": rangeIndex = i; break;
                    case "minimumcapacity": capacityIndex = i; break;
                }
            }

            if (originIndex == -1 || destIndex == -1 || timeIndex == -1 || rangeIndex == -1 || capacityIndex == -1) {
                result.addError(0, "Headers", "Missing required columns (originIataCode, destinationIataCode, estimatedFlightTime, minimumRange, minimumCapacity)");
                return result;
            }

            String[] line;
            int rowNumber = 1;

            while ((line = csvReader.readNext()) != null) {
                rowNumber++;
                try {
                    String origin = getValue(line, originIndex);
                    String dest = getValue(line, destIndex);
                    Integer time = parseInteger(getValue(line, timeIndex));
                    Double range = parseDouble(getValue(line, rangeIndex));
                    Integer capacity = parseInteger(getValue(line, capacityIndex));

                    CreateRouteRequest request = new CreateRouteRequest(
                            origin, dest, time, range, capacity, createdBy != null ? createdBy : "BulkImport"
                    );

                    createRouteUseCase.execute(request);
                    result.addSuccess(origin + "-" + dest);
                } catch (Exception ex) {
                    result.addError(rowNumber, String.join(",", line), ex.getMessage());
                }
            }

        } catch (Exception e) {
            result.addError(0, "File Processing", "Failed to parse CSV file: " + e.getMessage());
        }

        return result;
    }

    private String getValue(String[] line, int index) {
        if (index == -1 || index >= line.length) return null;
        String val = line[index].trim();
        return val.isEmpty() ? null : val;
    }

    private Double parseDouble(String val) {
        if (val == null) return null;
        try {
            return Double.parseDouble(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInteger(String val) {
        if (val == null) return null;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
