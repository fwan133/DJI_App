package com.aucklanduni.uavbridgeinspection;

import androidx.annotation.CheckResult;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import CustomClassPackage.KeyPoint;
import CustomClassPackage.Point;
import CustomClassPackage.Point2D;
import CustomClassPackage.Segment;
import CustomClassPackage.ShotPoint;
import jxl.Sheet;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class FlightRoutePlanning extends AppCompatActivity {

    private EditText ET_X;
    private EditText ET_Y;
    private EditText ET_Z;
    private EditText ET_BridgeWidth;
    private EditText ET_GSD;
    private EditText ET_Overlap;
    private EditText ET_FileName;
    private Button Bt_New;
    private Button Bt_Delete;
    private Button Bt_Clean;
    private Button Bt_Complete;
    private Button Bt_Create;
    private Button Bt_Confirm;
    private Button Bt_Calculate;
    private Button Bt_Save;
    private Button Bt_Load;
    private Button Bt_Show;
    private ScrollView SV_KeyPoints;
    private TextView TV_KeyPoints;
    private RadioButton RB_L2L;
    private RadioButton RB_L2C;
    private RadioButton RB_C2L;
    private RadioButton RB_C2C;
    private RadioGroup RG_type;
    private Spinner Sp_RouteFile;

    private boolean CheckDataResult;
    private String PointType;
    private ArrayList<KeyPoint> mKeyPoint;
    private ArrayList<Point> mPoint;
    private ArrayList<ShotPoint> mShotPoint;

    double Coordinate_Y;
    double GSD;
    double Overlap;
    double Distance;
    double LongSide;
    double ShortSide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_route_planning);
        initUI(FlightRoutePlanning.this);
        mKeyPoint=new ArrayList<>();
        mPoint=new ArrayList<>();
        mShotPoint=new ArrayList<>();

        //the Function of Create Button
        Bt_New.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                // Judge whether the coordinate data is complete
                if (ET_X.getText().toString().isEmpty()||ET_Y.getText().toString().isEmpty()){
                    CheckDataResult=true;
                }else if (ET_Z.getText().toString().isEmpty()){
                    CheckDataResult=true;
                }else{
                    CheckDataResult=false;
                }
                // Judge the type of the key point
                if (RB_C2C.isChecked()){
                    PointType="C2C";
                }else if (RB_C2L.isChecked()){
                    PointType="C2L";
                }else if (RB_L2C.isChecked()){
                    PointType="L2C";
                }else if (RB_L2L.isChecked()){
                    PointType="L2L";
                }

                // Create KeyPoint
                if (CheckDataResult){
                    showToast("Please firstly input the coordinates and type!");
                }else{
                    double x=Double.parseDouble(ET_X.getText().toString());
                    double y=Double.parseDouble(ET_Y.getText().toString());
                    double z=Double.parseDouble(ET_Z.getText().toString());
                    mKeyPoint.add(new KeyPoint(x,y,z,PointType));
                    //Present the information of the key point
                    setMsgToText(mKeyPoint);
                }
            }
        });

        //the Function of Delete Button
        Bt_Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mKeyPoint.isEmpty()){
                    showToast("Please create the key point first!");
                }else{
                    mKeyPoint.remove(mKeyPoint.size()-1);
                    setMsgToText(mKeyPoint);
                }
            }
        });

        //the Function of Clean Button
        Bt_Clean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mKeyPoint.isEmpty()){
                    showToast("Please create the key point first!");
                }else{
                    mKeyPoint.clear();
                    TV_KeyPoints.setText("");
                };
            }
        });

        //the Function of Complete Button
        Bt_Complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mKeyPoint.size()>3){
                    for (int i=0;i<mKeyPoint.size();i++){
                        if (mKeyPoint.get(i).getType().equals("L2C")){
                            if (mKeyPoint.get(i+1).getType().equals("C2C")&&(mKeyPoint.get(i+2).getType().equals("C2L"))){
                                ArrayList<Point2D> mPoint2D = createDensePoint(new Point2D(mKeyPoint.get(i).getX(), mKeyPoint.get(i).getZ()), new Point2D(mKeyPoint.get(i + 1).getX(), mKeyPoint.get(i + 1).getZ()), new Point2D(mKeyPoint.get(i + 2).getX(), mKeyPoint.get(i + 2).getZ()));
                                for (int j = 0; j < mPoint2D.size(); j++) {
                                    mPoint.add(new Point(mPoint2D.get(j).getX(), mKeyPoint.get(i).getY(), mPoint2D.get(j).getY()));
                                }
                            }else{
                                showToast("There is something wrong with type of key points. Please input again");
                            }
                        }else if (mKeyPoint.get(i).getType().equals("L2L")){
                            mPoint.add(new Point(mKeyPoint.get(i).getX(),mKeyPoint.get(i).getY(),mKeyPoint.get(i).getZ()));
                        }
                    }
                    showToast("Create Points Successfully!");
                }else {
                    showToast("Please input at least 3 key points!");
                }
            }
        });

        //Create the Function of Create Button
        Bt_Create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ET_BridgeWidth.getText().toString().isEmpty()){
                    showToast("Please input the width of the bridge");
                }else{
                    Coordinate_Y=Double.parseDouble(ET_BridgeWidth.getText().toString())/2;
                    showToast("Create the 3D model successfully!!");
                }
            }
        });

        //The Function of Confirm Button
        Bt_Confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ET_GSD.getText().toString().isEmpty()||ET_Overlap.getText().toString().isEmpty()){
                    showToast("Please input the GSD and Overlap firstly");
                }else{
                    GSD=Double.parseDouble(ET_GSD.getText().toString());
                    Overlap=Double.parseDouble(ET_Overlap.getText().toString())/100;
                    Distance=GSD*8.8/(12.8/5472)/1000;
                    LongSide=GSD*5472/1000*(1-Overlap);
                    ShortSide=GSD*3648/1000*(1-Overlap);
                    showToast("Define the parameters successfully!!");
                }
            }
        });

        //The Function of Calculate Button
        Bt_Calculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double x_min=mPoint.get(0).getX();
                double x_max=mPoint.get(0).getX();
                double Number_CrossSection;
                double addX;
                ArrayList<Double> ArraylistX=new ArrayList<>();

                for (int i=0;i<mPoint.size();i++){
                    if (x_min>mPoint.get(i).getX()){
                        x_min=mPoint.get(i).getX();
                    }
                    if (x_max<mPoint.get(i).getX()){
                        x_max=mPoint.get(i).getX();
                    }
                }
                Number_CrossSection=Math.ceil((x_max-x_min)/LongSide);
                addX=(x_max-x_min)/Number_CrossSection;
                for (int i=0;i<Number_CrossSection;i++){
                    ArraylistX.add(x_min+addX*i);
                }
                ArraylistX.add(x_max);

                for (int i=0;i<ArraylistX.size();i++){
                    Segment segment=getIntersectionPoint(ArraylistX.get(i),mPoint);
                    ArrayList<ShotPoint> shotPoints=flightRoutePlan(segment,Coordinate_Y,Distance,ShortSide);
                    if (i%2==0){
                        for (int j=0;j<shotPoints.size();j++){
                            mShotPoint.add(shotPoints.get(j));
                        }
                    }else{
                        for (int j=0;j<shotPoints.size();j++){
                            mShotPoint.add(shotPoints.get(shotPoints.size()-j-1));
                        }
                    }
                    shotPoints.clear();
                }
                showToast("Successful Flight Route Planning!");
            }
        });

        //The Function of Save Button
        Bt_Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String FileName=ET_FileName.getText().toString();
                if (FileName.isEmpty()){
                    showToast("Please input the file name first!");
                }else {
                    SaveToExcel(FileName,mShotPoint);
                    showToast("Save Successfully!!!");
                }
            }
        });

        //The Function of Load Button
        Bt_Load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file=new File(getExternalCacheDir().getAbsolutePath().toString()+File.separator+"FlightRoute");
                File[] files=file.listFiles();
                List<String> s=new ArrayList<>();
                for (int i=0;i<files.length;i++){
                    s.add(files[i].getName());
                }

                ArrayAdapter<String> adapter;
                adapter=new ArrayAdapter<String>(FlightRoutePlanning.this,android.R.layout.simple_spinner_item,s);
                Sp_RouteFile.setPrompt("Please choose route file!");
                Sp_RouteFile.setAdapter(adapter);
                Sp_RouteFile.setVisibility(View.VISIBLE);
            }
        });

        //The Function of Show Button
        Bt_Show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String filename=Sp_RouteFile.getSelectedItem().toString();
                ArrayList<ShotPoint> SShotPoint=new ArrayList<>();
                SShotPoint=LoadFromExcel(filename);
            }
        });
    }

    private void initUI(Context context){
        ET_X=findViewById(R.id.ET_X);
        ET_Y=findViewById(R.id.ET_Y);
        ET_Z=findViewById(R.id.ET_Z);
        ET_BridgeWidth=findViewById(R.id.ET_BridgeWidth);
        ET_GSD=findViewById(R.id.ET_GSD);
        ET_Overlap=findViewById(R.id.ET_Overlap);
        ET_FileName=findViewById(R.id.ET_FileName);

        Bt_New=findViewById(R.id.Bt_New);
        Bt_Delete=findViewById(R.id.Bt_Delete);
        Bt_Clean=findViewById(R.id.Bt_Clean);
        Bt_Complete=findViewById(R.id.Bt_Complete);
        Bt_Create=findViewById(R.id.Bt_Create);
        Bt_Confirm=findViewById(R.id.Bt_Confirm);
        Bt_Calculate=findViewById(R.id.Bt_Calculate);
        Bt_Save=findViewById(R.id.Bt_Save);
        Bt_Load=findViewById(R.id.Bt_Load);
        Bt_Show=findViewById(R.id.Bt_Show);

        SV_KeyPoints=findViewById(R.id.SV_KeyPoints);
        TV_KeyPoints=findViewById(R.id.TV_KeyPoints);

        RG_type=findViewById(R.id.RG_type);
        RB_L2L=findViewById(R.id.RB_L2L);
        RB_L2C=findViewById(R.id.RB_L2C);
        RB_C2L=findViewById(R.id.RB_C2L);
        RB_C2C=findViewById(R.id.RB_C2C);

        Sp_RouteFile=findViewById(R.id.Sp_RouteFile);
    }

    private void showToast(String ToastMsg){
        Toast.makeText(getBaseContext(),ToastMsg,Toast.LENGTH_LONG).show();
    }

    private void setMsgToText(ArrayList<KeyPoint> mKeyPoint){
        TV_KeyPoints.setText("");
        for (int i=0;i<mKeyPoint.size();i++){
            TV_KeyPoints.append(mKeyPoint.get(i).getInfo()+"\n");
        }
    }

    private ArrayList<Point2D> createDensePoint(Point2D point1,Point2D point2, Point2D point3){
        ArrayList<Point2D> mDensePoint=new ArrayList<>();
        double x1=point1.getX(),y1=point1.getY();
        double x2=point2.getX(),y2=point2.getY();
        double x3=point3.getX(),y3=point3.getY();
        double a=x1-x2;
        double b=y1-y2;
        double c=x1-x3;
        double d=y1-y3;
        double e=((x1*x1-x2*x2)+(y1*y1-y2*y2))/2;
        double f=((x1*x1-x3*x3)+(y1*y1-y3*y3))/2;
        double det=b*c-a*d;

        double x0=-(d*e-b*f)/det;
        double y0=-(a*f-c*e)/det;
        double radius=Math.sqrt((x0-x1)*(x0-x1)+(y0-y1)*(y0-y1));

        //Judge the direction of the given three points
        double condition=(x2-x1)*(y3-y2)-(y2-y1)*(x3-x2);
        boolean direction;
        if (condition>0){
            direction=false;
        }else{
            direction=true;
        };

        //Calculate the coordinates of dense points
        double Angle1=Math.atan2(y1-y0,x1-x0);
        double Angle3=Math.atan2(y3-y0,x3-x0);
        if (Angle1<0){
            Angle1=Angle1+2*Math.PI;
        }
        if (Angle3<0){
            Angle3=Angle3+2*Math.PI;
        }

        int N=36;
        double AddAngle=Math.abs(Angle1-Angle3)/36;

        for (int i=0;i<N+1;i++){
            if (direction){
                double x=x0+radius*Math.cos(Angle1-i*AddAngle);
                double y=y0+radius*Math.sin(Angle1-i*AddAngle);
                mDensePoint.add(new Point2D(x,y));
            }else{
                double x=x0+radius*Math.cos(Angle1+i*AddAngle);
                double y=y0+radius*Math.sin(Angle1+i*AddAngle);
                mDensePoint.add(new Point2D(x,y));
            }
        }

        return mDensePoint;
    }

    private Segment getIntersectionPoint(double x, ArrayList<Point> mPoint){
        double X1,Y1;
        double X2,Y2;
        ArrayList<Double> zz=new ArrayList<>();

        for (int i=0;i<mPoint.size();i++){
            if (i==mPoint.size()-1){
                X1=mPoint.get(i).getX();
                Y1=mPoint.get(i).getZ();
                X2=mPoint.get(0).getX();
                Y2=mPoint.get(0).getZ();
            }else {
                X1=mPoint.get(i).getX();
                Y1=mPoint.get(i).getZ();
                X2=mPoint.get(i+1).getX();
                Y2=mPoint.get(i+1).getZ();
            }
            if (X1>X2){
                double X3=X2;
                double Y3=Y2;
                X2=X1;
                Y2=Y1;
                X1=X3;
                Y1=Y3;
            }

            if (X1==X2){
                zz.add(Y1);
                zz.add(Y2);
            }else if (x>=X1&&x<=X2){
                double z=Y1+(x-X1)/(X2-X1)*(Y2-Y1);
                zz.add(z);
            }
        }

        double z1=zz.get(0);
        double z2=zz.get(0);

        for (int i=0;i<zz.size();i++){
            if (z1>zz.get(i)){
                z1=zz.get(i);
            }
            if (z2<zz.get(i)){
                z2=zz.get(i);
            }
        }
        Segment mSegment=new Segment(x,z1,z2);
        return mSegment;
    }

    private ArrayList<ShotPoint> flightRoutePlan(Segment sSegment, double CoordinateY,double distance, double shortSide){
        int Number1,Number2,Number3;
        double addZ, addAngle,addY;
        double Angle=15;
        ArrayList<ShotPoint> mShotPoint=new ArrayList<>();

        Number1=(int)Math.ceil((sSegment.getZ2()-sSegment.getZ1())/shortSide);
        addZ=(sSegment.getZ2()-sSegment.getZ1())/Number1;
        Number2=(int)Math.ceil(90/Angle);
        addAngle=90/Number2;
        Number3=(int)Math.ceil(CoordinateY*2/shortSide);
        addY=2*CoordinateY/Number3;

        for (int i=0;i<Number1;i++){
            mShotPoint.add(new ShotPoint(sSegment.getX(),CoordinateY+distance,sSegment.getZ1()+i*addZ,0,0));
        }
        for (int i=0;i<Number2;i++){
            mShotPoint.add(new ShotPoint(sSegment.getX(),CoordinateY+distance*Math.cos(i*Angle/180*Math.PI),sSegment.getZ2()+distance*Math.sin(i*Angle/180*Math.PI),0,i*addAngle));
        }
        for (int i=0;i<=Number3;i++){
            double yaw;
            if (i<Number3/2){
                yaw=0;
            }else{
                yaw=180;
            }
            mShotPoint.add(new ShotPoint(sSegment.getX(),CoordinateY-i*addY,sSegment.getZ2()+distance,yaw,90));
        }
        for (int i=0;i<Number2;i++){
            mShotPoint.add(new ShotPoint(sSegment.getX(),-(CoordinateY+distance*Math.sin((i+1)*Angle/180*Math.PI)),sSegment.getZ2()+distance*Math.cos((i+1)*Angle/180*Math.PI),180,90-(i+1)*addAngle));
        }
        for (int i=0;i<Number1;i++){
            mShotPoint.add(new ShotPoint(sSegment.getX(),-(CoordinateY+distance),sSegment.getZ2()-(i+1)*addZ,180,0));
        }

        return mShotPoint;
    }

    public void SaveToExcel(String fileName, ArrayList<ShotPoint> mShotPoint){
        String savePath=getExternalCacheDir().getAbsolutePath()+File.separator+"FlightRoute";
        File dir=new File(savePath);
        if (!dir.exists()){
            dir.mkdirs();
        }
        File file=new File(dir.toString()+File.separator+fileName+".xls");

        try{
            WritableWorkbook mWritableWorkbook= Workbook.createWorkbook(file);
            WritableSheet mWritableSheet=mWritableWorkbook.createSheet("sheet",0);

            //Create Sheet Header
            mWritableSheet.addCell(new Label(0,0,"X"));
            mWritableSheet.addCell(new Label(1,0,"Y"));
            mWritableSheet.addCell(new Label(2,0,"Z"));
            mWritableSheet.addCell(new Label(3,0,"Yaw"));
            mWritableSheet.addCell(new Label(4,0,"Pitch"));

            //Create Content
            for (int i=0;i<mShotPoint.size();i++){
                mWritableSheet.addCell(new Label(0,i+1,Double.toString(mShotPoint.get(i).getX())));
                mWritableSheet.addCell(new Label(1,i+1,Double.toString(mShotPoint.get(i).getY())));
                mWritableSheet.addCell(new Label(2,i+1,Double.toString(mShotPoint.get(i).getZ())));
                mWritableSheet.addCell(new Label(3,i+1,Double.toString(mShotPoint.get(i).getYaw())));
                mWritableSheet.addCell(new Label(4,i+1,Double.toString(mShotPoint.get(i).getPitch())));
            }

            //Write
            mWritableWorkbook.write();
            mWritableWorkbook.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public ArrayList<ShotPoint> LoadFromExcel(String fileName){
        ArrayList<ShotPoint> mShotPoint=new ArrayList<>();
        String directory= getExternalCacheDir().getAbsoluteFile().toString();
        File file=new File(directory+File.separator+"FlightRoute"+File.separator+fileName);

        try{
            Workbook mWorkbook=Workbook.getWorkbook(file);
            Sheet mSheet=mWorkbook.getSheet(0);
            int Rows=mSheet.getRows();
            int Cols=mSheet.getColumns();

            for (int i=0; i<Rows;i++){
                Double x=Double.parseDouble(mSheet.getCell(0,i+1).getContents());
                Double y=Double.parseDouble(mSheet.getCell(1,i+1).getContents());
                Double z=Double.parseDouble(mSheet.getCell(2,i+1).getContents());
                Double yaw=Double.parseDouble(mSheet.getCell(3,i+1).getContents());
                Double pitch=Double.parseDouble(mSheet.getCell(4,i+1).getContents());
                mShotPoint.add(new ShotPoint(x,y,z,yaw,pitch));
            }

            return mShotPoint;

        }catch (Exception e){
            e.printStackTrace();
        }
        return mShotPoint;
    }
}
