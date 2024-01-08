package com.aucklanduni.uavbridgeinspection;

import android.app.Activity;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.View;

import dji.sdk.sdkmanager.DJISDKManager;

public class DJIConnectionControlActivity extends Activity {

    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setContentView(new View(this));

        Intent usbIntent=getIntent();
        if (usbIntent != null){
            String action = usbIntent.getAction();
            if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(action)){
                Intent attachedIntent=new Intent();
                attachedIntent.setAction(DJISDKManager.USB_ACCESSORY_ATTACHED);
                sendBroadcast(attachedIntent);
            }
        }

        finish();
    }
}

