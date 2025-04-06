package si.telekom.potresi.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import si.telekom.potresi.dto.EarthquakeRecordDTO;
import si.telekom.potresi.dto.EarthquakeRecordWithWeatherDTO;
import si.telekom.potresi.dto.GeoLocationDTO;

@Component
public class EarthquakeClient {

    private static final Logger log = LoggerFactory.getLogger(EarthquakeClient.class);
    private final RestTemplate restTemplate;

    public EarthquakeClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private static final String BASE_FEED_URL = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/";

    @Retry(name = "earthquakeApi")
    @CircuitBreaker(name = "earthquakeApi", fallbackMethod = "fallbackWorst")
    public EarthquakeRecordDTO getWorstEarthquakeInPeriod(int days) {
        String feed = getFeedNameForDays(days);
        String url = BASE_FEED_URL + feed;

        String response = this.restTemplate.getForObject(url, String.class);
        JSONObject json = new JSONObject(response);
        JSONArray features = json.getJSONArray("features");

        if (features.isEmpty()) return null;

        JSONObject strongest = null;
        double maxMag = Double.MIN_VALUE;

        for (int i = 0; i < features.length(); i++) {
            JSONObject feature = features.getJSONObject(i);
            JSONObject properties = feature.getJSONObject("properties");

            if (!properties.has("mag") || properties.isNull("mag")) continue;

            double mag = properties.getDouble("mag");
            if (mag > maxMag) {
                maxMag = mag;
                strongest = feature;
            }
        }

        return strongest != null ? mapToEarthquakeRecord(strongest) : null;
    }

    @Retry(name = "earthquakeApi")
    @CircuitBreaker(name = "earthquakeApi", fallbackMethod = "fallbackMostRecent")
    public EarthquakeRecordWithWeatherDTO getMostRecentEarthquake() {
        String url = BASE_FEED_URL + "all_hour.geojson";

        String response = this.restTemplate.getForObject(url, String.class);
        JSONObject json = new JSONObject(response);
        JSONArray features = json.getJSONArray("features");

        if (features.isEmpty()) return null;

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

        return mapToEarthquakeRecordWithWeather(mostRecent);
    }

    private EarthquakeRecordDTO mapToEarthquakeRecord(JSONObject feature) {
        JSONObject properties = feature.getJSONObject("properties");
        JSONObject geometry = feature.getJSONObject("geometry");
        JSONArray coordinates = geometry.getJSONArray("coordinates");

        double longitude = coordinates.getDouble(0);
        double latitude = coordinates.getDouble(1);
        double depth = coordinates.getDouble(2);

        String place = properties.optString("place", "Unknown location");

        EarthquakeRecordDTO record = new EarthquakeRecordDTO(place, new GeoLocationDTO(latitude, longitude), depth);
        log.debug("Mapped EarthquakeRecordDTO: {}", record);
        return record;
    }

    private EarthquakeRecordWithWeatherDTO mapToEarthquakeRecordWithWeather(JSONObject feature) {
        JSONObject properties = feature.getJSONObject("properties");
        JSONObject geometry = feature.getJSONObject("geometry");
        JSONArray coordinates = geometry.getJSONArray("coordinates");

        double longitude = coordinates.getDouble(0);
        double latitude = coordinates.getDouble(1);
        double depth = coordinates.getDouble(2);

        String place = properties.optString("place", "Unknown location");

        EarthquakeRecordWithWeatherDTO record = new EarthquakeRecordWithWeatherDTO(place, new GeoLocationDTO(latitude, longitude), depth, null);
        log.debug("Mapped EarthquakeRecordWithWeatherDTO: {}", record);
        return record;
    }

    private String getFeedNameForDays(int days) {
        if (days <= 1) return "all_day.geojson";
        if (days <= 7) return "all_week.geojson";
        return "all_month.geojson";
    }

    public EarthquakeRecordDTO fallbackWorst(int days, Throwable t) {
        System.err.println("Fallback for getWorstEarthquakeInPeriod: " + t.getMessage());
        return null;
    }

    public EarthquakeRecordWithWeatherDTO fallbackMostRecent(Throwable t) {
        System.err.println("Fallback for getMostRecentEarthquake: " + t.getMessage());
        return null;
    }
}
