package si.telekom.potresi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

public class EarthquakeRecordDTO {

    /** Nearest known place to the earthquake's epicenter. */
    private String nearestPlace;

    /** Geographic coordinates of the earthquake (latitude, longitude). */
    private GeoLocationDTO location;

    /** Depth of the earthquake in kilometers. */
    private double depth;

    /** Timestamp when this record was considered valid. */
    private Instant validAt;

    /**
     * Weather information at the time and location of the earthquake (optional).
     * Will be excluded from JSON if null.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private WeatherInfoDTO weather;

    public EarthquakeRecordDTO() {}

    public EarthquakeRecordDTO(String nearestPlace, GeoLocationDTO location, double depth) {
        this.nearestPlace = nearestPlace;
        this.location = location;
        this.depth = depth;
        this.weather = null;
    }

    public EarthquakeRecordDTO(String nearestPlace, GeoLocationDTO location, double depth, Instant validAt) {
        this.nearestPlace = nearestPlace;
        this.location = location;
        this.depth = depth;
        this.validAt = validAt;
    }

    public EarthquakeRecordDTO(String nearestPlace, GeoLocationDTO location, double depth, WeatherInfoDTO weather) {
        this.nearestPlace = nearestPlace;
        this.location = location;
        this.depth = depth;
        this.weather = weather;
    }

    public EarthquakeRecordDTO(String nearestPlace, GeoLocationDTO location, double depth, WeatherInfoDTO weather, Instant validAt) {
        this.nearestPlace = nearestPlace;
        this.location = location;
        this.depth = depth;
        this.weather = weather;
        this.validAt = validAt;
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

    public Instant getValidAt() {
        return validAt;
    }

    public void setValidAt(Instant validAt) {
        this.validAt = validAt;
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
