package CustomClassPackage;

public class FlightShotPoint {
    private double Latitude;
    private double Longitude;
    private double Altitude;
    private double Yaw;
    private double Pitch;

    public FlightShotPoint(double latitude, double longitude, double altitude, double yaw, double pitch) {
        Latitude = latitude;
        Longitude = longitude;
        Altitude = altitude;
        Yaw = yaw;
        Pitch = pitch;
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

    public double getYaw() {
        return Yaw;
    }

    public void setYaw(double yaw) {
        Yaw = yaw;
    }

    public double getPitch() {
        return Pitch;
    }

    public void setPitch(double pitch) {
        Pitch = pitch;
    }
}
