package application.com.batterysaver;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.provider.Settings;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import java.util.Calendar;

public class LogService extends IntentService {
    private DatabaseLogger database;
    private Calendar cal = Calendar.getInstance();
    private CpuMonitor cpuMonitor = new CpuMonitor();
    private int day;
    private int period;
    private BootReceiver bootReceiver = new BootReceiver();
    private PreferencesUtil prefs = PreferencesUtil.getInstance(this,
            Constants.SYSTEM_CONTEXT_PREFS, Context.MODE_PRIVATE);

    public LogService(){
        super("LogService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        day = cal.get(Calendar.DAY_OF_WEEK) - 1;
        period = cal.get(Calendar.HOUR_OF_DAY);
        database = new DatabaseLogger(this);

       // bootReceiver.startReceiver();

        //cpuMonitor.startCpuThread();

        logContext();

        WakefulBroadcastReceiver.completeWakefulIntent(intent);

    }


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
        batteryLevel = Predictor.predictBatteryUsage(trafficStats[0], trafficStats[1], cpuLoad, interactionTime, brightness);
/*
        Log.e("[Error]", "Interaction time " + interactionTime +
                "\nPredicted battery " + Predictor.predictInteractionBattery(interactionTime, brightness) +
                "\nTraffic " + trafficStats[0] + " " + trafficStats[1] +
                "\nPredicted battery " + Predictor.predictNetworkBattery(trafficStats[0],trafficStats[1]) +
                "\nCPU " + cpuLoad +
                "\nPredicted battery " + Predictor.predictCpuBattery(cpuLoad) +
                "\nPredicted overall battery " + batteryLevel );
*/
        systemContext = new SystemContext(day, period, isCharging, brightness, batteryLevel,
                timeOut, trafficStats[0], trafficStats[1], interactionTime, cpuLoad);

        database.logStatus(systemContext);
        //database.clearAllLogs();

    }

    private void applySettings() {
        int endTime;
        SavingsProfile s = null;

        UsageProfile[] u = prefs.getUsageProfiles()[day];

        for (UsageProfile usageProfile : u) {
            if (usageProfile != null) {
                if (usageProfile.getEnd() == 0) {
                    endTime = 24;
                } else {
                    endTime = usageProfile.getEnd();
                }

                if (period >= usageProfile.getStart() && period <= endTime) {
                    s = new SavingsProfile(usageProfile).generate();
                    break;
                }
            }
        }

        NetworkMonitor networkMonitor = new NetworkMonitor(s.getNetworkWarningLimit());

        Settings.System.putInt(this.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS, s.getBrightness());
        Settings.System.putLong(this.getContentResolver(),
                Settings.System.SCREEN_OFF_TIMEOUT, s.getTimeout());

        if (s.isSetCpuMonitor()) {
            if (!cpuMonitor.isAliveCpuMonitor()) {
                cpuMonitor.startCpuMonitor();
            }
        } else {
            cpuMonitor.stopCpuMonitor();
        }

        if (s.isSetNetworkMonitor()) {
            if (!networkMonitor.isAlive()) {
                networkMonitor.startNetworkMonitor();
            }
        } else {
            networkMonitor.stopNetworkMonitor();
        }
    }
}

