package si.telekom.potresi.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;
import si.telekom.potresi.dto.EarthquakeRecordDTO;
import si.telekom.potresi.dto.EarthquakeRecordWithWeatherDTO;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EarthquakeClientTest {

    private RestTemplate restTemplate;
    private EarthquakeClient earthquakeClient;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        earthquakeClient = new EarthquakeClient(restTemplate);
    }

    // ========== getWorstEarthquakeInPeriod() ==========

    @Test
    void getWorstEarthquakeInPeriod_shouldReturnStrongestQuake() {
        String mockJson = """
        {
            "features": [
                {
                    "properties": { "mag": 3.5, "place": "Place A" },
                    "geometry": { "coordinates": [14.5, 46.1, 10.0] }
                },
                {
                    "properties": { "mag": 4.7, "place": "Place B" },
                    "geometry": { "coordinates": [14.6, 46.2, 8.0] }
                }
            ]
        }
        """;

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockJson);

        EarthquakeRecordDTO result = earthquakeClient.getWorstEarthquakeInPeriod(7);

        assertNotNull(result);
        assertEquals("Place B", result.getNearestPlace());
        assertEquals(14.6, result.getLocation().getLongitude());
        assertEquals(46.2, result.getLocation().getLatitude());
        assertEquals(8.0, result.getDepth());
    }

    @Test
    void getWorstEarthquakeInPeriod_shouldReturnNullOnEmptyFeed() {
        String emptyJson = """
        {
            "features": []
        }
        """;

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(emptyJson);

        EarthquakeRecordDTO result = earthquakeClient.getWorstEarthquakeInPeriod(7);

        assertNull(result);
    }

    @Test
    void getWorstEarthquakeInPeriod_shouldReturnNullOnInvalidResponse() {
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("invalid json");

        EarthquakeRecordDTO result = earthquakeClient.getWorstEarthquakeInPeriod(7);

        assertNull(result); // fallback will be triggered
    }

    // ========== getMostRecentEarthquake() ==========

    @Test
    void getMostRecentEarthquake_shouldReturnLatestQuake() {
        String mockJson = """
        {
            "features": [
                {
                    "properties": { "place": "Earlier Quake", "time": 1000 },
                    "geometry": { "coordinates": [14.5, 46.0, 10.0] }
                },
                {
                    "properties": { "place": "Most Recent", "time": 2000 },
                    "geometry": { "coordinates": [14.6, 46.1, 5.0] }
                }
            ]
        }
        """;

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockJson);

        EarthquakeRecordWithWeatherDTO result = earthquakeClient.getMostRecentEarthquake();

        assertNotNull(result);
        assertEquals("Most Recent", result.getNearestPlace());
        assertEquals(14.6, result.getLocation().getLongitude());
        assertEquals(46.1, result.getLocation().getLatitude());
        assertEquals(5.0, result.getDepth());
    }

    @Test
    void getMostRecentEarthquake_shouldReturnNullOnEmptyFeed() {
        String emptyJson = """
        {
            "features": []
        }
        """;

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(emptyJson);

        EarthquakeRecordWithWeatherDTO result = earthquakeClient.getMostRecentEarthquake();

        assertNull(result);
    }

    @Test
    void getMostRecentEarthquake_shouldReturnNullOnInvalidJson() {
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("nonsense");

        EarthquakeRecordWithWeatherDTO result = earthquakeClient.getMostRecentEarthquake();

        assertNull(result); // fallback should handle
    }
}
