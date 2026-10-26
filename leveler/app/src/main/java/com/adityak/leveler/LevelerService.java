package com.adityak.leveler;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.DynamicsProcessing;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

/** Holds a DynamicsProcessing effect on the global output mix (session 0). */
public class LevelerService extends Service {
    static final String TAG = "Leveler";
    private static final String CHANNEL = "leveler";

    static volatile String status = "Not running";

    private DynamicsProcessing effect;

    /** Saves the preset and starts, re-applies or stops the service to match. */
    static void select(Context context, int preset) {
        Presets.save(context, preset);
        Intent svc = new Intent(context, LevelerService.class);
        if (preset == Presets.OFF) {
            context.stopService(svc);
        } else {
            context.startForegroundService(svc);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationManager nm = getSystemService(NotificationManager.class);
        nm.createNotificationChannel(new NotificationChannel(
                CHANNEL, "Volume Leveler", NotificationManager.IMPORTANCE_LOW));
        Notification n = new Notification.Builder(this, CHANNEL)
                .setContentTitle("Volume Leveler is on")
                .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
                .build();
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(1, n);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int preset = Presets.load(this);
        if (preset == Presets.OFF) {
            stopSelf();
            return START_NOT_STICKY;
        }
        apply(preset);
        return START_STICKY;
    }

    private void apply(int preset) {
        release();
        try {
            effect = new DynamicsProcessing(Integer.MAX_VALUE, 0, Presets.config(preset));
            effect.setControlStatusListener((e, hasControl) ->
                    Log.w(TAG, "control changed, hasControl=" + hasControl));
            int r = effect.setEnabled(true);
            status = "Active: " + Presets.NAMES[preset]
                    + (r == AudioEffect.SUCCESS ? "" : " (enable returned " + r + ")")
                    + ", control=" + effect.hasControl();
        } catch (RuntimeException e) {
            status = "Failed: " + e;
            release();
        }
        Log.i(TAG, status);
    }

    private void release() {
        if (effect != null) {
            effect.release();
            effect = null;
        }
    }

    @Override
    public void onDestroy() {
        release();
        status = "Not running";
        Log.i(TAG, status);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
