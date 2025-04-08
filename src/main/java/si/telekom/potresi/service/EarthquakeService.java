package si.telekom.potresi.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import si.telekom.potresi.client.EarthquakeClient;
import si.telekom.potresi.client.WeatherClient;
import si.telekom.potresi.dto.EarthquakeRecordDTO;
import si.telekom.potresi.dto.WeatherInfoDTO;

import java.util.List;
import java.util.function.Supplier;

@Service
public class EarthquakeService {

    private static final Logger log = LoggerFactory.getLogger(EarthquakeService.class);

    private final EarthquakeClient earthquakeClient;
    private final WeatherClient weatherClient;

    private List<EarthquakeRecordDTO> cachedWeeklyWorst;
    private List<EarthquakeRecordDTO> cachedMonthlyWorst;
    private EarthquakeRecordDTO cachedLastEarthquake;

    public EarthquakeService(EarthquakeClient earthquakeClient, WeatherClient weatherClient) {
        this.earthquakeClient = earthquakeClient;
        this.weatherClient = weatherClient;
    }

    public List<EarthquakeRecordDTO> getWorstEarthquakeLastWeek() {
        log.info("Getting cached weekly worst earthquakes.");
        List<EarthquakeRecordDTO> live = earthquakeClient.getWorstEarthquakeInPeriod(7);
        if (live != null && !live.isEmpty()) {
            cachedWeeklyWorst = live;
            return live;
        }
        log.warn("Using cached weekly data.");
        return cachedWeeklyWorst != null ? cachedWeeklyWorst : List.of();
    }

    public List<EarthquakeRecordDTO> getWorstEarthquakeLastMonth() {
        log.info("Getting cached monthly worst earthquakes.");
        List<EarthquakeRecordDTO> live = earthquakeClient.getWorstEarthquakeInPeriod(30);
        if (live != null && !live.isEmpty()) {
            cachedMonthlyWorst = live;
            return live;
        }
        log.warn("Using cached monthly data.");
        return cachedMonthlyWorst != null ? cachedMonthlyWorst : List.of();
    }

    public EarthquakeRecordDTO getLastEarthquakeWithWeather() {
        log.info("Getting most recent earthquake with weather.");
        EarthquakeRecordDTO live = earthquakeClient.getMostRecentEarthquake();

        if (live == null) {
            log.warn("Using cached last earthquake.");
            return cachedLastEarthquake;
        }

        try {
            double lat = live.getLocation().getLatitude();
            double lon = live.getLocation().getLongitude();
            WeatherInfoDTO weather = weatherClient.getCurrentWeather(lat, lon);
            live.setWeather(weather);
        } catch (Exception ex) {
            log.error("Failed to fetch weather info: {}", ex.getMessage());
        }

        cachedLastEarthquake = live;
        return live;
    }


    @Scheduled(fixedRate = 30 * 60 * 1000) // every 30 minutes
    public void refreshCache() {
        log.info("Scheduled cache refresh started.");

        List<EarthquakeRecordDTO> weekly = earthquakeClient.getWorstEarthquakeInPeriod(7);
        if (weekly != null && !weekly.isEmpty()) cachedWeeklyWorst = weekly;

        List<EarthquakeRecordDTO> monthly = earthquakeClient.getWorstEarthquakeInPeriod(30);
        if (monthly != null && !monthly.isEmpty()) cachedMonthlyWorst = monthly;

        EarthquakeRecordDTO last = earthquakeClient.getMostRecentEarthquake();
        if (last != null) {
            try {
                WeatherInfoDTO weather = weatherClient.getCurrentWeather(
                        last.getLocation().getLatitude(),
                        last.getLocation().getLongitude());
                last.setWeather(weather);
            } catch (Exception e) {
                log.error("Weather fetch failed in scheduled task: {}", e.getMessage());
            }
            cachedLastEarthquake = last;
        }

        log.info("Scheduled cache refresh completed.");
    }
}
