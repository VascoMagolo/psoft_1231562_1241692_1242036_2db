package aisafe.aircrafts.application;

import aisafe.aircrafts.application.dtos.AircraftModelResponse;
import aisafe.aircrafts.application.dtos.RegisterAircraftModelRequest;
import aisafe.aircrafts.domain.Manufacturer;
import aisafe.shared.application.UseCase;
import aisafe.shared.application.dtos.BulkImportResult;
import com.opencsv.CSVReader;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;

@UseCase
public class ImportAircraftModelsUseCase {

    private final RegisterAircraftModelUseCase registerUseCase;

    public ImportAircraftModelsUseCase(RegisterAircraftModelUseCase registerUseCase) {
        this.registerUseCase = registerUseCase;
    }

    public BulkImportResult<AircraftModelResponse> execute(MultipartFile file) {
        BulkImportResult<AircraftModelResponse> result = new BulkImportResult<>();
        try (Reader reader = new InputStreamReader(file.getInputStream());
             CSVReader csvReader = new CSVReader(reader)) {

            String[] header = csvReader.readNext();
            if (header == null) {
                return result;
            }

            int rowNumber = 1; // Header is row 1
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                rowNumber++;
                try {
                    if (line.length < 6) {
                        throw new IllegalArgumentException("Invalid number of columns");
                    }
                    // modelName,manufacturer,maximumTakeoffWeight,maximumPayloadCapacity,cruiseSpeed,maximumSeatCapacity
                    String modelName = line[0];
                    String manufacturerStr = line[1];
                    String maxTakeoffWeightStr = line[2];
                    String maxPayloadStr = line[3];
                    String cruiseSpeedStr = line[4];
                    String maxSeatCapacityStr = line[5];

                    RegisterAircraftModelRequest request = new RegisterAircraftModelRequest(
                            modelName,
                            Manufacturer.valueOf(manufacturerStr.toUpperCase()),
                            Double.parseDouble(maxTakeoffWeightStr), // Used as maxRange
                            Double.parseDouble(maxPayloadStr), // Used as fuelCapacity
                            Double.parseDouble(cruiseSpeedStr),
                            Integer.parseInt(maxSeatCapacityStr),
                            null,
                            null
                    );

                    AircraftModelResponse response = registerUseCase.execute(request);
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
