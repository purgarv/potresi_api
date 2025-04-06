package si.telekom.potresi.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import si.telekom.potresi.client.EarthquakeClient;
import si.telekom.potresi.client.WeatherClient;
import si.telekom.potresi.dto.EarthquakeRecordDTO;
import si.telekom.potresi.dto.EarthquakeRecordWithWeatherDTO;
import si.telekom.potresi.dto.WeatherInfoDTO;

import java.util.function.Supplier;

@Service
public class EarthquakeService {

    private static final Logger log = LoggerFactory.getLogger(EarthquakeService.class);

    private final EarthquakeClient earthquakeClient;
    private final WeatherClient weatherClient;

    private EarthquakeRecordDTO cachedWeeklyWorst;
    private EarthquakeRecordDTO cachedMonthlyWorst;
    private EarthquakeRecordWithWeatherDTO cachedLastEarthquake;

    public EarthquakeService(EarthquakeClient earthquakeClient, WeatherClient weatherClient) {
        this.earthquakeClient = earthquakeClient;
        this.weatherClient = weatherClient;
    }

    public EarthquakeRecordDTO getWorstEarthquakeLastWeek() {
        log.info("Getting cached weekly worst earthquake.");
        EarthquakeRecordDTO live = safeFetch(() -> earthquakeClient.getWorstEarthquakeInPeriod(7));
        if (live != null) {
            cachedWeeklyWorst = live;
            return live;
        }
        log.warn("Using cached weekly data.");
        return cachedWeeklyWorst;
    }

    public EarthquakeRecordDTO getWorstEarthquakeLastMonth() {
        log.info("Getting cached monthly worst earthquake.");
        EarthquakeRecordDTO live = safeFetch(() -> earthquakeClient.getWorstEarthquakeInPeriod(30));
        if (live != null) {
            cachedMonthlyWorst = live;
            return live;
        }
        log.warn("Using cached monthly data.");
        return cachedMonthlyWorst;
    }

    public EarthquakeRecordWithWeatherDTO getLastEarthquakeWithWeather() {
        log.info("Getting most recent earthquake with weather.");
        EarthquakeRecordWithWeatherDTO quake = safeFetch(earthquakeClient::getMostRecentEarthquake);

        if (quake == null) {
            log.warn("Using cached last earthquake.");
            return cachedLastEarthquake;
        }

        try {
            double lat = quake.getLocation().getLatitude();
            double lon = quake.getLocation().getLongitude();
            WeatherInfoDTO weather = weatherClient.getCurrentWeather(lat, lon);
            quake.setWeatherInfo(weather);
        } catch (Exception ex) {
            log.error("Failed to fetch weather info: {}", ex.getMessage());
        }

        cachedLastEarthquake = quake;
        return quake;
    }

    private <T extends EarthquakeRecordDTO> T safeFetch(Supplier<T> function) {
        try {
            return function.get();
        } catch (Exception e) {
            log.error("Fetch failed: {}", e.getMessage());
            return null;
        }
    }

    @Scheduled(fixedRate = 30 * 60 * 1000) // every 30 minutes
    public void refreshCache() {
        log.info("Scheduled cache refresh started.");

        EarthquakeRecordDTO weekly = safeFetch(() -> earthquakeClient.getWorstEarthquakeInPeriod(7));
        if (weekly != null) cachedWeeklyWorst = weekly;

        EarthquakeRecordDTO monthly = safeFetch(() -> earthquakeClient.getWorstEarthquakeInPeriod(30));
        if (monthly != null) cachedMonthlyWorst = monthly;

        EarthquakeRecordWithWeatherDTO last = safeFetch(earthquakeClient::getMostRecentEarthquake);
        if (last != null) {
            try {
                WeatherInfoDTO weather = weatherClient.getCurrentWeather(
                        last.getLocation().getLatitude(),
                        last.getLocation().getLongitude());
                last.setWeatherInfo(weather);
            } catch (Exception e) {
                log.error("Weather fetch failed in scheduled task: {}", e.getMessage());
            }
            cachedLastEarthquake = last;
        }

        log.info("Scheduled cache refresh completed.");
    }
}
