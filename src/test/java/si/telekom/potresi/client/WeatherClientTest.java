package si.telekom.potresi.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;
import si.telekom.potresi.dto.WeatherInfoDTO;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WeatherClientTest {

    private RestTemplate restTemplate;
    private WeatherClient weatherClient;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        weatherClient = new WeatherClient(restTemplate);

        // manually set API key
        try {
            var field = WeatherClient.class.getDeclaredField("apiKey");
            field.setAccessible(true);
            field.set(weatherClient, "dummy-api-key");
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject API key into WeatherClient", e);
        }
    }

    // ========== getCurrentWeather() ==========

    @Test
    void getCurrentWeather_shouldReturnParsedWeather() {
        String mockJson = """
        {
            "weather": [
                { "description": "clear sky" }
            ],
            "main": {
                "temp": 22.5,
                "humidity": 60.0
            }
        }
        """;

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockJson);

        WeatherInfoDTO result = weatherClient.getCurrentWeather(46.05, 14.5);

        assertNotNull(result);
        assertEquals("clear sky", result.getDescription());
        assertEquals(22.5, result.getTemperature());
        assertEquals(60.0, result.getHumidity());
    }

    @Test
    void getCurrentWeather_shouldReturnFallbackOnInvalidJson() {
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("invalid json");

        WeatherInfoDTO result = weatherClient.getCurrentWeather(46.05, 14.5);

        assertNotNull(result);
        assertEquals("unavailable", result.getDescription());
        assertEquals(0.0, result.getTemperature());
        assertEquals(0.0, result.getHumidity());
    }

    @Test
    void getCurrentWeather_shouldReturnFallbackOnException() {
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenThrow(new RuntimeException("Connection error"));

        WeatherInfoDTO result = weatherClient.getCurrentWeather(46.05, 14.5);

        assertNotNull(result);
        assertEquals("unavailable", result.getDescription());
        assertEquals(0.0, result.getTemperature());
        assertEquals(0.0, result.getHumidity());
    }
}
