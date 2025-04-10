package si.telekom.potresi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "earthquake.api")
public class EarthquakeApiConfig {

    private String baseUrl;
    private Map<String, String> feed;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Map<String, String> getFeed() {
        return feed;
    }

    public void setFeed(Map<String, String> feed) {
        this.feed = feed;
    }
}
