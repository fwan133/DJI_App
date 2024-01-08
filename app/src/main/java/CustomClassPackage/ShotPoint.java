package CustomClassPackage;

public class ShotPoint {
    private double x;
    private double y;
    private double z;
    private double Yaw;
    private double Pitch;

    public ShotPoint(double x, double y, double z, double yaw, double pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        Yaw = yaw;
        Pitch = pitch;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
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
