package aisafe.aircrafts.infrastructure;

import aisafe.aircrafts.application.ViewAircraftMoodsUseCase;
import aisafe.aircrafts.application.dtos.AircraftMoodResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * {@code GET /api/v2/unicorns}: the mood of every aircraft (the mock endpoint of Assignment 2). A
 * query of the CQRS read side: the {@code X-Query-Side} header says where the answer came from,
 * {@code read-model} (this replica's read model, kept by the events) or {@code peers} (events off:
 * gathered from the replicas, as in Assignment 1).
 */
@RestController
@RequestMapping("/api/v2/unicorns")
@Tag(name = "Unicorns", description = "Mood of the aircraft (Assignment 2 mock endpoint)")
public class UnicornController {

    private final ViewAircraftMoodsUseCase viewMoods;
    private final String querySide;

    public UnicornController(ViewAircraftMoodsUseCase viewMoods,
                             @Value("${aisafe.events.enabled:false}") boolean eventsEnabled) {
        this.viewMoods = viewMoods;
        this.querySide = eventsEnabled ? "read-model" : "peers";
    }

    @Operation(summary = "Mood of every aircraft",
            description = "SLEEPY (inactive), GRUMPY (in maintenance), EXCITED (in flight), ANXIOUS (maintenance planned) "
                    + "or HAPPY. Read only from the query side (CQRS): it never touches the write model.")
    @GetMapping
    public ResponseEntity<List<AircraftMoodResponse>> moods() {
        return ResponseEntity.ok().header("X-Query-Side", querySide).body(viewMoods.execute());
    }
}
