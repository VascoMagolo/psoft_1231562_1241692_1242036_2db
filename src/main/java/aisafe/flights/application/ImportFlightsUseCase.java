package aisafe.flights.application;

import aisafe.flights.application.dtos.ScheduleFlightRequest;
import aisafe.shared.application.UseCase;
import aisafe.shared.application.dtos.BulkImportResult;
import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.time.OffsetDateTime;

@UseCase
@RequiredArgsConstructor
public class ImportFlightsUseCase {

    private final ScheduleFlightUseCase scheduleFlightUseCase;

    public BulkImportResult<String> execute(MultipartFile file) {
        BulkImportResult<String> result = new BulkImportResult<>();

        try (Reader reader = new InputStreamReader(file.getInputStream());
             CSVReader csvReader = new CSVReader(reader)) {

            String[] header = csvReader.readNext();
            if (header == null) {
                result.addError(0, "File", "CSV file is empty");
                return result;
            }

            int flightNumberIndex = -1, originIndex = -1, destIndex = -1, aircraftIndex = -1, depIndex = -1, arrIndex = -1, statusIndex = -1;
            for (int i = 0; i < header.length; i++) {
                String col = header[i].trim().toLowerCase();
                switch (col) {
                    case "flightnumber": flightNumberIndex = i; break;
                    case "routeorigin": originIndex = i; break;
                    case "routedestination": destIndex = i; break;
                    case "aircraftregistration": aircraftIndex = i; break;
                    case "departuredate": depIndex = i; break;
                    case "arrivaldate": arrIndex = i; break;
                    case "status": statusIndex = i; break;
                }
            }

            if (originIndex == -1 || destIndex == -1 || aircraftIndex == -1 || depIndex == -1 || arrIndex == -1) {
                result.addError(0, "Headers", "Missing required columns (routeOrigin, routeDestination, aircraftRegistration, departureDate, arrivalDate)");
                return result;
            }

            String[] line;
            int rowNumber = 1;

            while ((line = csvReader.readNext()) != null) {
                rowNumber++;
                try {
                    String origin = getValue(line, originIndex);
                    String dest = getValue(line, destIndex);
                    String aircraft = getValue(line, aircraftIndex);
                    OffsetDateTime departureDate = parseDate(getValue(line, depIndex));
                    OffsetDateTime arrivalDate = parseDate(getValue(line, arrIndex));
                    String flightNumber = getValue(line, flightNumberIndex); // May be null

                    if (departureDate == null || arrivalDate == null) {
                        throw new IllegalArgumentException("Invalid date format. Expected ISO-8601.");
                    }

                    ScheduleFlightRequest request = new ScheduleFlightRequest(
                            aircraft, origin, dest, departureDate, arrivalDate
                    );

                    scheduleFlightUseCase.execute(request);
                    String identifier = (flightNumber != null && !flightNumber.isEmpty()) ? flightNumber : (aircraft + "-" + origin + "-" + dest);
                    result.addSuccess(identifier);
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

    private OffsetDateTime parseDate(String val) {
        if (val == null) return null;
        try {
            return OffsetDateTime.parse(val);
        } catch (Exception e) {
            return null;
        }
    }
}
