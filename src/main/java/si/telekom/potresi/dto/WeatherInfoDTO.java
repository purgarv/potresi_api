package si.telekom.potresi.dto;

public class WeatherInfoDTO {
    /** Text description of the current weather (e.g., "clear sky", "light rain"). */
    private String description;

    /** Temperature in degrees Celsius. */
    private double temperature;

    /** Relative humidity as a percentage (0–100). */
    private double humidity;

    /** Indicates whether the weather data was successfully retrieved. */
    private boolean weatherAvailable = true;

    public WeatherInfoDTO() {}

    public WeatherInfoDTO(String description, double temperature, double humidity) {
        this.description = description;
        this.temperature = temperature;
        this.humidity = humidity;
    }

    public WeatherInfoDTO(String description, double temperature, double humidity, boolean weatherAvailable) {
        this.description = description;
        this.temperature = temperature;
        this.humidity = humidity;
        this.weatherAvailable = weatherAvailable;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public boolean isWeatherAvailable() {
        return weatherAvailable;
    }

    public void setWeatherAvailable(boolean weatherAvailable) {
        this.weatherAvailable = weatherAvailable;
    }

    @Override
    public String toString() {
        return "WeatherInfo{" +
                "description='" + description + '\'' +
                ", temperature=" + temperature +
                ", humidity=" + humidity +
                '}';
    }
}
