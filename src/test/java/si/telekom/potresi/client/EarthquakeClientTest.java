package si.telekom.potresi.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.client.RestTemplate;
import si.telekom.potresi.config.EarthquakeApiConfig;
import si.telekom.potresi.dto.EarthquakeRecordDTO;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EarthquakeClientTest {

    private RestTemplate restTemplate;
    private EarthquakeClient earthquakeClient;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);

        // Inject real config values
        EarthquakeApiConfig config = new EarthquakeApiConfig();
        config.setBaseUrl("https://fake.earthquake.api/");
        config.setFeed(Map.of(
                "hourly", "all_hour.geojson",
                "daily", "all_day.geojson",
                "weekly", "all_week.geojson",
                "monthly", "all_month.geojson"
        ));

        earthquakeClient = new EarthquakeClient(restTemplate, config);
    }

    // -----------------------------
    // getWorstEarthquakeInPeriod
    // -----------------------------

    @Test
    void testGetWorstEarthquakeInPeriod_SingleStrongest() {
        String json = """
        {
          "features": [
            {
              "properties": { "mag": 4.5, "place": "A" },
              "geometry": { "coordinates": [10, 20, 5] }
            },
            {
              "properties": { "mag": 3.0, "place": "B" },
              "geometry": { "coordinates": [15, 25, 10] }
            }
          ]
        }""";

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(json);
        List<EarthquakeRecordDTO> result = earthquakeClient.getWorstEarthquakeInPeriod(7);

        assertEquals(1, result.size());
        assertEquals("A", result.getFirst().getNearestPlace());
    }

    @Test
    void testGetWorstEarthquakeInPeriod_MultipleTies() {
        String json = """
        {
          "features": [
            {
              "properties": { "mag": 5.0, "place": "X" },
              "geometry": { "coordinates": [10, 20, 5] }
            },
            {
              "properties": { "mag": 5.0, "place": "Y" },
              "geometry": { "coordinates": [15, 25, 10] }
            }
          ]
        }""";

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(json);
        List<EarthquakeRecordDTO> result = earthquakeClient.getWorstEarthquakeInPeriod(7);

        assertEquals(2, result.size());
        assertEquals("X", result.get(0).getNearestPlace());
        assertEquals("Y", result.get(1).getNearestPlace());
    }

    @Test
    void testGetWorstEarthquakeInPeriod_MissingMagField() {
        String json = """
        {
          "features": [
            {
              "properties": { "place": "NoMag" },
              "geometry": { "coordinates": [10, 20, 5] }
            }
          ]
        }""";

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(json);
        List<EarthquakeRecordDTO> result = earthquakeClient.getWorstEarthquakeInPeriod(7);

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetWorstEarthquakeInPeriod_NullMag() {
        String json = """
        {
          "features": [
            {
              "properties": { "mag": null, "place": "NullMag" },
              "geometry": { "coordinates": [10, 20, 5] }
            }
          ]
        }""";

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(json);
        List<EarthquakeRecordDTO> result = earthquakeClient.getWorstEarthquakeInPeriod(7);

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetWorstEarthquakeInPeriod_EmptyFeed() {
        String json = """
        {
          "features": []
        }""";

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(json);
        List<EarthquakeRecordDTO> result = earthquakeClient.getWorstEarthquakeInPeriod(7);

        assertTrue(result.isEmpty());
    }

    // -----------------------------
    // getMostRecentEarthquake
    // -----------------------------

    @Test
    void testGetMostRecentEarthquake_FindsLatest() {
        String json = """
        {
          "features": [
            {
              "properties": { "time": 1000, "place": "Old" },
              "geometry": { "coordinates": [10, 20, 5] }
            },
            {
              "properties": { "time": 3000, "place": "New" },
              "geometry": { "coordinates": [30, 40, 15] }
            }
          ]
        }""";

        when(restTemplate.getForObject(contains("all_hour"), eq(String.class))).thenReturn(json);

        EarthquakeRecordDTO result = earthquakeClient.getMostRecentEarthquake();

        assertNotNull(result);
        assertEquals("New", result.getNearestPlace());
    }

    @Test
    void testGetMostRecentEarthquake_OnlyOneRecord() {
        String json = """
        {
          "features": [
            {
              "properties": { "time": 5000, "place": "Solo" },
              "geometry": { "coordinates": [10, 10, 10] }
            }
          ]
        }""";

        when(restTemplate.getForObject(contains("all_hour"), eq(String.class))).thenReturn(json);

        EarthquakeRecordDTO result = earthquakeClient.getMostRecentEarthquake();

        assertNotNull(result);
        assertEquals("Solo", result.getNearestPlace());
    }

    @Test
    void testGetMostRecentEarthquake_MissingTime() {
        String json = """
        {
          "features": [
            {
              "properties": { "place": "MissingTime" },
              "geometry": { "coordinates": [10, 20, 5] }
            }
          ]
        }""";

        when(restTemplate.getForObject(contains("all_hour"), eq(String.class))).thenReturn(json);

        EarthquakeRecordDTO result = earthquakeClient.getMostRecentEarthquake();

        assertNotNull(result);
        assertEquals("MissingTime", result.getNearestPlace());
    }

    @Test
    void testGetMostRecentEarthquake_AllFeedsEmpty() {
        String emptyJson = "{ \"features\": [] }";

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(emptyJson);

        EarthquakeRecordDTO result = earthquakeClient.getMostRecentEarthquake();

        assertNull(result);
    }

    // -----------------------------
    // Fallbacks
    // -----------------------------

    @Test
    void testFallbackWorst_ReturnsEmptyList() {
        List<EarthquakeRecordDTO> fallback = earthquakeClient.fallbackWorst(7, new RuntimeException("Fail"));
        assertNotNull(fallback);
        assertTrue(fallback.isEmpty());
    }

    @Test
    void testFallbackMostRecent_ReturnsNull() {
        EarthquakeRecordDTO fallback = earthquakeClient.fallbackMostRecent(new RuntimeException("Fail"));
        assertNull(fallback);
    }
}
