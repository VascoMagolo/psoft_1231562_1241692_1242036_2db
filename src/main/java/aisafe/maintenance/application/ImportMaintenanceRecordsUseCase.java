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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import aisafe.shared.application.UseCase;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

@UseCase
public class ImportMaintenanceRecordsUseCase {

    private final CreateMaintenanceRecordUseCase createMaintenanceRecordUseCase;

    public ImportMaintenanceRecordsUseCase(CreateMaintenanceRecordUseCase createMaintenanceRecordUseCase) {
        this.createMaintenanceRecordUseCase = createMaintenanceRecordUseCase;
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public BulkImportResult<MaintenanceRecordResponse> execute(MultipartFile file) {
        BulkImportResult<MaintenanceRecordResponse> result = new BulkImportResult<>();
        try (Reader reader = new InputStreamReader(file.getInputStream());
             CSVReader csvReader = new CSVReader(reader)) {

            String[] header = csvReader.readNext();
            if (header == null) {
                result.addError(0, "File", "CSV file is empty");
                return result;
            }

            int regIdx = -1, templateIdx = -1, dateIdx = -1, statusIdx = -1, compIdx = -1, partsIdx = -1;
            int descIdx = -1, durIdx = -1, notesIdx = -1, costIdx = -1;

            for (int i = 0; i < header.length; i++) {
                String col = header[i].trim().toLowerCase();
                if (col.equals("registrationnumber")) regIdx = i;
                else if (col.equals("template")) templateIdx = i;
                else if (col.equals("startdate")) dateIdx = i;
                else if (col.equals("status")) statusIdx = i;
                else if (col.equals("components")) compIdx = i;
                else if (col.equals("parts")) partsIdx = i;
                else if (col.equals("description")) descIdx = i;
                else if (col.equals("expectedduration")) durIdx = i;
                else if (col.equals("notes")) notesIdx = i;
                else if (col.equals("cost")) costIdx = i;
            }

            if (regIdx == -1 || templateIdx == -1 || dateIdx == -1 || statusIdx == -1 || compIdx == -1 || descIdx == -1 || durIdx == -1 || costIdx == -1) {
                result.addError(0, "Headers", "Missing required columns");
                return result;
            }

            String[] line;
            int rowIndex = 1;
            while ((line = csvReader.readNext()) != null) {
                rowIndex++;

                try {
                    String aircraftRegistration = getValue(line, regIdx);
                    String templateName = getValue(line, templateIdx);
                    LocalDateTime date = LocalDateTime.parse(getValue(line, dateIdx));
                    MaintenanceStatus status = MaintenanceStatus.valueOf(getValue(line, statusIdx));
                    
                    Set<MaintenanceComponent> components = Arrays.stream(getValue(line, compIdx).split("[,;]"))
                            .map(String::trim)
                            .map(MaintenanceComponent::valueOf)
                            .collect(Collectors.toSet());

                    List<String> parts = new ArrayList<>();
                    String partsStr = getValue(line, partsIdx);
                    if (partsStr != null) {
                        parts = Arrays.stream(partsStr.split("[,;]"))
                                .map(String::trim)
                                .collect(Collectors.toList());
                    }

                    String description = getValue(line, descIdx);
                    Integer expectedDuration = Integer.parseInt(getValue(line, durIdx));
                    String notes = getValue(line, notesIdx);
                    BigDecimal cost = new BigDecimal(getValue(line, costIdx));

                    CreateMaintenanceRecordRequest request = new CreateMaintenanceRecordRequest(
                            description,
                            date,
                            expectedDuration,
                            parts,
                            notes,
                            templateName,
                            status,
                            aircraftRegistration,
                            components,
                            cost
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

    private String getValue(String[] line, int index) {
        if (index == -1 || index >= line.length) return null;
        String val = line[index].trim();
        return val.isEmpty() ? null : val;
    }
}
