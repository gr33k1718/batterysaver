package application.com.batterysaver;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import java.util.Calendar;

public class LogService extends Service {

    private PowerManager powerManager;
    private WakeLock wakeLock;
    private DatabaseLogger database;

    @Override
    public void onCreate() {
        super.onCreate();

        database = new DatabaseLogger(this);

        powerManager = (PowerManager) getBaseContext().getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");

        wakeLock.acquire();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logContext();

        stopSelf();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        wakeLock.release();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void logContext() {
        int isCharging, period, day, brightness, timeOut, status, batteryLevel;
        long interactionTime, screenOnTime, start;
        long[] trafficStats;
        int cpuLoad;
        SystemContext systemContext;

        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, -1);

        period = cal.get(Calendar.HOUR_OF_DAY);
        day = cal.get(Calendar.DAY_OF_WEEK);
        batteryLevel = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        trafficStats = NetworkMonitor.getTrafficStats();
        cpuLoad = CpuMonitor.getLoad();
        brightness = DisplayMonitor.screenBrightness();
        timeOut = DisplayMonitor.screenTimeout();
        isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL) ? 1 : 0;

        if (!powerManager.isInteractive()) {
            interactionTime = DisplayMonitor.loadTime(Constants.SCREEN_ON_TIME_PREF);
            DisplayMonitor.saveTime(0l, Constants.SCREEN_ON_START_TIME_PREF);
        } else {
            start = DisplayMonitor.loadTime(Constants.SCREEN_ON_START_TIME_PREF);
            screenOnTime = DisplayMonitor.loadTime(Constants.SCREEN_ON_TIME_PREF);
            interactionTime = (System.currentTimeMillis() - start) + screenOnTime;
            DisplayMonitor.saveTime(System.currentTimeMillis(), Constants.SCREEN_ON_START_TIME_PREF);
        }

        DisplayMonitor.clearTime();
        //NetworkMonitor.clearTraffic();

        systemContext = new SystemContext(day, period, isCharging, brightness, batteryLevel,
                timeOut, trafficStats[0], trafficStats[1], interactionTime, cpuLoad);

        database.logStatus(systemContext);
        //database.clearAllLogs();

        restartTimer();
    }

    private void restartTimer() {
        stopService(new Intent(this, ScreenOnService.class));
        startService(new Intent(this, ScreenOnService.class));
    }

}

