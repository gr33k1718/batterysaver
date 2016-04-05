package application.com.batterysaver;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

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

        //gatherData();

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
    public void gatherData() {
        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        DisplayMonitor displayMonitor = new DisplayMonitor();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, -1);
        period = cal.get(Calendar.HOUR_OF_DAY);
        day = cal.get(Calendar.DAY_OF_WEEK);

        int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        int batteryLevel = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL) ? 1 : 0;

        long[] trafficStats = NetworkMonitor.getTrafficStats();

        int cpuLoad = cpuMonitor.getTotalCpuLoad();

        int brightness = displayMonitor.screenBrightness();
        int timeOut = displayMonitor.screenTimeout();
        long interactionTime =  displayMonitor.getInteractionTime();


        SystemContext systemContext  = new SystemContext(day, period, isCharging, brightness, batteryLevel,
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

    private double movingAvg(int count, int prev, int cur){
        return ((prev * count) + cur)/(double)(count +1);
    }

    private void logData(){
        int count = prefs.getInt("Count",1);

        int prevCpuLoad = prefs.getInt(Constants.CPU_LOAD_PREF_INT, 0);

        int curCpuLoad = CpuMonitor.readCpuUsage(5);

        double avgLoad = movingAvg(count,prevCpuLoad,curCpuLoad);

        Log.e("[Error]", "Count " + count + " Prev " + prevCpuLoad + " Current " + curCpuLoad + " Result " + avgLoad);

        //Log every 60 min: 5 min alarm * 12
        if(count % 12 == 0){
            gatherData();

            prefs.putInt(Constants.CPU_LOAD_PREF_INT, 0);
            prefs.putInt("Count",1);
        }
        else{
            prefs.putInt(Constants.CPU_LOAD_PREF_INT, (int)avgLoad);
            prefs.putInt("Count", count + 1 );

        }

        prefs.commit();
    }

}

