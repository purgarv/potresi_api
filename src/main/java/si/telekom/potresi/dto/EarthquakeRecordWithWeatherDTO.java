package si.telekom.potresi.dto;

public class EarthquakeRecordWithWeatherDTO extends EarthquakeRecordDTO {
    private WeatherInfoDTO weatherInfo;

    public EarthquakeRecordWithWeatherDTO() {
        super();
    }

    public EarthquakeRecordWithWeatherDTO(String nearestPlace, GeoLocationDTO location, double depth, WeatherInfoDTO weatherInfo) {
        super(nearestPlace, location, depth);
        this.weatherInfo = weatherInfo;
    }

    public WeatherInfoDTO getWeatherInfo() {
        return weatherInfo;
    }

    public void setWeatherInfo(WeatherInfoDTO weatherInfo) {
        this.weatherInfo = weatherInfo;
    }

    @Override
    public String toString() {
        return "DetailedEarthquakeDTO{" +
                "nearestPlace='" + getNearestPlace() + '\'' +
                ", location=" + getLocation() +
                ", depth=" + getDepth() +
                ", weatherInfo=" + weatherInfo +
                '}';
    }
}
