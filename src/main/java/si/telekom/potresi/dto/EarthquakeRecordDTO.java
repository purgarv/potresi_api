package si.telekom.potresi.dto;

public class EarthquakeRecordDTO {
    private String nearestPlace;
    private GeoLocationDTO location;
    private double depth;



    public EarthquakeRecordDTO() {}

    public EarthquakeRecordDTO(String nearestPlace, GeoLocationDTO location, double depth) {
        this.nearestPlace = nearestPlace;
        this.location = location;
        this.depth = depth;
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

    @Override
    public String toString() {
        return "BasicEarthquakeDTO{" +
                "nearestPlace='" + nearestPlace + '\'' +
                ", location=" + location +
                ", depth=" + depth +
                '}';
    }
}
