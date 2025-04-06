package si.telekom.potresi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import si.telekom.potresi.client.EarthquakeClient;
import si.telekom.potresi.client.WeatherClient;
import si.telekom.potresi.dto.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EarthquakeServiceTest {

    private EarthquakeClient earthquakeClient;
    private WeatherClient weatherClient;
    private EarthquakeService earthquakeService;

    @BeforeEach
    void setUp() {
        earthquakeClient = mock(EarthquakeClient.class);
        weatherClient = mock(WeatherClient.class);
        earthquakeService = new EarthquakeService(earthquakeClient, weatherClient);
    }

    // ========== getWorstEarthquakeLastWeek() ==========

    @Test
    void getWorstEarthquakeLastWeek_shouldReturnValidData() {
        EarthquakeRecordDTO mockRecord = new EarthquakeRecordDTO("Kranj", new GeoLocationDTO(46.2, 14.4), 10.1);
        when(earthquakeClient.getWorstEarthquakeInPeriod(7)).thenReturn(mockRecord);

        EarthquakeRecordDTO result = earthquakeService.getWorstEarthquakeLastWeek();

        assertNotNull(result);
        assertEquals("Kranj", result.getNearestPlace());
        assertEquals(10.1, result.getDepth());
    }

    @Test
    void getWorstEarthquakeLastWeek_shouldHandleExceptionGracefully() {
        when(earthquakeClient.getWorstEarthquakeInPeriod(7)).thenThrow(new RuntimeException("Boom"));

        EarthquakeRecordDTO result = earthquakeService.getWorstEarthquakeLastWeek();

        assertNull(result);
    }

    // ========== getWorstEarthquakeLastMonth() ==========

    @Test
    void getWorstEarthquakeLastMonth_shouldReturnValidData() {
        EarthquakeRecordDTO mockRecord = new EarthquakeRecordDTO("Maribor", new GeoLocationDTO(46.55, 15.65), 12.5);
        when(earthquakeClient.getWorstEarthquakeInPeriod(30)).thenReturn(mockRecord);

        EarthquakeRecordDTO result = earthquakeService.getWorstEarthquakeLastMonth();

        assertNotNull(result);
        assertEquals("Maribor", result.getNearestPlace());
        assertEquals(12.5, result.getDepth());
    }

    @Test
    void getWorstEarthquakeLastMonth_shouldHandleExceptionGracefully() {
        when(earthquakeClient.getWorstEarthquakeInPeriod(30)).thenThrow(new RuntimeException("Boom"));

        EarthquakeRecordDTO result = earthquakeService.getWorstEarthquakeLastMonth();

        assertNull(result);
    }

    // ========== getLastEarthquakeWithWeather() ==========

    @Test
    void getLastEarthquakeWithWeather_shouldReturnValidData() {
        GeoLocationDTO location = new GeoLocationDTO(46.05, 14.5);
        EarthquakeRecordWithWeatherDTO earthquake = new EarthquakeRecordWithWeatherDTO("Ljubljana", location, 9.0, null);
        WeatherInfoDTO weather = new WeatherInfoDTO("Sunny", 24.0, 50.0);

        when(earthquakeClient.getMostRecentEarthquake()).thenReturn(earthquake);
        when(weatherClient.getCurrentWeather(location.getLatitude(), location.getLongitude())).thenReturn(weather);

        EarthquakeRecordWithWeatherDTO result = earthquakeService.getLastEarthquakeWithWeather();

        assertNotNull(result);
        assertEquals("Ljubljana", result.getNearestPlace());
        assertEquals("Sunny", result.getWeatherInfo().getDescription());
        assertEquals(24.0, result.getWeatherInfo().getTemperature());
    }

    @Test
    void getLastEarthquakeWithWeather_shouldReturnNullIfNoEarthquake() {
        when(earthquakeClient.getMostRecentEarthquake()).thenReturn(null);

        EarthquakeRecordWithWeatherDTO result = earthquakeService.getLastEarthquakeWithWeather();

        assertNull(result);
    }

    @Test
    void getLastEarthquakeWithWeather_shouldHandleWeatherFetchException() {
        GeoLocationDTO location = new GeoLocationDTO(46.1, 14.6);
        EarthquakeRecordWithWeatherDTO earthquake = new EarthquakeRecordWithWeatherDTO("Nova Gorica", location, 7.8, null);

        when(earthquakeClient.getMostRecentEarthquake()).thenReturn(earthquake);
        when(weatherClient.getCurrentWeather(location.getLatitude(), location.getLongitude()))
                .thenThrow(new RuntimeException("Weather API error"));

        EarthquakeRecordWithWeatherDTO result = earthquakeService.getLastEarthquakeWithWeather();

        assertNotNull(result);
        assertEquals("Nova Gorica", result.getNearestPlace());
        assertNull(result.getWeatherInfo()); // should fail gracefully
    }
}
