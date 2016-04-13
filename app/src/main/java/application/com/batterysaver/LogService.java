package application.com.batterysaver;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.util.Calendar;

public class LogService extends Service {
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

        //gatherData();

        ActivityManager am = (ActivityManager)this.getSystemService(ACTIVITY_SERVICE);
        // The first in the list of RunningTasks is always the foreground task.
        ActivityManager.RunningTaskInfo foregroundTaskInfo = am.getRunningTasks(1).get(0);
        String foregroundTaskPackageName = foregroundTaskInfo .topActivity.getPackageName();
        PackageManager pm = this.getPackageManager();
        PackageInfo foregroundAppPackageInfo = null;
        try {
            foregroundAppPackageInfo = pm.getPackageInfo(foregroundTaskPackageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String foregroundTaskAppName = foregroundAppPackageInfo.applicationInfo.loadLabel(pm).toString();

        Log.e("[Error]", foregroundTaskAppName);

        logData();

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
        long interactionTime =  displayMonitor.getInteractionTime();

        long[] trafficStats = NetworkMonitor.getTrafficStats();
        long wifiTraffic = trafficStats[0];
        long mobileTraffic = trafficStats[1];

        int cpuLoad = cpuMonitor.getTotalCpuLoad();


        double usageScore = Predictor.milliAmpPerHour(wifiTraffic, mobileTraffic, cpuLoad,
                interactionTime, brightness);

        int predictedConsumption = (int)((usageScore/2550.0)*100);

        //Log.e("[Error]", "Predicted consumption " + predictedConsumption + " mAh " + Predictor.milliAmpPerHour(wifiTraffic, mobileTraffic, cpuLoad,
                //interactionTime, brightness));


        String usageType = usageType(predictedConsumption);

        LogData systemContext  = new LogData(day, period, isCharging, brightness,
                batteryLevel, predictedConsumption,timeOut, wifiTraffic, mobileTraffic,
                interactionTime, cpuLoad, usageScore, usageType);

        //database.clearAllLogs(Constants.LOG_TABLE_NAME_ONE);

        //database.alter();
        database.logStatus(systemContext);

        displayMonitor.restartScreenService();
        //database.copyTable();
    }

    /*
     *  Determines the usage profile for the given time of the day. Calculates the the settings
     *  to apply and activates the settings suggested.
     *  Setting are applied based on whether the current time
     */
    /*
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
*/

    private void logData(){
        int count = prefs.getInt("Count", 1);

        int prevCpuLoad = prefs.getInt(Constants.CPU_LOAD_PREF_INT, 0);

        int curCpuLoad = Integer.parseInt(CpuMonitor.readCpuUsage(5).get(0));

        double avgLoad = Math.round(movingAvg(count, prevCpuLoad, curCpuLoad));

        int load = (int)avgLoad;

        double a = scale(avgLoad, 50, 10);

        NetworkMonitor.getUsageTimes();
        Log.e("[Error]", "Current load " + curCpuLoad + " Average load " + avgLoad + " Int average load " + load);

        //Log every 60 min: 5 min alarm * 12
        if(count % 12 == 0){
            gatherData();
            NetworkMonitor.activateSyncSetting(5);
            NetworkMonitor.clearUsageTimes();
            prefs.putInt(Constants.CPU_LOAD_PREF_INT, curCpuLoad);
            prefs.putInt("Count", 1);
        }

        else{
            prefs.putInt(Constants.CPU_LOAD_PREF_INT, load);
            prefs.putInt("Count", count + 1);

        }

        prefs.commit();
    }

    private double movingAvg(int count, int prev, int cur){
        return ((prev * count) + cur)/(double)(count +1);
    }

    private String usageType(double usageScore){

        if(usageScore <= 1){
            return "minimal";
        }
        else if (usageScore <= 3){
            return "low";
        }
        else if(usageScore <= 6){
            return "medium";
        }
        else{
            return "high";
        }
    }

    private double scale(double valueIn, double baseMax, double limitMax) {

        if(valueIn > baseMax){
            return limitMax;
        }

        if(valueIn < 0){
            return 0;
        }

        return (valueIn/ baseMax) * limitMax;
    }

    private double usageScore(double interactionTime, double cpuLoad, double networkTraffic){
        final double maxLimit = 10;
        final double maxInteraction = 1800000;
        final double maxTraffic = 20000000;
        final double maxCpu = 50;
        final double maxScore = 30;

        double interaction = scale(interactionTime,  maxInteraction, maxLimit);
        double cpu = scale(cpuLoad, maxCpu,  maxLimit);
        double traffic = scale(networkTraffic,  maxTraffic, maxLimit);

        double result = interaction + cpu + traffic;

        return scale(result, maxScore, maxLimit);
    }

}

