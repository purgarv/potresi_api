package si.telekom.potresi.client;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestTemplate;
import si.telekom.potresi.dto.WeatherInfoDTO;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class WeatherClientResilienceTest {

    @Autowired
    private WeatherClient weatherClient;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private EarthquakeClient earthquakeClient;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void resetCircuitBreaker() {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("weatherApi");
        cb.reset();
    }

    @Test
    void getCurrentWeather_ApiFails_RetriesAndFallbackTriggered() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RuntimeException("Simulated failure"));

        WeatherInfoDTO result = weatherClient.getCurrentWeather(45.0, 14.0);

        assertNotNull(result);
        assertEquals("Weather data unavailable", result.getDescription());

        verify(restTemplate, atLeast(3)).getForObject(anyString(), eq(String.class));
    }


    @Test
    void getCurrentWeather_CircuitBreakerOpen_TriggersFallbackImmediately() {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("weatherApi");
        cb.transitionToOpenState();

        WeatherInfoDTO result = weatherClient.getCurrentWeather(45.0, 14.0);

        assertNotNull(result);
        assertEquals("Weather data unavailable", result.getDescription());
        assertFalse(result.isWeatherAvailable());

        verify(restTemplate, never()).getForObject(anyString(), eq(String.class));
    }
}
