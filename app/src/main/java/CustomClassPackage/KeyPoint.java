package CustomClassPackage;

public class KeyPoint {
    private double X;
    private double Y;
    private double Z;
    private String Type;

    public KeyPoint(double X, double Y, double Z, String Type){
        this.X=X;
        this.Y=Y;
        this.Z=Z;
        this.Type=Type;
    }

    public double getX(){
        return this.X;
    }

    public double getY(){
        return this.Y;
    }

    public double getZ(){
        return this.Z;
    }

    public String getType(){
        return this.Type;
    }

    public String getInfo(){
        String keyPointInfo=this.getType()+","+this.getX()+","+this.getY()+","+this.getZ();
        return keyPointInfo;
    }
}
