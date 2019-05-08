package com.example.diegocasas.powertest;

import android.Manifest;
import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.RequiresApi;

import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import java.text.SimpleDateFormat;
import java.util.Calendar;

import services.NotificationServices;
import services.TestServices;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

public class MainActivity extends Activity {

    Button startTest;
    TextView nivelBateriaInicial, horaInicial, frec_txt;
    Chronometer chronometer;
    Calendar calendar;
    SimpleDateFormat simpleDateFormat;
    //EditText frecuencia;
    String time;
    int brightness = 255;

    private TextView mBatteryLevelText;
    private ProgressBar mBatteryLevelProgress;
    private BroadcastReceiver mReceiver;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(MainActivity.this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //cronómetro
        chronometer = (Chronometer)findViewById(R.id.chronometertxt);
        startTest = (Button) findViewById(R.id.buttonIniciar);

        //frecuencia
        //frecuencia = (EditText)findViewById(R.id.frecuenciTxt);

        //Brightness
      boolean settingsCanWrite = Settings.System.canWrite(MainActivity.this);
        if (!settingsCanWrite){
            Log.d("Brightness", "No se tiene permisos de escritura de opciones");
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            startActivity(intent);
        } else {
            Log.d("Brightness", "Si se tuvo permisos");
            ContentResolver cResolver = this.getApplicationContext().getContentResolver();
            Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, brightness/2);
        }

        //TxtFrecuencia
        //frec_txt = (TextView)findViewById(R.id.fre_txt);

        //hora inicial
        horaInicial = (TextView)findViewById(R.id.horaTxt);

        //nivel de bateria inicial
        nivelBateriaInicial = (TextView)findViewById(R.id.nivelBateriaTxt);

        //nivel actual de bateria
        mBatteryLevelText = (TextView) findViewById(R.id.nivelActualBateriaTxt);
        mBatteryLevelProgress = (ProgressBar) findViewById(R.id.progressBar);
        mReceiver = new BatteryBroadcastReceiver();

        startTest.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                int frec;
                //frec = Integer.parseInt(frecuencia.getText().toString());
                startJobTask();
                //startJobNotification();
                startChronometer();
                levelBattery();
                horaInicial();
                //checkLowLevelBattery();
                //frecuencia.setVisibility(View.INVISIBLE);
                //frec_txt.setVisibility(View.VISIBLE);
                //frec_txt.setText(frecuencia.getText().toString());
            }
        });
    }
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
    public static int getBatteryPercentage(Context context) {

        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, iFilter);

        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

        float batteryPct = level / (float) scale;

        return (int) (batteryPct * 100);
    }
    public void startChronometer(){
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();

        chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                //if( chronometer.getText().toString().equalsIgnoreCase("8:00:00")){
                if( chronometer.getText().toString().equalsIgnoreCase("05:00")){
                    Toast.makeText(MainActivity.this, "Primer periodo", Toast.LENGTH_LONG).show();
                    //chronometer.stop();
                    //mBatteryLevelText.setText(String.valueOf(getBatteryPercentage(MainActivity.this)) + "%");
                    //mBatteryLevelProgress.setProgress(getBatteryPercentage(MainActivity.this));
                    playRingtone();
                } else if (chronometer.getText().toString().equalsIgnoreCase("10:00")){
                    Toast.makeText(MainActivity.this, "Segundo periodo", Toast.LENGTH_LONG).show();
                    playRingtone();
                }else if (chronometer.getText().toString().equalsIgnoreCase("15:00")){
                    Toast.makeText(MainActivity.this, "Final", Toast.LENGTH_LONG).show();
                    playRingtone();
                    chronometer.stop();
                }
            }
        });
    }
    public void levelBattery(){
        nivelBateriaInicial.setText(String.valueOf(getBatteryPercentage(MainActivity.this)) + "%");
    }
    public void horaInicial(){
        calendar = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        time = simpleDateFormat.format(calendar.getTime());

        horaInicial.setText(String.valueOf(time));
    }
    public void playAlarm(){
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(),uri);
        ringtone.play();

    }
    public void playRingtone(){
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void checkLowLevelBattery(){
        int levelLowBattery;
        levelLowBattery = getBatteryPercentage(MainActivity.this);
        if (levelLowBattery <= 1){
            Toast.makeText(MainActivity.this, "Nivel muy bajo de batería, prueba terminada", Toast.LENGTH_LONG).show();
            playAlarm();
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startJobTask(){
        final long REFRESH_INTERVAL  = 300 * 1000; // 5 minutos
        ComponentName componentName = new ComponentName(MainActivity.this, TestServices.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
          JobInfo  info = new JobInfo.Builder(123, componentName)
                    .setMinimumLatency(REFRESH_INTERVAL)
                    .build();
            JobScheduler scheduler = (JobScheduler)getSystemService(JOB_SCHEDULER_SERVICE);
            int resultCode = scheduler.schedule(info);
            if (resultCode == JobScheduler.RESULT_SUCCESS){
                Log.d("MainActivity", "Job Schedule JobTask >= N");

            } else {
                Log.d("MainActivity", "Job Scheduling failed");
            }
        }else {
            JobInfo info = new JobInfo.Builder(123,componentName)
                    .setRequiresCharging(true)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                    .setPersisted(true)
                    .setPeriodic(MINUTES.toMillis(5))
                    .build();
            JobScheduler scheduler = (JobScheduler)getSystemService(JOB_SCHEDULER_SERVICE);
            int resultCode = scheduler.schedule(info);
            if (resultCode == JobScheduler.RESULT_SUCCESS){
                Log.d("MainActivity", "Job Schedule JobTask < N");
            } else {
                Log.d("MainActivity", "Job Scheduling failed");
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startJobNotification(){
        final long REFRESH_INTERVAL  = 60 * 1000; // 30min
        ComponentName componentName = new ComponentName(MainActivity.this, NotificationServices.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            JobInfo  info = new JobInfo.Builder(456, componentName)
                    .setMinimumLatency(REFRESH_INTERVAL)
                    .build();
            JobScheduler scheduler = (JobScheduler)getSystemService(JOB_SCHEDULER_SERVICE);
            int resultCode = scheduler.schedule(info);
            if (resultCode == JobScheduler.RESULT_SUCCESS){
                Log.d("MainActivity", "Job Schedule JobNotification >= N");
            } else {
                Log.d("MainActivity", "Job Scheduling failed");
            }
        } else {
            JobInfo info = new JobInfo.Builder(456,componentName)
                    .setRequiresCharging(true)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                    .setPersisted(true)
                    .setPeriodic(MINUTES.toMillis(1))
                    .build();
            JobScheduler scheduler = (JobScheduler)getSystemService(JOB_SCHEDULER_SERVICE);
            int resultCode = scheduler.schedule(info);
            if (resultCode == JobScheduler.RESULT_SUCCESS){
                Log.d("MainActivity", "Job Schedule JobNotification < N");
            } else {
                Log.d("MainActivity", "Job Scheduling failed");
            }
        }
    }
    @Override
    protected void onStart() {
        registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        super.onStart();
    }
    @Override
    protected void onStop() {
        unregisterReceiver(mReceiver);
        super.onStop();
    }
    private class BatteryBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            if (level >= 1) {
                mBatteryLevelText.setText(level + "%");
                mBatteryLevelProgress.setProgress(level);
            } else {
                Toast.makeText(MainActivity.this, "Nivel muy bajo de batería, prueba terminada", Toast.LENGTH_LONG).show();
                playAlarm();
            }
        }
    }
}