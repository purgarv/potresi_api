package si.telekom.potresi.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import si.telekom.potresi.dto.EarthquakeRecordDTO;
import si.telekom.potresi.service.EarthquakeService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/potresi")
public class EarthquakeController {

    private static final Logger log = LoggerFactory.getLogger(EarthquakeController.class);

    private final EarthquakeService earthquakeService;

    public EarthquakeController(EarthquakeService earthquakeService) {
        this.earthquakeService = earthquakeService;
    }

    /**
     * Returns the strongest earthquake in the last 7 days.
     *
     * @return a list of EarthquakeRecordDTO or an error message
     */
    @GetMapping("/rekordi/tedenski")
    public ResponseEntity<?> getWorstWeeklyEarthquake() {
        log.info("Request received: GET /potresi/rekordi/tedenski");

        List<EarthquakeRecordDTO> records = earthquakeService.getWorstEarthquakeLastWeek();

        if (records != null && !records.isEmpty()) {
            log.info("Returning {} weekly earthquake record(s).", records.size());
            return ResponseEntity.ok(records);
        } else {
            log.warn("No weekly earthquake records found.");
            return ResponseEntity.status(503).body(Map.of("error", "Could not fetch the worst earthquake data for the last week."));
        }
    }

    /**
     * Returns the strongest earthquake in the last 30 days.
     *
     * @return a list of EarthquakeRecordDTO or an error message
     */
    @GetMapping("/rekordi/mesecni")
    public ResponseEntity<?> getWorstMonthlyEarthquake() {
        log.info("Request received: GET /potresi/rekordi/mesecni");

        List<EarthquakeRecordDTO> records = earthquakeService.getWorstEarthquakeLastMonth();

        if (records != null && !records.isEmpty()) {
            log.info("Returning {} monthly earthquake record(s).", records.size());
            return ResponseEntity.ok(records);
        } else {
            log.warn("No monthly earthquake records found.");
            return ResponseEntity.status(503).body(Map.of("error", "Could not fetch the worst earthquake data for the last month."));
        }
    }

    /**
     * Returns the most recent earthquake along with related weather data.
     *
     * @return an EarthquakeRecordDTO or an error message
     */
    @GetMapping("/zadnji")
    public ResponseEntity<?> getMostRecentEarthquakeWithWeather() {
        log.info("Request received: GET /potresi/zadnji");

        EarthquakeRecordDTO record = earthquakeService.getLastEarthquakeWithWeather();

        if (record != null) {
            log.info("Returning most recent earthquake record: {}", record);
            return ResponseEntity.ok(record);
        } else {
            log.warn("No recent earthquake record found.");
            return ResponseEntity.status(503).body(Map.of("error", "Could not fetch the most recent earthquake data."));
        }
    }
}
