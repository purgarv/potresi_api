package si.telekom.potresi.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import si.telekom.potresi.config.EarthquakeApiConfig;
import si.telekom.potresi.dto.EarthquakeRecordDTO;
import si.telekom.potresi.dto.GeoLocationDTO;

import java.util.ArrayList;
import java.util.List;
import java.time.Instant;

@Component
public class EarthquakeClient {

    private static final Logger log = LoggerFactory.getLogger(EarthquakeClient.class);
    private final RestTemplate restTemplate;
    private final EarthquakeApiConfig config;

    public EarthquakeClient(RestTemplate restTemplate, EarthquakeApiConfig config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    /**
     * Fetches the strongest earthquake(s) in the given period.
     * Applies circuit breaker and retry mechanisms.
     *
     * @param days number of past days to search in
     * @return list of strongest earthquakes (could be more than one if tied)
     */
    @CircuitBreaker(name = "earthquakeApi")
    @Retry(name = "earthquakeApi", fallbackMethod = "fallbackWorst")
    public List<EarthquakeRecordDTO> getWorstEarthquakeInPeriod(int days) {
        String feed = getFeedNameForDays(days);
        String url = config.getBaseUrl() + feed;

        log.info("Requesting earthquake data from: {}", url);
        String response = this.restTemplate.getForObject(url, String.class);

        JSONObject json = new JSONObject(response);
        JSONArray features = json.getJSONArray("features");

        if (features.length() == 0) {
            log.info("No earthquake data found in the response.");
            return List.of();
        }

        double maxMag = Double.MIN_VALUE;
        List<JSONObject> strongest = new ArrayList<>();

        // Iterate through each feature and find the strongest earthquakes
        for (int i = 0; i < features.length(); i++) {
            JSONObject feature = features.getJSONObject(i);
            JSONObject properties = feature.getJSONObject("properties");

            if (!properties.has("mag") || properties.isNull("mag")) continue;

            double mag = properties.getDouble("mag");

            if (mag > maxMag) {
                maxMag = mag;
                strongest.clear();
                strongest.add(feature);
            } else if (mag == maxMag) {
                strongest.add(feature);
            }
        }

        log.info("Found {} strongest earthquake(s) with magnitude {}", strongest.size(), maxMag);
        return strongest.stream().map(this::mapToEarthquakeRecord).toList();
    }

    /**
     * Retrieves the most recent earthquake event available in the feeds.
     * Tries multiple feeds until one with data is found.
     *
     * @return most recent EarthquakeRecordDTO or null if none found
     */
    @CircuitBreaker(name = "earthquakeApi")
    @Retry(name = "earthquakeApi", fallbackMethod = "fallbackMostRecent")
    public EarthquakeRecordDTO getMostRecentEarthquake() {
        String[] feeds = config.getFeed().keySet().toArray(new String[0]);

        for (String feed : feeds) {
            String url = config.getBaseUrl() + config.getFeed().get(feed);
            log.info("Attempting to fetch recent earthquakes from feed: {}", feed);

            String response = this.restTemplate.getForObject(url, String.class);
            JSONObject json = new JSONObject(response);
            JSONArray features = json.getJSONArray("features");

            if (features.length() == 0) {
                log.debug("No data found in feed: {}", feed);
                continue;
            }

            // Find the most recent earthquake by comparing timestamps
            JSONObject mostRecent = features.getJSONObject(0);
            long latestTime = mostRecent.getJSONObject("properties").optLong("time", 0);

            for (int i = 1; i < features.length(); i++) {
                JSONObject current = features.getJSONObject(i);
                long currentTime = current.getJSONObject("properties").optLong("time", 0);
                if (currentTime > latestTime) {
                    mostRecent = current;
                    latestTime = currentTime;
                }
            }

            EarthquakeRecordDTO record = mapToEarthquakeRecord(mostRecent);
            record.setValidAt(Instant.now());
            log.info("Most recent earthquake found: {}", record);
            return record;
        }

        log.warn("No recent earthquakes found in any feed.");
        return null;
    }

    /**
     * Maps a JSONObject representing an earthquake to a DTO.
     */
    private EarthquakeRecordDTO mapToEarthquakeRecord(JSONObject feature) {
        JSONObject properties = feature.getJSONObject("properties");
        JSONObject geometry = feature.getJSONObject("geometry");
        JSONArray coordinates = geometry.getJSONArray("coordinates");

        double longitude = coordinates.getDouble(0);
        double latitude = coordinates.getDouble(1);
        double depth = coordinates.getDouble(2);

        String place = properties.optString("place", "Unknown location");

        EarthquakeRecordDTO record = new EarthquakeRecordDTO(place, new GeoLocationDTO(latitude, longitude), depth, Instant.now());
        log.debug("Mapped EarthquakeRecordDTO: {}", record);
        return record;
    }

    /**
     * Determines which feed to use based on the number of days.
     */
    private String getFeedNameForDays(int days) {
        if (days <= 1) return config.getFeed().get("daily");
        if (days <= 7) return config.getFeed().get("weekly");
        return config.getFeed().get("monthly");
    }

    /**
     * Fallback method when getWorstEarthquakeInPeriod fails.
     */
    public List<EarthquakeRecordDTO> fallbackWorst(int days, Throwable t) {
        log.warn("Fallback for getWorstEarthquakeInPeriod triggered", t);
        return List.of();
    }

    /**
     * Fallback method when getMostRecentEarthquake fails.
     */
    public EarthquakeRecordDTO fallbackMostRecent(Throwable t) {
        log.warn("Fallback for getMostRecentEarthquake triggered", t);
        return null;
    }
}
