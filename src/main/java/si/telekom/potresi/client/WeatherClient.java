package si.telekom.potresi.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import si.telekom.potresi.config.WeatherApiConfig;
import si.telekom.potresi.dto.WeatherInfoDTO;

@Component
public class WeatherClient {

    private static final Logger log = LoggerFactory.getLogger(WeatherClient.class);
    private final RestTemplate restTemplate;
    private final WeatherApiConfig config;

    public WeatherClient(RestTemplate restTemplate, WeatherApiConfig config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    /**
     * Retrieves current weather data for the given coordinates.
     * Uses circuit breaker and retry for resilience.
     *
     * @param latitude latitude of the location
     * @param longitude longitude of the location
     * @return WeatherInfoDTO containing weather details
     */
    @CircuitBreaker(name = "weatherApi")
    @Retry(name = "weatherApi", fallbackMethod = "weatherFallback")
    public WeatherInfoDTO getCurrentWeather(double latitude, double longitude) {
        // Build the full URI with parameters
        String uri = UriComponentsBuilder.fromUriString(config.getBaseUrl())
                .queryParam("lat", latitude)
                .queryParam("lon", longitude)
                .queryParam("appid", config.getKey())
                .queryParam("units", "metric")
                .build()
                .toUriString();

        log.info("Fetching weather data from URI: {}", uri);

        // Call the API and parse the JSON response
        String response = restTemplate.getForObject(uri, String.class);
        JSONObject json = new JSONObject(response);

        log.debug("Received weather response: {}", json.toString());

        // Extract weather description
        String description = json
                .getJSONArray("weather")
                .getJSONObject(0)
                .getString("description");

        // Extract temperature and humidity from "main" section
        JSONObject main = json.getJSONObject("main");
        double temp = main.getDouble("temp");
        double humidity = main.getDouble("humidity");

        log.info("Parsed weather info - Description: {}, Temp: {}, Humidity: {}", description, temp, humidity);

        return new WeatherInfoDTO(description, temp, humidity);
    }

    /**
     * Fallback method triggered when weather API call fails.
     */
    public WeatherInfoDTO weatherFallback(double latitude, double longitude, Throwable t) {
        log.warn("Weather fallback triggered for location ({}, {})", latitude, longitude, t);
        return new WeatherInfoDTO("Weather data unavailable", 0.0, 0.0, false);
    }
}
