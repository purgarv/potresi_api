package si.telekom.potresi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import si.telekom.potresi.dto.EarthquakeRecordDTO;
import si.telekom.potresi.dto.EarthquakeRecordWithWeatherDTO;
import si.telekom.potresi.service.EarthquakeService;

import java.util.Map;

@RestController
@RequestMapping("/potresi")
public class EarthquakeController {

    private final EarthquakeService earthquakeService;

    public EarthquakeController(EarthquakeService earthquakeService) {
        this.earthquakeService = earthquakeService;
    }

    @GetMapping("/rekordi/tedenski")
    public ResponseEntity<?> getWorstWeeklyEarthquake() {
        EarthquakeRecordDTO record = earthquakeService.getWorstEarthquakeLastWeek();
        return record != null
                ? ResponseEntity.ok(record)
                : ResponseEntity.status(503).body(Map.of("error", "Could not fetch the worst earthquake data for the last week."));
    }

    @GetMapping("/rekordi/mesecni")
    public ResponseEntity<?> getWorstMonthlyEarthquake() {
        EarthquakeRecordDTO record = earthquakeService.getWorstEarthquakeLastMonth();
        return record != null
                ? ResponseEntity.ok(record)
                : ResponseEntity.status(503).body(Map.of("error", "Could not fetch the worst earthquake data for the last month."));
    }

    @GetMapping("/zadnji")
    public ResponseEntity<?> getMostRecentEarthquakeWithWeather() {
        EarthquakeRecordWithWeatherDTO record = earthquakeService.getLastEarthquakeWithWeather();
        return record != null
                ? ResponseEntity.ok(record)
                : ResponseEntity.status(503).body(Map.of("error", "Could not fetch the most recent earthquake data."));
    }
}
