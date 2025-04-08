package si.telekom.potresi.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import si.telekom.potresi.dto.EarthquakeRecordDTO;
import si.telekom.potresi.dto.GeoLocationDTO;
import si.telekom.potresi.dto.WeatherInfoDTO;
import si.telekom.potresi.service.EarthquakeService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EarthquakeController.class)
class EarthquakeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EarthquakeService earthquakeService;


    // -------------------------
    // /potresi/rekordi/tedenski
    // -------------------------

    // Weekly — 200
    @Test
    void testGetWorstWeeklyEarthquake_Returns200_FullFields() throws Exception {
        var record = new EarthquakeRecordDTO("WeeklyCity", new GeoLocationDTO(1.0, 2.0), 4.0);
        when(earthquakeService.getWorstEarthquakeLastWeek()).thenReturn(List.of(record));

        mockMvc.perform(get("/potresi/rekordi/tedenski"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nearestPlace").value("WeeklyCity"))
                .andExpect(jsonPath("$[0].depth").value(4.0))
                .andExpect(jsonPath("$[0].location.latitude").value(1.0))
                .andExpect(jsonPath("$[0].location.longitude").value(2.0));
    }

    // Weekly — multiple
    @Test
    void testGetWorstWeeklyEarthquake_MultipleRecords_FullFields() throws Exception {
        var r1 = new EarthquakeRecordDTO("A", new GeoLocationDTO(1.1, 2.2), 3.1);
        var r2 = new EarthquakeRecordDTO("B", new GeoLocationDTO(3.3, 4.4), 3.2);
        when(earthquakeService.getWorstEarthquakeLastWeek()).thenReturn(List.of(r1, r2));

        mockMvc.perform(get("/potresi/rekordi/tedenski"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nearestPlace").value("A"))
                .andExpect(jsonPath("$[0].depth").value(3.1))
                .andExpect(jsonPath("$[0].location.latitude").value(1.1))
                .andExpect(jsonPath("$[0].location.longitude").value(2.2))
                .andExpect(jsonPath("$[1].nearestPlace").value("B"))
                .andExpect(jsonPath("$[1].depth").value(3.2))
                .andExpect(jsonPath("$[1].location.latitude").value(3.3))
                .andExpect(jsonPath("$[1].location.longitude").value(4.4));
    }

    // Weekly — 503
    @Test
    void testGetWorstWeeklyEarthquake_Returns503() throws Exception {
        when(earthquakeService.getWorstEarthquakeLastWeek()).thenReturn(List.of());

        mockMvc.perform(get("/potresi/rekordi/tedenski"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("Could not fetch the worst earthquake data for the last week."));
    }


    // --------------------------
    // /potresi/rekordi/mesecni
    // --------------------------

    // Monthly — 200
    @Test
    void testGetWorstMonthlyEarthquake_Returns200_FullFields() throws Exception {
        var record = new EarthquakeRecordDTO("MonthlyCity", new GeoLocationDTO(5.5, 6.6), 5.0);
        when(earthquakeService.getWorstEarthquakeLastMonth()).thenReturn(List.of(record));

        mockMvc.perform(get("/potresi/rekordi/mesecni"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nearestPlace").value("MonthlyCity"))
                .andExpect(jsonPath("$[0].depth").value(5.0))
                .andExpect(jsonPath("$[0].location.latitude").value(5.5))
                .andExpect(jsonPath("$[0].location.longitude").value(6.6));
    }

    // Monthly — multiple
    @Test
    void testGetWorstMonthlyEarthquake_MultipleRecords_FullFields() throws Exception {
        var r1 = new EarthquakeRecordDTO("X", new GeoLocationDTO(1.0, 2.0), 5.1);
        var r2 = new EarthquakeRecordDTO("Y", new GeoLocationDTO(3.0, 4.0), 5.2);
        var r3 = new EarthquakeRecordDTO("Z", new GeoLocationDTO(5.0, 6.0), 5.3);
        when(earthquakeService.getWorstEarthquakeLastMonth()).thenReturn(List.of(r1, r2, r3));

        mockMvc.perform(get("/potresi/rekordi/mesecni"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].nearestPlace").value("X"))
                .andExpect(jsonPath("$[0].depth").value(5.1))
                .andExpect(jsonPath("$[0].location.latitude").value(1.0))
                .andExpect(jsonPath("$[0].location.longitude").value(2.0))
                .andExpect(jsonPath("$[1].nearestPlace").value("Y"))
                .andExpect(jsonPath("$[1].depth").value(5.2))
                .andExpect(jsonPath("$[1].location.latitude").value(3.0))
                .andExpect(jsonPath("$[1].location.longitude").value(4.0))
                .andExpect(jsonPath("$[2].nearestPlace").value("Z"))
                .andExpect(jsonPath("$[2].depth").value(5.3))
                .andExpect(jsonPath("$[2].location.latitude").value(5.0))
                .andExpect(jsonPath("$[2].location.longitude").value(6.0));
    }

    // Monthly — 503
    @Test
    void testGetWorstMonthlyEarthquake_Returns503() throws Exception {
        when(earthquakeService.getWorstEarthquakeLastMonth()).thenReturn(List.of());

        mockMvc.perform(get("/potresi/rekordi/mesecni"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("Could not fetch the worst earthquake data for the last month."));
    }


    // ---------------------
    // /potresi/zadnji
    // ---------------------

    // Most recent — 200
    @Test
    void testGetMostRecentEarthquakeWithWeather_Returns200_FullFields() throws Exception {
        var quake = new EarthquakeRecordDTO("LatestPlace", new GeoLocationDTO(11.1, 22.2), 3.3,
                new WeatherInfoDTO("Rainy", 14.5, 78.0));
        when(earthquakeService.getLastEarthquakeWithWeather()).thenReturn(quake);

        mockMvc.perform(get("/potresi/zadnji"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nearestPlace").value("LatestPlace"))
                .andExpect(jsonPath("$.depth").value(3.3))
                .andExpect(jsonPath("$.location.latitude").value(11.1))
                .andExpect(jsonPath("$.location.longitude").value(22.2))
                .andExpect(jsonPath("$.weather.description").value("Rainy"))
                .andExpect(jsonPath("$.weather.temperature").value(14.5))
                .andExpect(jsonPath("$.weather.humidity").value(78.0));
    }

    // Most recent — 503
    @Test
    void testGetMostRecentEarthquakeWithWeather_Returns503() throws Exception {
        when(earthquakeService.getLastEarthquakeWithWeather()).thenReturn(null);

        mockMvc.perform(get("/potresi/zadnji"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("Could not fetch the most recent earthquake data."));
    }
}
