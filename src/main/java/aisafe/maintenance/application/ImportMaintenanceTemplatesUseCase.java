package aisafe.maintenance.application;

import aisafe.maintenance.application.dtos.CreateMaintenanceTemplateRequest;
import aisafe.maintenance.domain.MaintenanceType;
import aisafe.shared.application.dtos.BulkImportResult;
import com.opencsv.CSVReader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

@Service
public class ImportMaintenanceTemplatesUseCase {

    private final CreateMaintenanceTemplateUseCase createMaintenanceTemplateUseCase;

    public ImportMaintenanceTemplatesUseCase(CreateMaintenanceTemplateUseCase createMaintenanceTemplateUseCase) {
        this.createMaintenanceTemplateUseCase = createMaintenanceTemplateUseCase;
    }

    public BulkImportResult<aisafe.maintenance.application.dtos.MaintenanceTemplateResponse> execute(MultipartFile file) {
        BulkImportResult<aisafe.maintenance.application.dtos.MaintenanceTemplateResponse> result = new BulkImportResult<>();
        try (Reader reader = new InputStreamReader(file.getInputStream());
             CSVReader csvReader = new CSVReader(reader)) {

            String[] line;
            boolean firstLine = true;
            int rowIndex = 0;
            while ((line = csvReader.readNext()) != null) {
                rowIndex++;
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                try {
                    String templateName = line[0];
                    String description = line[1];
                    int flightHoursThreshold = Integer.parseInt(line[2]);
                    int monthsThreshold = Integer.parseInt(line[3]);
                    int flightCyclesThreshold = Integer.parseInt(line[4]);
                    List<String> models = Arrays.asList(line[5].split(";"));
                    List<String> checklist = Arrays.asList(line[6].split("[,;]"));

                    CreateMaintenanceTemplateRequest request = new CreateMaintenanceTemplateRequest(
                            templateName,
                            MaintenanceType.INSPECTION,
                            models,
                            checklist,
                            flightHoursThreshold,
                            monthsThreshold
                    );
                    var response = createMaintenanceTemplateUseCase.execute(request);
                    result.addSuccess(response);
                } catch (Exception e) {
                    result.addError(rowIndex, String.join(",", line), e.getMessage());
                }
            }
        } catch (Exception e) {
            result.addError(0, "File", "Failed to parse CSV file: " + e.getMessage());
        }
        return result;
    }
}
