package com.example.screentime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.content.Context.LOCATION_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;

public class MyAlarmService extends BroadcastReceiver {
    ArrayList<Float>speeds;

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context,"Alarm service sample your speed ",Toast.LENGTH_LONG).show();
        Log.d("ptt","from onReceive");
        LocationService.saveSpeed();




    }
}
