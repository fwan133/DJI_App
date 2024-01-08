package com.aucklanduni.uavbridgeinspection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import CustomClassPackage.Coordinate3D;
import CustomClassPackage.FlightShotPoint;
import CustomClassPackage.Point;
import CustomClassPackage.ShotPoint;
import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.LocationCoordinate3D;
import dji.common.gimbal.Attitude;
import dji.common.model.LocationCoordinate2D;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.mission.MissionControl;
import dji.sdk.mission.timeline.TimelineElement;
import dji.sdk.mission.timeline.actions.AircraftYawAction;
import dji.sdk.mission.timeline.actions.GimbalAttitudeAction;
import dji.sdk.mission.timeline.actions.GoHomeAction;
import dji.sdk.mission.timeline.actions.GoToAction;
import dji.sdk.mission.timeline.actions.ShootPhotoAction;
import dji.sdk.mission.timeline.actions.TakeOffAction;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;
import jxl.Sheet;
import jxl.Workbook;

public class ImageDataCollection extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMissionMap;

    private EditText EdT_Point1Latitude;
    private EditText EdT_Point1Longitude;
    private Button Btn_GetP1Location;
    private Button Btn_SetP1Location;
    private EditText EdT_Point2Latitude;
    private EditText EdT_Point2Longitude;
    private Button Btn_GetP2Location;
    private Button Btn_SetP2Location;
    private Button Btn_HomePoint;
    private Button Btn_CreateMission;
    private Button Btn_StartMission;
    private Button Btn_PauseMission;
    private Button Btn_ResumeMission;
    private Button Btn_StopMission;
    private Button Btn_GoHome;
    private Button Btn_Continue;
    private Spinner Sp_FileName;
    private Button Btn_Browser;
    private Button Btn_Load;

    private FlightController mFlightController;
    private MissionControl mMissionControl;
    private List<TimelineElement> elements=new ArrayList<>();

    private ArrayList<ShotPoint> mShotPoint=new ArrayList<>();
    private ArrayList<FlightShotPoint> TotalMissionPoint=new ArrayList<>();

    private Marker markerP1;
    private Marker markerP2;
    private Marker markerHomepoint;
    private Marker TargetMarker;
    private Marker droneMarker;
    List<Marker> TargetElement=new ArrayList<>();
    private double P1_Latitude;
    private double P1_Longitude;
    private double P2_Latitude;
    private double P2_Longitude;
    private double Home_Latitude;
    private double Home_Longitude;
    private final double R_equator=6378137;
    private final double R_pole=6356752.31424518;
    private float heading;
    private int InterruptIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_data_collection);
        initUI(ImageDataCollection.this);

        //Initial Google Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.MissionMap);
        mapFragment.getMapAsync(this);

        //Initialize the Flight Controller
        initFlightController();

        //The Function of Btn_Browser
        Btn_Browser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file=new File(getExternalCacheDir().getAbsolutePath().toString()+File.separator+"FlightRoute");
                File[] files=file.listFiles();
                List<String> s=new ArrayList<>();
                for (int i=0;i<files.length;i++){
                    s.add(files[i].getName());
                }

                ArrayAdapter<String> adapter;
                adapter=new ArrayAdapter<String>(ImageDataCollection.this,android.R.layout.simple_spinner_item,s);
                Sp_FileName.setPrompt("Please choose route file!");
                Sp_FileName.setAdapter(adapter);
                Sp_FileName.setVisibility(View.VISIBLE);
            }
        });

        //The Function of Btn_Load
        Btn_Load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String filename=Sp_FileName.getSelectedItem().toString();
                mShotPoint=LoadFromExcel(filename);
            }
        });

        //Obtain the location of Point1
        Btn_GetP1Location.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if (mFlightController!=null){
                    FlightControllerState flightControllerState=mFlightController.getState();
                    double Loc_Lat=flightControllerState.getAircraftLocation().getLatitude();
                    double Loc_Log=flightControllerState.getAircraftLocation().getLongitude();
                    DecimalFormat df=new DecimalFormat("0.0000000");
                    EdT_Point1Latitude.setText(""+df.format(Loc_Lat));
                    EdT_Point1Longitude.setText(""+df.format(Loc_Log));
                }else {
                    showToast("Please connect this device to the drone first.");
                }
            }
        });

        //Set the location of Point1
        Btn_SetP1Location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( EdT_Point1Latitude.getText().toString().isEmpty()||EdT_Point1Longitude.getText().toString().isEmpty()){
                    showToast("Please input or obtain the location of RefPoint 1");
                }else{
                    P1_Latitude=Double.parseDouble(EdT_Point1Latitude.getText().toString());
                    P1_Longitude=Double.parseDouble(EdT_Point1Longitude.getText().toString());
                    if (markerP1==null){
                        markerP1=RefPointMarker(P1_Latitude,P1_Longitude,"RefPoint1");
                    }else{
                        markerP1.remove();
                        markerP1=RefPointMarker(P1_Latitude,P1_Longitude,"RefPoint1");
                    }
                }
            }
        });

        //Obtain the location of Point2
        Btn_GetP2Location.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if (mFlightController!=null){
                    FlightControllerState flightControllerState=mFlightController.getState();
                    double Loc_Lat=flightControllerState.getAircraftLocation().getLatitude();
                    double Loc_Log=flightControllerState.getAircraftLocation().getLongitude();
                    DecimalFormat df=new DecimalFormat("0.0000000");
                    EdT_Point2Latitude.setText(""+df.format(Loc_Lat));
                    EdT_Point2Longitude.setText(""+df.format(Loc_Log));
                }else {
                    showToast("Please connect this device to the drone first.");
                }
            }
        });

        //Set the location of Point2
        Btn_SetP2Location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (EdT_Point2Latitude.getText().toString().isEmpty()||EdT_Point2Longitude.getText().toString().isEmpty()){
                    showToast("Please input or obtain the location of RefPoint 2");
                }else{
                    P2_Latitude=Double.parseDouble(EdT_Point2Latitude.getText().toString());
                    P2_Longitude=Double.parseDouble(EdT_Point2Longitude.getText().toString());
                    if (markerP2==null){
                        markerP2=RefPointMarker(P2_Latitude,P2_Longitude,"RefPoint1");
                    }else{
                        markerP2.remove();
                        markerP2=RefPointMarker(P2_Latitude,P2_Longitude,"RefPoint1");
                    }
                }
            }
        });

        //Set the HomePoint Location
        Btn_HomePoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFlightController!=null){
                    FlightControllerState flightControllerState=mFlightController.getState();
                    Home_Latitude=flightControllerState.getAircraftLocation().getLatitude();
                    Home_Longitude=flightControllerState.getAircraftLocation().getLongitude();
                    if (markerHomepoint==null){
                        markerHomepoint=HomePointMarker(Home_Latitude,Home_Longitude,"HomePoint");
                    }else{
                        markerHomepoint.remove();
                        markerHomepoint=HomePointMarker(Home_Latitude,Home_Longitude,"HomePoint");
                    }
                }else {
                    showToast("Please connect this device to a drone and put the drone at the start off point.");
                }
            }
        });

        //Define the function of Button CreateMission
        Btn_CreateMission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Create Flight Shot Points
                double Center_Lat=(P1_Latitude+P2_Latitude)/2;
                double Center_Log=(P1_Longitude+P2_Longitude)/2;
                double Center_Alt=0;
                double Angle=GetAngle(P1_Latitude, P1_Longitude, P2_Latitude, P2_Longitude);
                for (int i=1;i<mShotPoint.size();i++){
                    //Transfer the XYZ to LLA
                    Point TargetPoint=new Point(mShotPoint.get(i).getX(),mShotPoint.get(i).getY(),mShotPoint.get(i).getZ());
                    Coordinate3D TargetCoordinate3D=XYZ2LLA(new Coordinate3D(Center_Lat,Center_Log,Center_Alt),Angle,TargetPoint);
                    //Transfer the Yaw
                    double Yaw=mShotPoint.get(i).getYaw();
                    double TargetYaw=Yaw+Angle;
                    if (Yaw==180){
                        TargetYaw=Yaw+Angle-360;
                    }
                    //Get the Pitch
                    double TargetPitch=mShotPoint.get(i).getPitch();

                    //Get TotalMissionPoint
                    TotalMissionPoint.add(new FlightShotPoint(TargetCoordinate3D.getLatitude(),TargetCoordinate3D.getLongitude(),TargetCoordinate3D.getAltitude(),TargetYaw,TargetPitch));

                    //Show the Flight Route in Google Map
                    TargetMarker=TargetPointMarker(TargetCoordinate3D.getLatitude(),TargetCoordinate3D.getLongitude(),TargetYaw);
                    TargetElement.add(i,TargetMarker);
                }
            }
        });

        //The Function of Start Button
        Btn_StartMission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initFlightController();
                elements.clear();
                //Step 1: Take off and fly to the defined altitude
                elements.add(new TakeOffAction());
                elements.add(new GoToAction(new LocationCoordinate2D(Home_Latitude,Home_Longitude),30.0f));

                //Step 2:Fly to the target point and take a photo
                for (int i=0;i<TotalMissionPoint.size();i++){
                    double Target_Lat=TotalMissionPoint.get(i).getLatitude();
                    double Target_Log=TotalMissionPoint.get(i).getLongitude();
                    float Target_Alt=double2float(TotalMissionPoint.get(i).getAltitude());
                    float Target_Yaw=double2float(TotalMissionPoint.get(i).getYaw());
                    float Target_Pitch=double2float(TotalMissionPoint.get(i).getPitch());
                    // Create Mission
                    elements.add(new GoToAction(new LocationCoordinate2D(Target_Lat,Target_Log),Target_Alt));
                    AircraftYawAction mAircraftYawAction=new AircraftYawAction(Target_Yaw,true);
                    elements.add(mAircraftYawAction);
                    Attitude mAttitude=new Attitude(Target_Pitch,Attitude.NO_ROTATION,Attitude.NO_ROTATION);
                    GimbalAttitudeAction mGimbalAttitudeAction=new GimbalAttitudeAction(mAttitude);
                    elements.add(mGimbalAttitudeAction);
                    elements.add(ShootPhotoAction.newShootSinglePhotoAction());
                }

                //Step 3: GoHome
                elements.add(new GoToAction(new LocationCoordinate2D(Home_Latitude,Home_Longitude),30.0f));
                elements.add(new GoHomeAction());

                //Execute the Mission
                mMissionControl.unscheduleEverything();
                mMissionControl.scheduleElements(elements);
                mMissionControl.startTimeline();

                //Update the location of Aircraft
                mFlightController.setStateCallback(new FlightControllerState.Callback() {
                    @Override
                    public void onUpdate(@NonNull FlightControllerState flightControllerState) {
                        LocationCoordinate3D AircraftCoordinate=flightControllerState.getAircraftLocation();
                        heading=mFlightController.getCompass().getHeading();
                        updateDroneLocation(AircraftCoordinate.getLatitude(),AircraftCoordinate.getLongitude(),heading);
                    }
                });
            }
        });

        //The Function of Pause Button
        Btn_PauseMission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MissionControl.getInstance().isTimelineRunning()){
                    mMissionControl.pauseTimeline();
                    showToast("The mission has been paused successfully");
                }else {
                    showToast("No Current Mission being executed");
                }
            }
        });

        //The Function of Resume Button
        Btn_ResumeMission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MissionControl.getInstance().isTimelinePaused()){
                    mMissionControl.resumeTimeline();
                    showToast("Continue with the current mission");
                }else {
                    showToast("No paused Mission");
                }
            }
        });

        //Define the function of Stop Mission Button
        Btn_StopMission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MissionControl.getInstance().isTimelineRunning()){
                    int Index=mMissionControl.getCurrentTimelineMarker();
                    InterruptIndex=(int) Math.floor((Index+1-2)/4);
                    mMissionControl.stopTimeline();
                    showToast("The mission has been stopped successfully");
                }else {
                    showToast("No Current Mission being executed");
                }
            }
        });

        //Define the function of Go Home Button
        Btn_GoHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMissionControl==null){
                    showToast("No mission being executed");
                }else{
                    if (MissionControl.getInstance().isTimelineRunning()){
                        mMissionControl.stopTimeline();
                        mFlightController.setHomeLocation(new LocationCoordinate2D(Home_Latitude, Home_Longitude), new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                if (djiError==null){
                                }
                                else{
                                    showToast(djiError.getDescription());
                                }
                            }
                        });
                        mFlightController.startGoHome(new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                if (djiError==null){
                                    showToast("Successful");
                                }
                                else{
                                    showToast(djiError.getDescription());
                                }
                            }
                        });
                    }else{
                        mFlightController.setHomeLocation(new LocationCoordinate2D(Home_Latitude, Home_Longitude), new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                if (djiError==null){
                                }
                                else{
                                    showToast(djiError.getDescription());
                                }
                            }
                        });
                        mFlightController.startGoHome(new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                if (djiError==null){
                                    showToast("Successful");
                                }
                                else{
                                    showToast(djiError.getDescription());
                                }
                            }
                        });
                    }
                }
            }
        });

        //The Function of Continue Button
        Btn_Continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initFlightController();
                elements.clear();
                //Step 1: Take off and fly to the defined altitude
                elements.add(new TakeOffAction());
                elements.add(new GoToAction(new LocationCoordinate2D(Home_Latitude,Home_Longitude),30.0f));

                //Step 2:Fly to the target point and take a photo
                for (int i=InterruptIndex;i<TotalMissionPoint.size();i++){
                    double Target_Lat=TotalMissionPoint.get(i).getLatitude();
                    double Target_Log=TotalMissionPoint.get(i).getLongitude();
                    float Target_Alt=double2float(TotalMissionPoint.get(i).getAltitude());
                    float Target_Yaw=double2float(TotalMissionPoint.get(i).getYaw());
                    float Target_Pitch=double2float(TotalMissionPoint.get(i).getPitch());
                    // Create Mission
                    elements.add(new GoToAction(new LocationCoordinate2D(Target_Lat,Target_Log),Target_Alt));
                    AircraftYawAction mAircraftYawAction=new AircraftYawAction(Target_Yaw,true);
                    elements.add(mAircraftYawAction);
                    Attitude mAttitude=new Attitude(Target_Pitch,Attitude.NO_ROTATION,Attitude.NO_ROTATION);
                    GimbalAttitudeAction mGimbalAttitudeAction=new GimbalAttitudeAction(mAttitude);
                    elements.add(mGimbalAttitudeAction);
                    elements.add(ShootPhotoAction.newShootSinglePhotoAction());
                }

                //Step 3: GoHome
                elements.add(new GoToAction(new LocationCoordinate2D(Home_Latitude,Home_Longitude),30.0f));
                elements.add(new GoHomeAction());

                //Execute the Mission
                mMissionControl.unscheduleEverything();
                mMissionControl.scheduleElements(elements);
                mMissionControl.startTimeline();

                //Update the location of Aircraft
                mFlightController.setStateCallback(new FlightControllerState.Callback() {
                    @Override
                    public void onUpdate(@NonNull FlightControllerState flightControllerState) {
                        LocationCoordinate3D AircraftCoordinate=flightControllerState.getAircraftLocation();
                        heading=mFlightController.getCompass().getHeading();
                        updateDroneLocation(AircraftCoordinate.getLatitude(),AircraftCoordinate.getLongitude(),heading);
                    }
                });
            }
        });
    }

    private void initUI(Context context){
        EdT_Point1Latitude=(EditText)findViewById(R.id.EdT_Point1Latitude);
        EdT_Point1Longitude=(EditText)findViewById(R.id.EdT_Point1Longitude);
        Btn_GetP1Location=(Button)findViewById(R.id.Btn_GetP1Location);
        Btn_SetP1Location=(Button)findViewById(R.id.Btn_SetP1Location);
        EdT_Point2Latitude=(EditText)findViewById(R.id.EdT_Point2Latitude);
        EdT_Point2Longitude=(EditText)findViewById(R.id.EdT_Point2Longitude);
        Btn_GetP2Location=(Button)findViewById(R.id.Btn_GetP2Location);
        Btn_SetP2Location=(Button)findViewById(R.id.Btn_SetP2Location);
        Btn_HomePoint=(Button)findViewById(R.id.Btn_HomePoint);
        Btn_CreateMission=(Button)findViewById(R.id.Btn_CreateMission);
        Btn_StartMission=(Button)findViewById(R.id.Btn_StartMission);
        Btn_PauseMission=(Button)findViewById(R.id.Btn_PauseMission);
        Btn_ResumeMission=(Button)findViewById(R.id.Btn_ResumeMission);
        Btn_StopMission=(Button)findViewById(R.id.Btn_StopMission);
        Btn_GoHome=(Button)findViewById(R.id.Btn_GoHome);
        Btn_Continue=(Button)findViewById(R.id.Btn_Continue);
        Btn_Browser=findViewById(R.id.Btn_Browser);
        Btn_Load=findViewById(R.id.Btn_Load);
        Sp_FileName=findViewById(R.id.Sp_FileName);
    }

    private void initFlightController(){
        BaseProduct product= DJISDKManager.getInstance().getProduct();
        if (product!=null){
            mFlightController=((Aircraft)product).getFlightController();
            mMissionControl=MissionControl.getInstance();
        }else{
            showToast("No connection with drone");
        }
    }

    private void showToast(String ToastMsg){
        Toast.makeText(getBaseContext(),ToastMsg,Toast.LENGTH_LONG).show();
    }

    public void onMapReady(GoogleMap googleMap){
        if (mMissionMap==null){
            mMissionMap=googleMap;
        }
        mMissionMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMissionMap.setMyLocationEnabled(true);
    }

    public ArrayList<ShotPoint> LoadFromExcel(String fileName){
        ArrayList<ShotPoint> mShotPoint=new ArrayList<>();
        String directory= getExternalCacheDir().getAbsoluteFile().toString();
        File file=new File(directory+File.separator+"FlightRoute"+File.separator+fileName);

        try{
            Workbook mWorkbook=Workbook.getWorkbook(file);
            Sheet mSheet=mWorkbook.getSheet(0);
            int Rows=mSheet.getRows();

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

    private Marker RefPointMarker(Double Latitude,Double Longitude, String title){
        LatLng point=new LatLng(Latitude,Longitude);
        Marker marker=mMissionMap.addMarker(new MarkerOptions().position(point).title(title));
        mMissionMap.moveCamera(CameraUpdateFactory.newLatLng(point));
        mMissionMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));
        return marker;
    }

    private Marker HomePointMarker(Double Latitude,Double Longitude, String title){
        LatLng point=new LatLng(Latitude,Longitude);
        Marker marker=mMissionMap.addMarker(new MarkerOptions().position(point).title(title).icon(BitmapDescriptorFactory.fromResource(R.drawable.home)).anchor(0.5f,0.5f));
        mMissionMap.moveCamera(CameraUpdateFactory.newLatLng(point));
        mMissionMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));
        return marker;
    }

    private Marker TargetPointMarker(Double Latitude, Double Longitude, Double Direction){
        LatLng target=new LatLng(Latitude,Longitude);
        Direction=360+90-Direction;
        String str_direction=Double.toString(Direction);
        float direction=Float.parseFloat(str_direction);
        Marker marker=mMissionMap.addMarker(new MarkerOptions().position(target).rotation(direction).anchor(0.5f,0.5f).icon(BitmapDescriptorFactory.fromResource(R.drawable.aircraft)).flat(true));
        return (marker);
    }

    private double GetAngle(double Lat1,double Log1,double Lat2,double Log2){
        double Log1rad=Log1/180*Math.PI;
        double Log2rad=Log2/180*Math.PI;

        // Calculate the vertical distance between P1 and P2
        double Point1_xy[]=Getx0y0(Lat1);
        double Point2_xy[]=Getx0y0(Lat2);

        Double Dis_North=Math.sqrt(Math.pow(Point1_xy[0]-Point2_xy[0], 2)+Math.pow(Point1_xy[1]-Point2_xy[1], 2));
        if(Lat1==Math.max(Lat1, Lat2)) {
            Dis_North=-Dis_North;
        }
        double Dis_East=Point1_xy[0]*(Math.abs(Log1rad-Log2rad));
        if(Log1==Math.max(Log1, Log2)) {
            Dis_East=-Dis_East;
        }
        Double Angle=Math.atan(Dis_North/Dis_East)*180/Math.PI;
        return(Angle);
    }

    private double [] Getx0y0(double Lat) {
        double Result[]=new double [2];
        double Latrad=Lat*Math.PI/180;
        double para1=Math.sqrt(Math.pow(R_equator,2)+Math.pow(R_pole,2)*Math.pow(Math.tan(Latrad),2));
        double Result_x0=Math.pow(R_equator,2)/para1;
        Result[0]=Result_x0;
        double para2=Math.sqrt(Math.pow(R_pole,2)+Math.pow(R_equator,2)*Math.pow((1/Math.tan(Latrad)),2));
        double Result_y0=Math.pow(R_pole,2)/para2;
        if (Lat<0) {
            Result[1]=-Result_y0;
        }else {
            Result[1]=Result_y0;
        }
        return(Result);
    }

    private Coordinate3D XYZ2LLA(Coordinate3D RefCoordinate3D, Double Angle, Point mPoint){
        double Center_Lat=RefCoordinate3D.getLatitude();
        double Center_Log=RefCoordinate3D.getLongitude();
        double Center_Alt=RefCoordinate3D.getAltitude();
        double AngleRad=Angle/180*Math.PI;
        double x=mPoint.getX();
        double y=mPoint.getY();
        double z=mPoint.getZ();

        double TargetPoint_x=x*Math.cos(AngleRad)-y*Math.sin(AngleRad);
        double TargetPoint_y=y*Math.cos(AngleRad)+x*Math.sin(AngleRad);

        //Calculate the x0, y0 of CenterPoint
        double Center_LatRad=Center_Lat*Math.PI/180;
        double para1=Math.sqrt(Math.pow(R_equator,2)+Math.pow(R_pole,2)*Math.pow(Math.tan(Center_LatRad),2));
        double Result_x0=Math.pow(R_equator,2)/para1;
        double CenterPoint_x0=Result_x0;
        double para2=Math.sqrt(Math.pow(R_pole,2)+Math.pow(R_equator,2)*Math.pow((1/Math.tan(Center_LatRad)),2));
        double Result_y0=Math.pow(R_pole,2)/para2;
        double CenterPoint_y0;
        if (Center_Lat<0) {
            CenterPoint_y0=-Result_y0;
        }else {
            CenterPoint_y0=Result_y0;
        }

        //Calculate x0, y0 of TargetPoint
        double TargetPoint_x0=CenterPoint_x0-TargetPoint_y*Math.sin(Center_LatRad);
        double TargetPoint_y0=CenterPoint_y0+TargetPoint_y*Math.cos(Center_LatRad);

        //Calculate the Lat of TargetPoint
        double TargetPoint_Lat=Math.atan((TargetPoint_y0*R_equator*R_equator)/(TargetPoint_x0*R_pole*R_pole))*180/Math.PI;

        //Calculate the Log of TargetPoint
        double TargetPoint_Log=Center_Log+Math.atan(TargetPoint_x/CenterPoint_x0)*180/Math.PI;

        //Calculate the Ati of TargetPoint
        double TargetPoint_Alt=z+Center_Alt;

        Coordinate3D TargetPoint=new Coordinate3D(TargetPoint_Lat,TargetPoint_Log,TargetPoint_Alt);
        return TargetPoint;
    }

    private float double2float(double Angle_double){
        String Angle_string=Double.toString(Angle_double);
        float Angle_float=Float.parseFloat(Angle_string);
        return(Angle_float);
    }

    private void updateDroneLocation(double Latitude, double Longitude, float heading){
        final LatLng pos=new LatLng(Latitude,Longitude);
        if (heading<0){
            heading=360+heading;
        }
        final float mHeading=heading;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (droneMarker!=null) {
                    droneMarker.remove();
                }
                droneMarker=mMissionMap.addMarker(new MarkerOptions().position(pos).rotation(mHeading).anchor(0.5f,0.5f).icon(BitmapDescriptorFactory.fromResource(R.drawable.drone)).flat(true).zIndex(1.0f));
            }
        });
    }
}
