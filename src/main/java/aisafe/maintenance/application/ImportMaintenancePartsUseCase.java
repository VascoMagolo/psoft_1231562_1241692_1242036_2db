package aisafe.maintenance.application;

import aisafe.maintenance.application.dtos.CreateMaintenancePartRequest;
import aisafe.maintenance.domain.MaintenanceComponent;
import aisafe.shared.application.UseCase;
import aisafe.shared.application.dtos.BulkImportResult;
import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;

@UseCase
@RequiredArgsConstructor
public class ImportMaintenancePartsUseCase {

    private final CreateMaintenancePartUseCase createMaintenancePartUseCase;

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public BulkImportResult<String> execute(MultipartFile file) {
        BulkImportResult<String> result = new BulkImportResult<>();

        try (Reader reader = new InputStreamReader(file.getInputStream());
             CSVReader csvReader = new CSVReader(reader)) {

            String[] header = csvReader.readNext();
            if (header == null) {
                result.addError(0, "File", "CSV file is empty");
                return result;
            }

            int partNumberIdx = -1, nameIdx = -1, descIdx = -1, stockIdx = -1, thresholdIdx = -1, componentIdx = -1;
            for (int i = 0; i < header.length; i++) {
                String col = header[i].trim().toLowerCase();
                if (col.equals("partnumber")) partNumberIdx = i;
                else if (col.equals("name")) nameIdx = i;
                else if (col.equals("description")) descIdx = i;
                else if (col.equals("stockquantity")) stockIdx = i;
                else if (col.equals("minimumthreshold")) thresholdIdx = i;
                else if (col.equals("component")) componentIdx = i;
            }

            if (partNumberIdx == -1 || nameIdx == -1 || stockIdx == -1 || thresholdIdx == -1 || componentIdx == -1) {
                result.addError(0, "Headers", "Missing required columns (partNumber, name, stockQuantity, minimumThreshold, component)");
                return result;
            }

            String[] line;
            int rowNumber = 1;

            while ((line = csvReader.readNext()) != null) {
                rowNumber++;
                try {
                    String partNumber = getValue(line, partNumberIdx);
                    String name = getValue(line, nameIdx);
                    String description = getValue(line, descIdx);
                    Integer stock = parseInt(getValue(line, stockIdx));
                    Integer threshold = parseInt(getValue(line, thresholdIdx));
                    String componentStr = getValue(line, componentIdx);

                    MaintenanceComponent component = componentStr != null ? MaintenanceComponent.valueOf(componentStr.toUpperCase()) : null;

                    CreateMaintenancePartRequest request = new CreateMaintenancePartRequest(
                            partNumber, name, description, stock, threshold, component
                    );

                    createMaintenancePartUseCase.execute(request);
                    result.addSuccess(partNumber);
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

    private Integer parseInt(String val) {
        if (val == null) return null;
        return Integer.parseInt(val);
    }
}
