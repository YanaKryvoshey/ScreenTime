package com.example.screentime;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.Manifest;
import android.app.Activity;
import android.net.Uri;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.material.button.MaterialButton;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class MainActivity extends AppCompatActivity {
    PendingIntent pendingIntent;
    MaterialButton main_BTN_STARTALARMservice;
    MaterialButton main_BTN_STOPALARMservice;
    MaterialButton main_BTN_averageSpeed;
    MaterialButton main_BTN_restart;
    TextView main_TXT_DATEFIIL;
    TextView main_TXT_SPEEDFIIL;
    TextView main_TXT_averageSpeed;
    private static final int PERMISSION_REGULAR_LOCATION_REQUEST_CODE = 133;
    private static final int PERMISSION_BACKGROUND_LOCATION_REQUEST_CODE = 134;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        main_BTN_STARTALARMservice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMYService();
                setAlarm();
            }
        });
        main_BTN_STOPALARMservice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopMYService();
                cancelTheAlarm();
            }
        });
        main_BTN_averageSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAverageSpeed();
            }
        });
        main_BTN_restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteTheExistsArr();
            }
        });
    }

    //delete all the data in the Shared Preferences
    private void deleteTheExistsArr() {
        MSP.getInstance().deleteAll();
    }

    //take the data from Shared Preferences and count the average speed
    private void showAverageSpeed() {
        TypeToken typeToken = new TypeToken<ArrayList<Float>>() {
        };
        Float avg = 0.0f;
        ArrayList<Float> arr = MSP.getInstance().getArray(MSP.KEYS.SPEED_ARAY, typeToken);
        if (arr != null) {
            for (int i = 0; i < arr.size(); i++) {
                avg = avg + arr.get(i);
            }
            main_TXT_averageSpeed.setText("" + avg / arr.size());
        } else {
            main_TXT_averageSpeed.setText("0.0");
        }

    }

    //Stop the alarm manager
    private void cancelTheAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            Toast.makeText(MainActivity.this, "Alarm Service Cancelled", Toast.LENGTH_LONG).show();
        }
    }

    //Start the alarm manager
    private void setAlarm() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        //getting the alarm manager
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        //creating a new intent specifying the broadcast receiver
        Intent i = new Intent(this, MyAlarmService.class);

        //creating a pending intent using the intent
        pendingIntent = PendingIntent.getBroadcast(this, 0, i, 0);

        //setting the repeating alarm that will be fired every day
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1000, pendingIntent);
        Log.d("ptt", "Alarm is set");
        Toast.makeText(this, "Alarm is set", Toast.LENGTH_SHORT).show();
    }

    //Start the background service
    private void startMYService() {
        getPermission();
        // actionToService(LocationService.START_FOREGROUND_SERVICE);
    }

    //Stop the background service
    private void stopMYService() {
        actionToService(LocationService.STOP_FOREGROUND_SERVICE);
    }


    private void actionToService(String action) {
        Intent startIntent = new Intent(MainActivity.this, LocationService.class);
        startIntent.setAction(action);
        // makeAlarm();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(startIntent);
            // or
            //ContextCompat.startForegroundService(this, startIntent);
        } else {
            startService(startIntent);
        }
    }

    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showSpeedAndTime(intent);
        }
    };

    //get the data from background service and show it
    public void showSpeedAndTime(Intent intent) {
        if (intent != null) {
            Location lastLocation = intent.getParcelableExtra("EXTRA_TIME");
            Date date = new Date(lastLocation.getTime());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss a");
            main_TXT_SPEEDFIIL.setText("" + lastLocation.getSpeed());
            main_TXT_DATEFIIL.setText("" + sdf.format(date));
            Log.d("pttt", "showSpeedAndTime date:" + sdf.format(date) + "speed:" + lastLocation.getSpeed());
        }
    }

    private void getPermission() {
        boolean per1 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean per2 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean per3 = android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (!per1 || !per2) {
            // if i can ask for permission
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else if (!per3) {
            // if i can ask for permission
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        } else {
            actionToService(LocationService.START_FOREGROUND_SERVICE);
            //getLocation();
        }
    }

    private ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    actionToService(LocationService.START_FOREGROUND_SERVICE);
                } else {
                    getPermissionManually();
                }
            });

    //get Permission Manually
    private void getPermissionManually() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        manuallyPermissionResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> manuallyPermissionResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        actionToService(LocationService.START_FOREGROUND_SERVICE);
                    }
                }
            });

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(LocationService.BROADCAST_NEW_SCREEN_TIME);
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver, intentFilter);
        registerReceiver(myReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
    }

    private void findViews() {
        main_BTN_STARTALARMservice = findViewById(R.id.main_BTN_STARTALARMservice);
        main_BTN_STOPALARMservice = findViewById(R.id.main_BTN_STOPALARMservice);
        main_BTN_averageSpeed = findViewById(R.id.main_BTN_averageSpeed);
        main_TXT_DATEFIIL = findViewById(R.id.main_TXT_DATEFIIL);
        main_TXT_SPEEDFIIL = findViewById(R.id.main_TXT_SPEEDFIIL);
        main_TXT_averageSpeed = findViewById(R.id.main_TXT_averageSpeed);
        main_BTN_restart = findViewById(R.id.main_BTN_restart);
    }
}