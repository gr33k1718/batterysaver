package application.com.batterysaver;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;

import java.util.Calendar;

/**
 * The LogService is responsible for collection of user data at specific intervals. Also
 * handles the activation of the setting provided by the power savings profiles
 */
public class LogService extends Service {
    ActivityManager am;
    private PreferencesUtil prefs = PreferencesUtil.getInstance(this,
            Constants.SYSTEM_CONTEXT_PREFS, Context.MODE_PRIVATE);
    private PowerManager pm;
    private PowerManager.WakeLock mWakeLock;


    @Override
    public void onCreate() {
        super.onCreate();

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

    /**
     * Gets all the data from the various sub components of the system and stores them into
     * the database. Activates the setting provided from the savings profiles and restarts
     * the screen interaction time service.
     */
    public void gatherData() {
        DatabaseLogger database = new DatabaseLogger(this);
        CpuMonitor cpuMonitor = new CpuMonitor();
        DisplayMonitor displayMonitor = new DisplayMonitor();
        SystemMonitor systemMonitor = new SystemMonitor();

        applySettings();

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

        LogData systemContext = new LogData(day, period, isCharging, brightness,
                batteryLevel, timeOut, wifiTraffic, mobileTraffic,
                interactionTime, cpuLoad, wifiUsage, mobileUsage);

        database.logStatus(systemContext);

        displayMonitor.restartScreenService();
    }

    /**
     * For the day and time given activates the setting provided by the savings profile.
     * Will just exit if no profile exists.
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

        if(savingsProfile == null){
            return;
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

    /**
     * Reads and stores the users CPU load and data network usage times over 5 min periods.
     * Logs the total usage every 1 hour
     */
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

}

