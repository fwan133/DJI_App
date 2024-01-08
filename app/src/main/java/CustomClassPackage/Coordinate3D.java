package CustomClassPackage;

public class Coordinate3D {
    private double Latitude;
    private double Longitude;
    private double Altitude;

    public Coordinate3D(double latitude, double longitude, double altitude) {
        Latitude = latitude;
        Longitude = longitude;
        Altitude = altitude;
    }

    public double getLatitude() {
        return Latitude;
    }

    public void setLatitude(double latitude) {
        Latitude = latitude;
    }

    public double getLongitude() {
        return Longitude;
    }

    public void setLongitude(double longitude) {
        Longitude = longitude;
    }

    public double getAltitude() {
        return Altitude;
    }

    public void setAltitude(double altitude) {
        Altitude = altitude;
    }
}
