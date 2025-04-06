package si.telekom.potresi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EarthquakeApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(EarthquakeApiApplication.class, args);
    }
}
