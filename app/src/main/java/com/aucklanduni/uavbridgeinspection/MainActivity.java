package com.aucklanduni.uavbridgeinspection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.sdkmanager.DJISDKInitEvent;
import dji.sdk.sdkmanager.DJISDKManager;

public class MainActivity extends AppCompatActivity {
    private static final String TAG=MainActivity.class.getName();
    public static final String FLAG_CONNECTION_CHANGE="dji_sdk_connection_change";
    private Handler mHandler;

    protected Button Bt_FlightRoutePlanning;
    protected Button Bt_ImageDataCollection;
    protected TextView TxV_ConnectionStatus;

    private static final String[] REQUIRED_PERMISSION_LIST = new String[]{
            Manifest.permission.VIBRATE, // Gimbal rotation
            Manifest.permission.INTERNET, // API requests
            Manifest.permission.ACCESS_WIFI_STATE, // WIFI connected products
            Manifest.permission.ACCESS_COARSE_LOCATION, // Maps
            Manifest.permission.ACCESS_NETWORK_STATE, // WIFI connected products
            Manifest.permission.ACCESS_FINE_LOCATION, // Maps
            Manifest.permission.CHANGE_WIFI_STATE, // Changing between WIFI and USB connection
            Manifest.permission.WRITE_EXTERNAL_STORAGE, // Log files
            Manifest.permission.BLUETOOTH, // Bluetooth connected products
            Manifest.permission.BLUETOOTH_ADMIN, // Bluetooth connected products
            Manifest.permission.READ_EXTERNAL_STORAGE, // Log files
            Manifest.permission.READ_PHONE_STATE, // Device UUID accessed upon registration
            Manifest.permission.RECORD_AUDIO // Speaker accessory
    };
    private List<String> missingPermission=new ArrayList<>();
    private AtomicBoolean isRegistrationInProgress=new AtomicBoolean(false);
    private static final int REQUEST_PERMISSION_CODE=12345;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkAndRequestPermissions();
        setContentView(R.layout.activity_main);
        mHandler=new Handler(Looper.getMainLooper());

        // The Function of Bt_FlightRoutePlanning
        Bt_FlightRoutePlanning=findViewById(R.id.Bt_FlightRoutePlanning);
        Bt_FlightRoutePlanning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,FlightRoutePlanning.class);
                startActivity(intent);
            }
        });

        //The Function of Bt_ImageDataCollection
        Bt_ImageDataCollection=findViewById(R.id.Bt_ImageDataCollection);
        Bt_ImageDataCollection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,ImageDataCollection.class);
                startActivity(intent);
            }
        });

    }

    private void checkAndRequestPermissions(){
        for (String eachPermission:REQUIRED_PERMISSION_LIST){
            if (ContextCompat.checkSelfPermission(this,eachPermission)!= PackageManager.PERMISSION_GRANTED) {
                missingPermission.add(eachPermission);
            }
        }
        if (missingPermission.isEmpty()){
            startSDKRegistration();
        }else if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            showToast("Need to grant the permissions");
            ActivityCompat.requestPermissions(this,missingPermission.toArray(new String[missingPermission.size()]),REQUEST_PERMISSION_CODE);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if (requestCode==REQUEST_PERMISSION_CODE){
            for (int i=grantResults.length-1;i>=0;i--){
                if (grantResults[i]==PackageManager.PERMISSION_GRANTED){
                    missingPermission.remove(permissions[i]);
                }
            }
        }
        if(missingPermission.isEmpty()){
            startSDKRegistration();
        }else {
            showToast("Missing permissions!!!");
        }
    }

    private void startSDKRegistration(){
        if (isRegistrationInProgress.compareAndSet(false,true)){
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    showToast("registering, pls wait...");
                    DJISDKManager.getInstance().registerApp(MainActivity.this.getApplicationContext(), new DJISDKManager.SDKManagerCallback() {
                        @Override
                        public void onRegister(DJIError djiError) {
                            if (djiError== DJISDKError.REGISTRATION_SUCCESS){
                                DJISDKManager.getInstance().startConnectionToProduct();
                                showToast("SDK Register Success");
                            }else{
                                showToast("Register sdk fails, please check the bundle id and network connection");
                            }
                            Log.v(TAG,djiError.getDescription());
                        }

                        @Override
                        public void onProductDisconnect() {
                            Log.d(TAG,"onProductDisconnect");
                            showToast("Product Disconnected");
                            notifyStatusChange();
                        }

                        @Override
                        public void onProductConnect(BaseProduct baseProduct) {
                            Log.d(TAG,String.format("onProductConnect newProduct:%s",baseProduct));
                            showToast("Product Connected");
                            //Change the connection status
                            TxV_ConnectionStatus=findViewById(R.id.TxV_ConnectionStatus);
                            TxV_ConnectionStatus.setText("Status: Product Connected");
                            notifyStatusChange();
                        }

                        @Override
                        public void onComponentChange(BaseProduct.ComponentKey componentKey, BaseComponent oldComponent, BaseComponent newComponent) {
                            Log.d(TAG, componentKey.toString()+"changed");
                            notifyStatusChange();
                        }

                        @Override
                        public void onInitProcess(DJISDKInitEvent djisdkInitEvent, int i) {

                        }

                        @Override
                        public void onDatabaseDownloadProgress(long current, long total) {

                        }
                    });
                }
            });
        }
    }

    private void notifyStatusChange(){
        mHandler.removeCallbacks(updateRunnable);
        mHandler.postDelayed(updateRunnable,500);
    }

    private Runnable updateRunnable=new Runnable() {
        @Override
        public void run() {
            Intent intent=new Intent(FLAG_CONNECTION_CHANGE);
            sendBroadcast(intent);
        }
    };

    private void showToast(final String toastMsg){
        Handler handler=new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),toastMsg,Toast.LENGTH_LONG).show();
            }
        });
    }

}
