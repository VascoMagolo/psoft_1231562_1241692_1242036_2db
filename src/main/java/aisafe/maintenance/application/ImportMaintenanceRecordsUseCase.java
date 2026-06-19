package aisafe.maintenance.application;

import aisafe.maintenance.application.dtos.CreateMaintenanceRecordRequest;
import aisafe.maintenance.application.dtos.MaintenanceRecordResponse;
import aisafe.maintenance.domain.MaintenanceComponent;
import aisafe.maintenance.domain.MaintenanceStatus;
import aisafe.shared.application.dtos.BulkImportResult;
import com.opencsv.CSVReader;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import aisafe.shared.application.UseCase;

@UseCase
public class ImportMaintenanceRecordsUseCase {

    private final CreateMaintenanceRecordUseCase createMaintenanceRecordUseCase;

    public ImportMaintenanceRecordsUseCase(CreateMaintenanceRecordUseCase createMaintenanceRecordUseCase) {
        this.createMaintenanceRecordUseCase = createMaintenanceRecordUseCase;
    }

    public BulkImportResult<MaintenanceRecordResponse> execute(MultipartFile file) {
        BulkImportResult<MaintenanceRecordResponse> result = new BulkImportResult<>();
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
                    String aircraftRegistration = line[0];
                    String templateName = line[1];
                    LocalDateTime date = LocalDateTime.parse(line[2]);
                    MaintenanceStatus status = MaintenanceStatus.valueOf(line[3]);
                    Set<MaintenanceComponent> components = Arrays.stream(line[4].split("[,;]"))
                            .map(String::trim)
                            .map(MaintenanceComponent::valueOf)
                            .collect(Collectors.toSet());

                    List<String> parts = new ArrayList<>();
                    if (line.length > 5 && line[5] != null && !line[5].isEmpty()) {
                        parts = Arrays.stream(line[5].split("[,;]"))
                                .map(String::trim)
                                .collect(Collectors.toList());
                    }

                    CreateMaintenanceRecordRequest request = new CreateMaintenanceRecordRequest(
                            "Bulk imported record",
                            date,
                            0,
                            parts,
                            null,
                            templateName,
                            status,
                            aircraftRegistration,
                            components,
                            null
                    );
                    var response = createMaintenanceRecordUseCase.execute(request);
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
