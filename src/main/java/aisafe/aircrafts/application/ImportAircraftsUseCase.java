package aisafe.aircrafts.application;

import aisafe.aircrafts.application.dtos.RegisterAircraftRequest;
import aisafe.aircrafts.application.dtos.ViewAircraftDetailsResponse;
import aisafe.shared.application.UseCase;
import aisafe.shared.application.dtos.BulkImportResult;
import com.opencsv.CSVReader;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@UseCase
public class ImportAircraftsUseCase {

    private final RegisterAircraftUseCase registerUseCase;

    public ImportAircraftsUseCase(RegisterAircraftUseCase registerUseCase) {
        this.registerUseCase = registerUseCase;
    }

    public BulkImportResult<ViewAircraftDetailsResponse> execute(MultipartFile file) {
        BulkImportResult<ViewAircraftDetailsResponse> result = new BulkImportResult<>();
        try (Reader reader = new InputStreamReader(file.getInputStream());
             CSVReader csvReader = new CSVReader(reader)) {

            String[] header = csvReader.readNext();
            if (header == null) return result;

            int rowNumber = 1;
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                rowNumber++;
                try {
                    if (line.length < 6) {
                        throw new IllegalArgumentException("Invalid number of columns");
                    }
                    // registrationNumber,modelName,status,manufacturingDate,range,seatCapacity,features
                    String regNum = line[0];
                    String modelName = line[1];
                    String status = line[2];
                    String mfgDateStr = line[3];
                    String rangeStr = line[4];
                    String seatCapStr = line[5];
                    String featuresStr = line.length > 6 ? line[6] : "";

                    List<String> features = featuresStr.isBlank() ? List.of() : Arrays.asList(featuresStr.split(";"));

                    RegisterAircraftRequest request = new RegisterAircraftRequest(
                            regNum,
                            modelName,
                            LocalDate.parse(mfgDateStr),
                            Integer.parseInt(seatCapStr),
                            Double.parseDouble(rangeStr),
                            status,
                            features
                    );

                    ViewAircraftDetailsResponse response = registerUseCase.execute(request);
                    result.addSuccess(response);
                } catch (Exception e) {
                    result.addError(rowNumber, String.join(",", line), e.getMessage() != null ? e.getMessage() : "Unknown error");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to process CSV file", e);
        }
        return result;
    }
}
