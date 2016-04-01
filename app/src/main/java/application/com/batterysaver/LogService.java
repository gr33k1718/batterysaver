package application.com.batterysaver;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;

import java.util.Calendar;

public class LogService extends Service {
    private DatabaseLogger database;
    private Calendar cal = Calendar.getInstance();
    private CpuMonitor cpuMonitor = new CpuMonitor();
    private int day;
    private int period;
    private PreferencesUtil prefs = PreferencesUtil.getInstance(this,
            Constants.SYSTEM_CONTEXT_PREFS, Context.MODE_PRIVATE);
    private PowerManager pm;
    private PowerManager.WakeLock mWakeLock;


    @Override
    public void onCreate() {
        super.onCreate();

        //Ensures the CPU is kept awake while the screen is off
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "lock");
        mWakeLock.acquire();

        day = cal.get(Calendar.DAY_OF_WEEK) - 1;
        period = cal.get(Calendar.HOUR_OF_DAY);
        database = new DatabaseLogger(this);

        logContext();

        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mWakeLock.release();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*
     *  Restarts the service that calculates interactions time and cpu load.
     *  This is necessary as Android can force-stop background applications and services
     */
    private void restartService() {
        stopService(new Intent(this, LongRunningService.class));
        startService(new Intent(this, LongRunningService.class));
    }

    /*
     *  Gathers the context information related to the user and stores within the database.
     */
    public void logContext() {
        DisplayMonitor displayMonitor = new DisplayMonitor();
        int isCharging, period, day, brightness, timeOut, status, batteryLevel;
        long interactionTime;
        long[] trafficStats;
        int cpuLoad;
        SystemContext systemContext;
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, -1);

        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

        period = cal.get(Calendar.HOUR_OF_DAY);
        day = cal.get(Calendar.DAY_OF_WEEK);

        trafficStats = NetworkMonitor.getTrafficStats();
        cpuLoad = cpuMonitor.getTotalCpuLoad();
        brightness = displayMonitor.screenBrightness();
        timeOut = displayMonitor.screenTimeout();
        isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL) ? 1 : 0;
        interactionTime = displayMonitor.getInteractionTime();
        batteryLevel = Predictor.milliAmpPerHpur(trafficStats[0], trafficStats[1], cpuLoad, interactionTime, brightness);

        systemContext = new SystemContext(day, period, isCharging, brightness, batteryLevel,
                timeOut, trafficStats[0], trafficStats[1], interactionTime, cpuLoad);

        database.logStatus(systemContext);

        restartService();
        //database.copyTable();


    }

    /*
     *  Determines the usage profile for the given time of the day. Calculates the the settings
     *  to apply and activates the settings suggested.
     *  Setting are applied based on whether the current time
     */
    private void applySettings() {
        int endTime;
        SavingsProfile savingsProfile = null;

        UsageProfile[] usageProfiles = prefs.getUsageProfiles()[day];

        for (UsageProfile usageProfile : usageProfiles) {
            if (usageProfile != null) {
                if (usageProfile.getEnd() == 0) {
                    endTime = 24;
                } else {
                    endTime = usageProfile.getEnd();
                }

                if (period >= usageProfile.getStart() && period <= endTime) {
                    savingsProfile = new SavingsProfile(usageProfile).generate();
                    break;
                }
            }
        }

        NetworkMonitor networkMonitor = new NetworkMonitor(savingsProfile.getNetworkWarningLimit());

        Settings.System.putInt(this.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS, savingsProfile.getBrightness());
        Settings.System.putLong(this.getContentResolver(),
                Settings.System.SCREEN_OFF_TIMEOUT, savingsProfile.getTimeout());

        if (savingsProfile.isSetCpuMonitor()) {
            if (!cpuMonitor.isAliveCpuMonitor()) {
                cpuMonitor.startCpuMonitor();
            }
        } else {
            cpuMonitor.stopCpuMonitor();
        }

        if (savingsProfile.isSetNetworkMonitor()) {
            if (!networkMonitor.isAlive()) {
                networkMonitor.startNetworkMonitor();
            }
        } else {
            networkMonitor.stopNetworkMonitor();
        }
    }
}

