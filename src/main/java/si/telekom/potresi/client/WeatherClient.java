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

    @CircuitBreaker(name = "weatherApi")
    @Retry(name = "weatherApi", fallbackMethod = "weatherFallback")
    public WeatherInfoDTO getCurrentWeather(double latitude, double longitude) {
        String uri = UriComponentsBuilder.fromUriString(config.getBaseUrl())
                .queryParam("lat", latitude)
                .queryParam("lon", longitude)
                .queryParam("appid",  config.getKey())
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
        log.warn("Weather fallback triggered", t);
        return new WeatherInfoDTO("Weather data unavailable", 0.0, 0.0, false);
    }

}
