package si.telekom.potresi.controller;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Controller that exposes a custom summary of HTTP metrics
 * collected by Micrometer (via Spring Actuator).
 */
@RestController
@RequestMapping("/metrics")
public class MetricsController {

    private static final Logger log = LoggerFactory.getLogger(MetricsController.class);

    private final MeterRegistry meterRegistry;

    public MetricsController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Returns a summary of HTTP server request metrics including average time,
     * max time, total calls, and response status counts, grouped by URI and method.
     *
     * @return a nested map of metric data
     */
    @GetMapping("/summary")
    public Map<String, Object> getMetricsSummary() {
        log.info("Request received: GET /metrics/summary");

        Map<String, Object> summary = new TreeMap<>();
        Collection<Timer> timers = meterRegistry.find("http.server.requests").timers();

        for (Timer timer : timers) {
            Map<String, String> tags = timer.getId().getTags().stream()
                    .collect(HashMap::new, (m, t) -> m.put(t.getKey(), t.getValue()), HashMap::putAll);

            String uri = tags.getOrDefault("uri", "unknown");
            String method = tags.getOrDefault("method", "UNKNOWN");
            String status = tags.getOrDefault("status", "unknown");

            // Skip irrelevant or wildcard URIs
            if (uri.equals("/**") || uri.startsWith("/actuator")) continue;

            summary.putIfAbsent(uri, new TreeMap<>());
            Map<String, Object> methodMap = (Map<String, Object>) summary.get(uri);

            methodMap.putIfAbsent(method, new LinkedHashMap<>());
            Map<String, Object> methodStats = (Map<String, Object>) methodMap.get(method);

            // Initialize metric values if not present
            if (!methodStats.containsKey("averageTimeMs")) {
                methodStats.put("averageTimeMs", timer.mean(TimeUnit.MILLISECONDS));
                methodStats.put("maxTimeMs", timer.max(TimeUnit.MILLISECONDS));
                methodStats.put("totalCalls", 0L);
                methodStats.put("responseCounts", new TreeMap<String, Long>());
            }

            // Update response counts and total call count
            Map<String, Long> responseCounts = (Map<String, Long>) methodStats.get("responseCounts");
            long count = timer.count();
            responseCounts.put(status, responseCounts.getOrDefault(status, 0L) + count);

            long total = (long) methodStats.get("totalCalls");
            methodStats.put("totalCalls", total + count);
        }

        log.info("Returning metrics summary with {} tracked URI(s).", summary.size());
        return summary;
    }
}
