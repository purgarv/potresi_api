package si.telekom.potresi.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.client.RestTemplate;
import si.telekom.potresi.config.WeatherApiConfig;
import si.telekom.potresi.dto.WeatherInfoDTO;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WeatherClientTest {

    private RestTemplate restTemplate;
    private WeatherClient weatherClient;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);

        WeatherApiConfig config = new WeatherApiConfig();
        config.setBaseUrl("https://fake-weather-api.com");
        config.setKey("test-api-key");

        weatherClient = new WeatherClient(restTemplate, config);
    }

    // -----------------------------
    // getCurrentWeather
    // -----------------------------

    @Test
    void testGetCurrentWeather_ParsesValidJson() {
        String json = """
        {
          "weather": [ { "description": "Cloudy" } ],
          "main": { "temp": 19.5, "humidity": 70 }
        }
        """;

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(json);

        WeatherInfoDTO result = weatherClient.getCurrentWeather(46.0, 14.0);

        assertNotNull(result);
        assertEquals("Cloudy", result.getDescription());
        assertEquals(19.5, result.getTemperature());
        assertEquals(70.0, result.getHumidity());
        assertTrue(result.isWeatherAvailable());
    }

    @Test
    void testGetCurrentWeather_MissingFields_Throws() {
        String json = """
        {
          "weather": [],
          "main": {}
        }
        """;

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(json);

        assertThrows(Exception.class, () -> {
            weatherClient.getCurrentWeather(50.0, 16.0);
        });
    }

    @Test
    void testGetCurrentWeather_InvalidJson_Throws() {
        String json = "INVALID_JSON";

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(json);

        assertThrows(Exception.class, () -> {
            weatherClient.getCurrentWeather(0.0, 0.0);
        });
    }

    // -----------------------------
    // weatherFallback
    // -----------------------------

    @Test
    void testWeatherFallback_ReturnsUnavailableDTO() {
        WeatherInfoDTO fallback = weatherClient.weatherFallback(10.0, 20.0, new RuntimeException("API down"));

        assertNotNull(fallback);
        assertEquals("Weather data unavailable", fallback.getDescription());
        assertEquals(0.0, fallback.getTemperature());
        assertEquals(0.0, fallback.getHumidity());
        assertFalse(fallback.isWeatherAvailable());
    }
}
