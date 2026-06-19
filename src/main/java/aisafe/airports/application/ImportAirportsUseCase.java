package aisafe.airports.application;

import aisafe.airports.application.dtos.RegisterAirportRequest;
import aisafe.airports.domain.Airport;
import aisafe.shared.application.UseCase;
import aisafe.shared.application.dtos.BulkImportResult;
import aisafe.shared.domain.DomainException;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

@UseCase
@RequiredArgsConstructor
public class ImportAirportsUseCase {

    private final RegisterAirportUseCase registerAirportUseCase;

    public BulkImportResult<String> execute(MultipartFile file) {
        BulkImportResult<String> result = new BulkImportResult<>();

        try (Reader reader = new InputStreamReader(file.getInputStream());
             CSVReader csvReader = new CSVReader(reader)) {

            String[] header = csvReader.readNext();
            if (header == null) {
                result.addError(0, "File", "CSV file is empty");
                return result;
            }

            // Map headers to indexes
            int iataIndex = -1, nameIndex = -1, cityIndex = -1, countryIndex = -1, regionIndex = -1, tzIndex = -1, latIndex = -1, lonIndex = -1;
            for (int i = 0; i < header.length; i++) {
                String col = header[i].trim().toLowerCase();
                switch (col) {
                    case "iatacode": iataIndex = i; break;
                    case "name": nameIndex = i; break;
                    case "city": cityIndex = i; break;
                    case "country": countryIndex = i; break;
                    case "region": regionIndex = i; break;
                    case "timezone": tzIndex = i; break;
                    case "latitude": latIndex = i; break;
                    case "longitude": lonIndex = i; break;
                }
            }

            if (iataIndex == -1 || nameIndex == -1 || cityIndex == -1 || countryIndex == -1) {
                result.addError(0, "Headers", "Missing required columns (iataCode, name, city, country)");
                return result;
            }

            String[] line;
            int rowNumber = 1;

            while ((line = csvReader.readNext()) != null) {
                rowNumber++;
                try {
                    String iata = getValue(line, iataIndex);
                    String name = getValue(line, nameIndex);
                    String city = getValue(line, cityIndex);
                    String country = getValue(line, countryIndex);
                    String region = getValue(line, regionIndex);
                    String timezone = getValue(line, tzIndex);
                    Double lat = parseDouble(getValue(line, latIndex));
                    Double lon = parseDouble(getValue(line, lonIndex));

                    RegisterAirportRequest request = new RegisterAirportRequest(
                            iata, name, city, country, region, timezone, lat, lon,
                            List.of(new RegisterAirportRequest.RunwayRequest("01/19", 2000, "010/190")),
                            null, null, null, List.of(), List.of(), List.of()
                    );
                    
                    // Attempt to register
                    registerAirportUseCase.execute(request);
                    result.addSuccess(iata);
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
}
