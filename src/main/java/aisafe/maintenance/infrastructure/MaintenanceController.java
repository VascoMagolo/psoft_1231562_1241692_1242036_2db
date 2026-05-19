package aisafe.maintenance.infrastructure;

import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.maintenance.application.*;
import aisafe.maintenance.application.dtos.*;
import aisafe.maintenance.domain.MaintenancePart;
import aisafe.maintenance.domain.MaintenanceRecord;
import aisafe.maintenance.domain.MaintenanceTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceController {
    private final CreateMaintenanceTemplateUseCase createMaintenanceTemplateUseCase;
    private final CreateMaintenanceRecordUseCase createMaintenanceRecordUseCase;
    private final CreateMaintenancePartUseCase createMaintenancePartUseCase;
    private final UpdateMaintenanceRecordUseCase updateMaintenanceRecordUseCase;
    private final ViewAllMaintenanceRecordsUseCase viewAllMaintenanceRecordsUseCase;
    private final ViewTotalMaintenanceHoursInFleetUseCase viewTotalMaintenanceHoursInFleetUseCase;

    public MaintenanceController(CreateMaintenanceTemplateUseCase createMaintenanceTemplateUseCase, CreateMaintenanceRecordUseCase createMaintenanceRecordUseCase, CreateMaintenancePartUseCase createMaintenancePartUseCase, UpdateMaintenanceRecordUseCase updateMaintenanceRecordUseCase, ViewAllMaintenanceRecordsUseCase viewAllMaintenanceRecordsUseCase, ViewTotalMaintenanceHoursInFleetUseCase viewTotalMaintenanceHoursInFleetUseCase) {
        this.createMaintenanceTemplateUseCase = createMaintenanceTemplateUseCase;
        this.createMaintenanceRecordUseCase = createMaintenanceRecordUseCase;
        this.createMaintenancePartUseCase = createMaintenancePartUseCase;
        this.updateMaintenanceRecordUseCase = updateMaintenanceRecordUseCase;
        this.viewAllMaintenanceRecordsUseCase = viewAllMaintenanceRecordsUseCase;
        this.viewTotalMaintenanceHoursInFleetUseCase = viewTotalMaintenanceHoursInFleetUseCase;
    }

    @PostMapping("/templates")
    public ResponseEntity<MaintenanceTemplate> createMaintenanceTemplate(@RequestBody CreateMaintenanceTemplateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createMaintenanceTemplateUseCase.execute(request));
    }

    @PostMapping("/records")
    public ResponseEntity<MaintenanceRecord> createMaintenanceRecord(@RequestBody CreateMaintenanceRecordRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createMaintenanceRecordUseCase.execute(request));
    }

    @PostMapping("/parts")
    public ResponseEntity<MaintenancePart> createMaintenancePart(@RequestBody CreateMaintenancePartRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createMaintenancePartUseCase.execute(request));
    }

    @PatchMapping("/records/update")
    public ResponseEntity<MaintenanceRecord> updateRecordStatusAndNotes(
            @RequestBody UpdateMaintenanceRecordsRequest request) {
        return ResponseEntity.ok(updateMaintenanceRecordUseCase.execute(request));
    }

    @GetMapping("/records/{registrationNumber}")
    public ResponseEntity<List<ViewAllMaintenanceRecordsResponse>> getAllMaintenanceRecordsForAircraft(
            @PathVariable RegistrationNumber registrationNumber) {
        List<ViewAllMaintenanceRecordsResponse> records = viewAllMaintenanceRecordsUseCase.execute(registrationNumber);
        return ResponseEntity.ok(records);
    }

    @GetMapping("/records/hours")
    public ResponseEntity<ViewTotalMaintenanceHoursinFleetResponse> getTotalMaintenanceHoursInFleet() {
        ViewTotalMaintenanceHoursinFleetResponse totalHours  = viewTotalMaintenanceHoursInFleetUseCase.execute();
        return ResponseEntity.ok(totalHours);
    }
}
