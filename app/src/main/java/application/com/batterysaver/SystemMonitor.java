package application.com.batterysaver;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import java.util.Calendar;

public class SystemMonitor {
    private Context context = MyApplication.getAppContext();
    private Calendar cal;
    private Intent batteryIntent = context.registerReceiver(null,
            new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

    public SystemMonitor() {
        cal = Calendar.getInstance();
    }

    public int getDay() {
        return cal.get(Calendar.DAY_OF_WEEK);
    }

    public int getPeriod() {
        return cal.get(Calendar.HOUR_OF_DAY);
    }

    public int getBatteryLevel() {
        return batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
    }

    public int isCharging() {
        int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        return (status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL) ? 1 : 0;
    }
}
