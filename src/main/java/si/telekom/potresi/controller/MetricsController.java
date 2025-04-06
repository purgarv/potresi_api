package si.telekom.potresi.controller;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/metrics")
public class MetricsController {

    private final MeterRegistry meterRegistry;

    public MetricsController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @GetMapping("/summary")
    public Map<String, Object> getMetricsSummary() {
        Map<String, Object> summary = new TreeMap<>();

        Collection<Timer> timers = meterRegistry.find("http.server.requests").timers();

        for (Timer timer : timers) {
            Map<String, String> tags = timer.getId().getTags().stream()
                    .collect(HashMap::new, (m, t) -> m.put(t.getKey(), t.getValue()), HashMap::putAll);

            String uri = tags.getOrDefault("uri", "unknown");
            String method = tags.getOrDefault("method", "UNKNOWN");
            String status = tags.getOrDefault("status", "unknown");

            if (uri.equals("/**") || uri.startsWith("/actuator")) continue;

            summary.putIfAbsent(uri, new TreeMap<>());
            Map<String, Object> methodMap = (Map<String, Object>) summary.get(uri);

            methodMap.putIfAbsent(method, new LinkedHashMap<>());
            Map<String, Object> methodStats = (Map<String, Object>) methodMap.get(method);

            // Initialize if not yet added
            if (!methodStats.containsKey("averageTimeMs")) {
                methodStats.put("averageTimeMs", timer.mean(TimeUnit.MILLISECONDS));
                methodStats.put("maxTimeMs", timer.max(TimeUnit.MILLISECONDS));
                methodStats.put("totalCalls", 0L);
                methodStats.put("responseCounts", new TreeMap<String, Long>());
            }

            // Update response count
            Map<String, Long> responseCounts = (Map<String, Long>) methodStats.get("responseCounts");
            long count = timer.count();
            responseCounts.put(status, responseCounts.getOrDefault(status, 0L) + count);

            // Update total calls
            long total = (long) methodStats.get("totalCalls");
            methodStats.put("totalCalls", total + count);
        }

        return summary;
    }
}
