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

/**
 * Service layer for managing earthquake-related operations.
 * Retrieves data via clients, includes weather info, and has simple in-memory caching.
 */
@Service
public class EarthquakeService {

    private static final Logger log = LoggerFactory.getLogger(EarthquakeService.class);

    private final EarthquakeClient earthquakeClient;
    private final WeatherClient weatherClient;

    // Cached values for fallback or performance
    private List<EarthquakeRecordDTO> cachedWeeklyWorst;
    private List<EarthquakeRecordDTO> cachedMonthlyWorst;
    private EarthquakeRecordDTO cachedLastEarthquake;

    public EarthquakeService(EarthquakeClient earthquakeClient, WeatherClient weatherClient) {
        this.earthquakeClient = earthquakeClient;
        this.weatherClient = weatherClient;
    }

    /**
     * Retrieves the worst earthquake(s) in the last 7 days, with fallback to cached data.
     */
    public List<EarthquakeRecordDTO> getWorstEarthquakeLastWeek() {
        log.info("Fetching worst earthquake(s) from the past week.");
        List<EarthquakeRecordDTO> live = earthquakeClient.getWorstEarthquakeInPeriod(7);

        if (live != null && !live.isEmpty()) {
            cachedWeeklyWorst = live;
            return live;
        }

        log.warn("Falling back to cached weekly earthquake data.");
        return cachedWeeklyWorst != null ? cachedWeeklyWorst : List.of();
    }

    /**
     * Retrieves the worst earthquake(s) in the last 30 days, with fallback to cached data.
     */
    public List<EarthquakeRecordDTO> getWorstEarthquakeLastMonth() {
        log.info("Fetching worst earthquake(s) from the past month.");
        List<EarthquakeRecordDTO> live = earthquakeClient.getWorstEarthquakeInPeriod(30);

        if (live != null && !live.isEmpty()) {
            cachedMonthlyWorst = live;
            return live;
        }

        log.warn("Falling back to cached monthly earthquake data.");
        return cachedMonthlyWorst != null ? cachedMonthlyWorst : List.of();
    }

    /**
     * Retrieves the most recent earthquake and includes weather data.
     * Falls back to a cached version if live fetch fails.
     */
    public EarthquakeRecordDTO getLastEarthquakeWithWeather() {
        log.info("Fetching most recent earthquake with weather data.");
        EarthquakeRecordDTO live = earthquakeClient.getMostRecentEarthquake();

        if (live == null) {
            log.warn("Falling back to cached most recent earthquake.");
            return cachedLastEarthquake;
        }

        try {
            double lat = live.getLocation().getLatitude();
            double lon = live.getLocation().getLongitude();
            WeatherInfoDTO weather = weatherClient.getCurrentWeather(lat, lon);
            live.setWeather(weather);
            log.debug("Enriched earthquake with weather info: {}", weather);
        } catch (Exception ex) {
            log.error("Failed to fetch weather info for earthquake: {}", ex.getMessage());
        }

        cachedLastEarthquake = live;
        return live;
    }

    /**
     * Scheduled task that refreshes all earthquake data and updates internal caches.
     * Runs every 30 minutes.
     */
    @Scheduled(fixedRate = 30 * 60 * 1000)
    public void refreshCache() {
        log.info("Scheduled cache refresh started.");

        // Refresh weekly worst earthquakes
        List<EarthquakeRecordDTO> weekly = earthquakeClient.getWorstEarthquakeInPeriod(7);
        if (weekly != null && !weekly.isEmpty()) {
            cachedWeeklyWorst = weekly;
            log.info("Weekly cache refreshed with {} records.", weekly.size());
        }

        // Refresh monthly worst earthquakes
        List<EarthquakeRecordDTO> monthly = earthquakeClient.getWorstEarthquakeInPeriod(30);
        if (monthly != null && !monthly.isEmpty()) {
            cachedMonthlyWorst = monthly;
            log.info("Monthly cache refreshed with {} records.", monthly.size());
        }

        // Refresh most recent earthquake with weather
        EarthquakeRecordDTO last = earthquakeClient.getMostRecentEarthquake();
        if (last != null) {
            try {
                WeatherInfoDTO weather = weatherClient.getCurrentWeather(
                        last.getLocation().getLatitude(),
                        last.getLocation().getLongitude());
                last.setWeather(weather);
                log.debug("Updated cached earthquake with new weather info.");
            } catch (Exception e) {
                log.error("Weather fetch failed during scheduled refresh: {}", e.getMessage());
            }
            cachedLastEarthquake = last;
        }

        log.info("Scheduled cache refresh completed.");
    }
}
