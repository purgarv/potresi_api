package si.telekom.potresi.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import si.telekom.potresi.dto.*;
import si.telekom.potresi.service.EarthquakeService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class EarthquakeControllerTest {

    private MockMvc mockMvc;
    private EarthquakeService earthquakeService;

    @BeforeEach
    void setup() {
        earthquakeService = mock(EarthquakeService.class);
        EarthquakeController controller = new EarthquakeController(earthquakeService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // ========== /potresi/rekordi/tedenski ==========

    @Test
    void getWorstWeeklyEarthquake_shouldReturnOk() throws Exception {
        EarthquakeRecordDTO record = new EarthquakeRecordDTO("Ljubljana", new GeoLocationDTO(46.05, 14.5), 9.1);
        when(earthquakeService.getWorstEarthquakeLastWeek()).thenReturn(record);

        mockMvc.perform(get("/potresi/rekordi/tedenski")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nearestPlace").value("Ljubljana"))
                .andExpect(jsonPath("$.location.latitude").value(46.05))
                .andExpect(jsonPath("$.location.longitude").value(14.5))
                .andExpect(jsonPath("$.depth").value(9.1));
    }

    @Test
    void getWorstWeeklyEarthquake_shouldReturnNoContent() throws Exception {
        when(earthquakeService.getWorstEarthquakeLastWeek()).thenReturn(null);

        mockMvc.perform(get("/potresi/rekordi/tedenski"))
                .andExpect(status().isNoContent());
    }

    // ========== /potresi/rekordi/mesecni ==========

    @Test
    void getWorstMonthlyEarthquake_shouldReturnOk() throws Exception {
        EarthquakeRecordDTO record = new EarthquakeRecordDTO("Maribor", new GeoLocationDTO(46.55, 15.65), 12.4);
        when(earthquakeService.getWorstEarthquakeLastMonth()).thenReturn(record);

        mockMvc.perform(get("/potresi/rekordi/mesecni")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nearestPlace").value("Maribor"))
                .andExpect(jsonPath("$.location.latitude").value(46.55))
                .andExpect(jsonPath("$.location.longitude").value(15.65))
                .andExpect(jsonPath("$.depth").value(12.4));
    }

    @Test
    void getWorstMonthlyEarthquake_shouldReturnNoContent() throws Exception {
        when(earthquakeService.getWorstEarthquakeLastMonth()).thenReturn(null);

        mockMvc.perform(get("/potresi/rekordi/mesecni"))
                .andExpect(status().isNoContent());
    }

    // ========== /potresi/zadnji ==========

    @Test
    void getMostRecentEarthquakeWithWeather_shouldReturnOk() throws Exception {
        EarthquakeRecordDTO record = new EarthquakeRecordDTO(
                "Celje",
                new GeoLocationDTO(46.24, 15.27),
                8.5,
                new WeatherInfoDTO("Sunny", 23.0, 60.0)
        );
        when(earthquakeService.getLastEarthquakeWithWeather()).thenReturn(record);

        mockMvc.perform(get("/potresi/zadnji")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nearestPlace").value("Celje"))
                .andExpect(jsonPath("$.location.latitude").value(46.24))
                .andExpect(jsonPath("$.location.longitude").value(15.27))
                .andExpect(jsonPath("$.depth").value(8.5))
                .andExpect(jsonPath("$.weatherInfo.description").value("Sunny"))
                .andExpect(jsonPath("$.weatherInfo.temperature").value(23.0))
                .andExpect(jsonPath("$.weatherInfo.humidity").value(60.0));
    }

    @Test
    void getMostRecentEarthquakeWithWeather_shouldReturnNoContent() throws Exception {
        when(earthquakeService.getLastEarthquakeWithWeather()).thenReturn(null);

        mockMvc.perform(get("/potresi/zadnji"))
                .andExpect(status().isNoContent());
    }
}
