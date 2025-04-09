package si.telekom.potresi.client;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestTemplate;
import si.telekom.potresi.dto.EarthquakeRecordDTO;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@SpringBootTest
public class EarthquakeClientResilienceTest {

    @Autowired
    private EarthquakeClient earthquakeClient;

    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void resetCircuitBreaker() {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("earthquakeApi");
        cb.reset();
    }

    @Test
    void getWorstEarthquakeInPeriod_ApiFails_RetriesAndFallbackTriggered() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RuntimeException("Simulated failure"));

        List<EarthquakeRecordDTO> result = earthquakeClient.getWorstEarthquakeInPeriod(7);

        assertNotNull(result); // Shouldn't be null because result is a list
        assertTrue(result.isEmpty());

        verify(restTemplate, atLeast(3)).getForObject(anyString(), eq(String.class));
    }

    @Test
    void getWorstEarthquakeInPeriod_CircuitBreakerOpen_TriggersFallbackImmediately() {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("earthquakeApi");
        cb.transitionToOpenState();

        List<EarthquakeRecordDTO> result = earthquakeClient.getWorstEarthquakeInPeriod(7);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Should not call the API
        verify(restTemplate, never()).getForObject(anyString(), eq(String.class));
    }


    @Test
    void getMostRecentEarthquake_ApiFails_RetriesAndFallbackTriggered() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RuntimeException("Simulated failure"));

        EarthquakeRecordDTO result = earthquakeClient.getMostRecentEarthquake();

        assertNull(result); // Null because result is a single object
        verify(restTemplate, atLeast(3)).getForObject(anyString(), eq(String.class));
    }

    @Test
    void getMostRecentEarthquake_CircuitBreakerOpen_TriggersFallbackImmediately() {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("earthquakeApi");
        cb.transitionToOpenState();

        EarthquakeRecordDTO result = earthquakeClient.getMostRecentEarthquake();

        assertNull(result);

        verify(restTemplate, never()).getForObject(anyString(), eq(String.class));
    }

}
