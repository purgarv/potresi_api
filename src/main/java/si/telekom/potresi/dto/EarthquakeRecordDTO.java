package si.telekom.potresi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

public class EarthquakeRecordDTO {
    private String nearestPlace;
    private GeoLocationDTO location;
    private double depth;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private WeatherInfoDTO weather;


    public EarthquakeRecordDTO() {}

    public EarthquakeRecordDTO(String nearestPlace, GeoLocationDTO location, double depth) {
        this.nearestPlace = nearestPlace;
        this.location = location;
        this.depth = depth;
        this.weather = null;
    }
    public EarthquakeRecordDTO(String nearestPlace, GeoLocationDTO location, double depth, WeatherInfoDTO weather) {
        this.nearestPlace = nearestPlace;
        this.location = location;
        this.depth = depth;
        this.weather = weather;
    }

    public String getNearestPlace() {
        return nearestPlace;
    }

    public void setNearestPlace(String nearestPlace) {
        this.nearestPlace = nearestPlace;
    }

    public GeoLocationDTO getLocation() {
        return location;
    }

    public void setLocation(GeoLocationDTO location) {
        this.location = location;
    }

    public double getDepth() {
        return depth;
    }

    public void setDepth(double depth) {
        this.depth = depth;
    }

    public WeatherInfoDTO getWeather() {
        return weather;
    }

    public void setWeather(WeatherInfoDTO weather) {
        this.weather = weather;
    }

    @Override
    public String toString() {
        return "BasicEarthquakeDTO{" +
                "nearestPlace='" + nearestPlace + '\'' +
                ", location=" + location +
                ", depth=" + depth +
                '}';
    }
}
