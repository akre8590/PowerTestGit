package services;

import android.annotation.SuppressLint;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;
import android.support.v4.app.ActivityCompat;

import com.example.diegocasas.powertest.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class TestServices extends JobService {
    private static final String TAG = "ExampleJobService";
    private boolean jobCancelled = false;

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "Job Started");
        doBackgroundWork(params);
        return true;
    }
    private void doBackgroundWork(final JobParameters params) {

        new Thread(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                    copyFile();
                    Log.d(TAG, getBatteryPercentage(TestServices.this) + "%");
                if (jobCancelled){
                    return;
                }
                try {
                    Thread.sleep(0);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                Log.d(TAG, "Job finished");
                jobFinished(params, false);
            }
        }).start();
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Job cancelled before completion");
        jobCancelled = true;
        return true;
    }

    public static int getBatteryPercentage(Context context) {

        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, iFilter);

        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

        float batteryPct = level / (float) scale;

        return (int) (batteryPct * 100);
    }

    public void copyFile() {
        String storageB = "/storage/emulated/0/Download";
        File targetLocation = new File(storageB + "/prueba.txt");

        Log.v(TAG, "Source Location: /res/raw");
        Log.v(TAG, "Target Location: " + targetLocation);

        try {

            InputStream in = getResources().openRawResource(R.raw.prueba);
            OutputStream out = new FileOutputStream(targetLocation);

            byte[] buf = new byte[1024];
            int len;

            while ((len = in.read(buf)) > 0){
                out.write(buf, 0, len);
            }
            in.close();
            out.close();

            Log.v(TAG, "Archivo copiado exitosamente");

        }catch (NullPointerException e){
            Log.v(TAG, "Falló copia de archivo. Perdida de archivo fuente");
            e.printStackTrace();
        } catch (Exception e){
            Log.v(TAG, "Falló copia de archivo. Perdida de archivo fuente");
            e.printStackTrace();
        }
    }
}
