package si.telekom.potresi.dto;

public class WeatherInfoDTO {
    private String description;
    private double temperature; // °C
    private double humidity; // %
    private boolean weatherAvailable = true;


    public WeatherInfoDTO() {}

    public WeatherInfoDTO(String description, double temperature, double humidity) {
        this.description = description;
        this.temperature = temperature;
        this.humidity = humidity;
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

    @Override
    public String toString() {
        return "WeatherInfo{" +
                "description='" + description + '\'' +
                ", temperature=" + temperature +
                ", humidity=" + humidity +
                '}';
    }

    public boolean isWeatherAvailable() {
        return weatherAvailable;
    }

    public void setWeatherAvailable(boolean weatherAvailable) {
        this.weatherAvailable = weatherAvailable;
    }
}
