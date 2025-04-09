package si.telekom.potresi.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import si.telekom.potresi.dto.WeatherInfoDTO;

@Component
public class WeatherClient {

    private static final Logger log = LoggerFactory.getLogger(WeatherClient.class);
    private final RestTemplate restTemplate;

    public WeatherClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Value("${weather.api.key}")
    private String apiKey;

    private static final String WEATHER_API = "https://api.openweathermap.org/data/2.5/weather";

    @CircuitBreaker(name = "weatherApi")
    @Retry(name = "weatherApi", fallbackMethod = "weatherFallback")
    public WeatherInfoDTO getCurrentWeather(double latitude, double longitude) {
        String uri = UriComponentsBuilder.fromUriString(WEATHER_API)
                .queryParam("lat", latitude)
                .queryParam("lon", longitude)
                .queryParam("appid", apiKey)
                .queryParam("units", "metric")
                .build()
                .toUriString();

        String response = restTemplate.getForObject(uri, String.class);
        JSONObject json = new JSONObject(response);

        String description = json
                .getJSONArray("weather")
                .getJSONObject(0)
                .getString("description");

        JSONObject main = json.getJSONObject("main");

        double temp = main.getDouble("temp");
        double humidity = main.getDouble("humidity");

        return new WeatherInfoDTO(description, temp, humidity);
    }

    public WeatherInfoDTO weatherFallback(double latitude, double longitude, Throwable t) {
        System.err.println("Weather fallback triggered: " + t.getMessage());
        return new WeatherInfoDTO("Weather data unavailable", 0.0, 0.0, false);
    }

}
