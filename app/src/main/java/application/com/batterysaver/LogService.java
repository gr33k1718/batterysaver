package application.com.batterysaver;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;

import java.util.Calendar;

public class LogService extends Service {
    ActivityManager am;
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
        am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);

        hourlyLog();

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
     *  Gathers the context information related to the user and stores within the database.
     */
    public void gatherData() {
        DatabaseLogger database = new DatabaseLogger(this);
        CpuMonitor cpuMonitor = new CpuMonitor();
        DisplayMonitor displayMonitor = new DisplayMonitor();
        SystemMonitor systemMonitor = new SystemMonitor();

        int day = systemMonitor.getDay();
        int period = systemMonitor.getPeriod();

        int batteryLevel = systemMonitor.getBatteryLevel();
        int isCharging = systemMonitor.isCharging();

        int brightness = displayMonitor.getScreenBrightness();
        int timeOut = displayMonitor.getScreenTimeout();
        long interactionTime = displayMonitor.getInteractionTime();

        long[] trafficStats = NetworkMonitor.getTrafficStats();
        int[] usageTimes = NetworkMonitor.getUsageTimes();
        long wifiTraffic = trafficStats[0];
        long mobileTraffic = trafficStats[1];
        int wifiUsage = usageTimes[0];
        int mobileUsage = usageTimes[1];

        int cpuLoad = cpuMonitor.getTotalCpuLoad();

        double usageScore = Predictor.predictBatteryUsage(wifiTraffic, mobileTraffic, cpuLoad,
                interactionTime, brightness,
                wifiUsage, mobileUsage);

        String usageType = usageType(usageScore);

        LogData systemContext = new LogData(day, period, isCharging, brightness,
                batteryLevel, timeOut, wifiTraffic, mobileTraffic,
                interactionTime, cpuLoad, wifiUsage, mobileUsage, usageType);

        database.logStatus(systemContext);

        displayMonitor.restartScreenService();
    }

    /*
     *  Determines the usage profile for the given time of the day. Calculates the the settings
     *  to apply and activates the settings suggested.
     *  Setting are applied based on whether the current time
     */

    private void applySettings() {
        int endTime;
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_WEEK);
        int period = cal.get(Calendar.HOUR_OF_DAY);
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
        long networkWarningLimit = savingsProfile.getNetworkWarningLimit();
        int brightness = savingsProfile.getBrightness();
        long timeout = savingsProfile.getTimeout();
        int syncTime = savingsProfile.getSyncTime();
        NetworkMonitor networkMonitor = new NetworkMonitor(networkWarningLimit);
        CpuMonitor cpuMonitor = new CpuMonitor();
        DisplayMonitor displayMonitor = new DisplayMonitor();

        networkMonitor.activateSyncSetting(syncTime);

        displayMonitor.setScreenBrightness(brightness);
        displayMonitor.setScreenTimeout(timeout);

        networkMonitor.activateNetworkMonitor();

        if (savingsProfile.isSetCpuMonitor()) {
            cpuMonitor.activateNetworkMonitor();
        } else {
            cpuMonitor.stopCpuMonitor();
        }
    }


    private void hourlyLog() {
        int count = prefs.getInt("Count", 1);

        //Log every 60 min: 5 min alarm * 12
        if (count % 12 == 0) {
            gatherData();

            NetworkMonitor.clearUsageTimes();

            prefs.putInt("Count", 1);
        } else {
            CpuMonitor.monitorCpuUsage(count);
            NetworkMonitor.monitorUsageTimes();

            prefs.putInt("Count", count + 1);
        }
        prefs.commit();
    }

    private String usageType(double usageScore) {

        if (usageScore <= 1) {
            return "minimal";
        } else if (usageScore <= 3) {
            return "low";
        } else if (usageScore <= 6) {
            return "medium";
        } else {
            return "high";
        }
    }
}

