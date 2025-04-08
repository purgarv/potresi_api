package si.telekom.potresi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import si.telekom.potresi.client.EarthquakeClient;
import si.telekom.potresi.client.WeatherClient;
import si.telekom.potresi.dto.EarthquakeRecordDTO;
import si.telekom.potresi.dto.GeoLocationDTO;
import si.telekom.potresi.dto.WeatherInfoDTO;

import java.util.List;

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

    // -----------------------------
    // getWorstEarthquakeLastWeek()
    // -----------------------------

    @Test
    void testGetWorstEarthquakeLastWeek_LiveDataAvailable() {
        var records = List.of(new EarthquakeRecordDTO("WeekLive", new GeoLocationDTO(1, 2), 3));
        when(earthquakeClient.getWorstEarthquakeInPeriod(7)).thenReturn(records);

        var result = earthquakeService.getWorstEarthquakeLastWeek();

        assertEquals(records, result);
        verify(earthquakeClient).getWorstEarthquakeInPeriod(7);
    }

    @Test
    void testGetWorstEarthquakeLastWeek_LiveDataEmpty_UsesEmptyCache() {
        when(earthquakeClient.getWorstEarthquakeInPeriod(7)).thenReturn(List.of());

        var result = earthquakeService.getWorstEarthquakeLastWeek();

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetWorstEarthquakeLastWeek_LiveDataEmpty_UsesPreviousCache() {
        var cached = List.of(new EarthquakeRecordDTO("CachedWeek", new GeoLocationDTO(1, 2), 3));
        when(earthquakeClient.getWorstEarthquakeInPeriod(7)).thenReturn(cached);
        earthquakeService.getWorstEarthquakeLastWeek(); // Prime cache

        when(earthquakeClient.getWorstEarthquakeInPeriod(7)).thenReturn(List.of());
        var result = earthquakeService.getWorstEarthquakeLastWeek();

        assertEquals(cached, result);
    }

    @Test
    void testGetWorstEarthquakeLastWeek_MultipleRecordsReturned() {
        var records = List.of(
                new EarthquakeRecordDTO("Location A", new GeoLocationDTO(1, 2), 4.5),
                new EarthquakeRecordDTO("Location B", new GeoLocationDTO(3, 4), 4.5)
        );
        when(earthquakeClient.getWorstEarthquakeInPeriod(7)).thenReturn(records);

        var result = earthquakeService.getWorstEarthquakeLastWeek();

        assertEquals(2, result.size());
        assertEquals("Location A", result.get(0).getNearestPlace());
        assertEquals("Location B", result.get(1).getNearestPlace());
    }


    // -----------------------------
    // getWorstEarthquakeLastMonth()
    // -----------------------------

    @Test
    void testGetWorstEarthquakeLastMonth_LiveDataAvailable() {
        var records = List.of(new EarthquakeRecordDTO("MonthLive", new GeoLocationDTO(1, 2), 3));
        when(earthquakeClient.getWorstEarthquakeInPeriod(30)).thenReturn(records);

        var result = earthquakeService.getWorstEarthquakeLastMonth();

        assertEquals(records, result);
        verify(earthquakeClient).getWorstEarthquakeInPeriod(30);
    }

    @Test
    void testGetWorstEarthquakeLastMonth_LiveDataEmpty_UsesEmptyCache() {
        when(earthquakeClient.getWorstEarthquakeInPeriod(30)).thenReturn(List.of());

        var result = earthquakeService.getWorstEarthquakeLastMonth();

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetWorstEarthquakeLastMonth_LiveDataEmpty_UsesPreviousCache() {
        var cached = List.of(new EarthquakeRecordDTO("CachedMonth", new GeoLocationDTO(1, 2), 3));
        when(earthquakeClient.getWorstEarthquakeInPeriod(30)).thenReturn(cached);
        earthquakeService.getWorstEarthquakeLastMonth(); // Prime cache

        when(earthquakeClient.getWorstEarthquakeInPeriod(30)).thenReturn(List.of());
        var result = earthquakeService.getWorstEarthquakeLastMonth();

        assertEquals(cached, result);
    }

    @Test
    void testGetWorstEarthquakeLastMonth_MultipleRecordsReturned() {
        var records = List.of(
                new EarthquakeRecordDTO("City X", new GeoLocationDTO(5, 6), 5.0),
                new EarthquakeRecordDTO("City Y", new GeoLocationDTO(7, 8), 5.0),
                new EarthquakeRecordDTO("City Z", new GeoLocationDTO(9, 10), 5.0)
        );
        when(earthquakeClient.getWorstEarthquakeInPeriod(30)).thenReturn(records);

        var result = earthquakeService.getWorstEarthquakeLastMonth();

        assertEquals(3, result.size());
        assertEquals("City X", result.get(0).getNearestPlace());
        assertEquals("City Y", result.get(1).getNearestPlace());
        assertEquals("City Z", result.get(2).getNearestPlace());
    }


    // -----------------------------
    // getLastEarthquakeWithWeather()
    // -----------------------------

    @Test
    void testGetLastEarthquakeWithWeather_SuccessfulData() {
        var location = new GeoLocationDTO(45.0, 15.0);
        var quake = new EarthquakeRecordDTO("RecentQuake", location, 6.0);
        var weather = new WeatherInfoDTO("Clear skies", 21.5, 40.0);

        when(earthquakeClient.getMostRecentEarthquake()).thenReturn(quake);
        when(weatherClient.getCurrentWeather(45.0, 15.0)).thenReturn(weather);

        var result = earthquakeService.getLastEarthquakeWithWeather();

        assertNotNull(result);
        assertEquals(weather, result.getWeather());
        verify(earthquakeClient).getMostRecentEarthquake();
        verify(weatherClient).getCurrentWeather(45.0, 15.0);
    }

    @Test
    void testGetLastEarthquakeWithWeather_WeatherServiceFails() {
        var location = new GeoLocationDTO(46.0, 14.0);
        var quake = new EarthquakeRecordDTO("NoWeather", location, 5.0);

        when(earthquakeClient.getMostRecentEarthquake()).thenReturn(quake);
        when(weatherClient.getCurrentWeather(46.0, 14.0))
                .thenThrow(new RuntimeException("Weather API error"));

        var result = earthquakeService.getLastEarthquakeWithWeather();

        assertNotNull(result);
        assertNull(result.getWeather()); // weather field not set due to failure
        verify(earthquakeClient).getMostRecentEarthquake();
        verify(weatherClient).getCurrentWeather(46.0, 14.0);
    }


    @Test
    void testGetLastEarthquakeWithWeather_ClientReturnsNull_NoCachedData() {
        // No cached data has been set yet
        when(earthquakeClient.getMostRecentEarthquake()).thenReturn(null);

        var result = earthquakeService.getLastEarthquakeWithWeather();

        assertNull(result, "Should return null when no live or cached data is available.");
        verify(earthquakeClient).getMostRecentEarthquake();
    }


    @Test
    void testGetLastEarthquakeWithWeather_ClientReturnsNull_UsesCachedData() {
        // First call: populate cache
        var cached = new EarthquakeRecordDTO("CachedQuake", new GeoLocationDTO(47, 13), 4.5);
        when(earthquakeClient.getMostRecentEarthquake()).thenReturn(cached);
        earthquakeService.getLastEarthquakeWithWeather();

        // Second call: simulate client failure
        when(earthquakeClient.getMostRecentEarthquake()).thenReturn(null);

        var result = earthquakeService.getLastEarthquakeWithWeather();

        assertNotNull(result, "Should return cached data");
        assertEquals("CachedQuake", result.getNearestPlace());
        verify(earthquakeClient, times(2)).getMostRecentEarthquake();
    }


    // -----------------------------
    // refreshCache()
    // -----------------------------

    @Test
    void testRefreshCache_FullData() {
        var record = new EarthquakeRecordDTO("Refreshed", new GeoLocationDTO(1, 2), 4);
        var weather = new WeatherInfoDTO("Fog", 16.0, 80.0);

        when(earthquakeClient.getWorstEarthquakeInPeriod(7)).thenReturn(List.of(record));
        when(earthquakeClient.getWorstEarthquakeInPeriod(30)).thenReturn(List.of(record));
        when(earthquakeClient.getMostRecentEarthquake()).thenReturn(record);
        when(weatherClient.getCurrentWeather(1, 2)).thenReturn(weather);

        earthquakeService.refreshCache();

        verify(earthquakeClient, times(1)).getWorstEarthquakeInPeriod(7);
        verify(earthquakeClient, times(1)).getWorstEarthquakeInPeriod(30);
        verify(earthquakeClient, times(1)).getMostRecentEarthquake();
        verify(weatherClient, times(1)).getCurrentWeather(1, 2);
    }

    @Test
    void testRefreshCache_WeatherFails() {
        var record = new EarthquakeRecordDTO("Refreshed", new GeoLocationDTO(1, 2), 4);

        when(earthquakeClient.getWorstEarthquakeInPeriod(7)).thenReturn(List.of(record));
        when(earthquakeClient.getWorstEarthquakeInPeriod(30)).thenReturn(List.of(record));
        when(earthquakeClient.getMostRecentEarthquake()).thenReturn(record);
        when(weatherClient.getCurrentWeather(1, 2)).thenThrow(new RuntimeException("Timeout"));

        earthquakeService.refreshCache();

        verify(weatherClient).getCurrentWeather(1, 2);
    }

    @Test
    void testRefreshCache_NullFromClients() {
        when(earthquakeClient.getWorstEarthquakeInPeriod(7)).thenReturn(null);
        when(earthquakeClient.getWorstEarthquakeInPeriod(30)).thenReturn(null);
        when(earthquakeClient.getMostRecentEarthquake()).thenReturn(null);

        earthquakeService.refreshCache(); // Should not crash

        verify(earthquakeClient, times(1)).getWorstEarthquakeInPeriod(7);
        verify(earthquakeClient, times(1)).getWorstEarthquakeInPeriod(30);
        verify(earthquakeClient, times(1)).getMostRecentEarthquake();
    }
}
