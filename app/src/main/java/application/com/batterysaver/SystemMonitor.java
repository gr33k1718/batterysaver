package application.com.batterysaver;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import java.util.Calendar;

/**
 * The SystemMonitor class is responsible for getting system related battery information
 */
public class SystemMonitor {
    private Context context = MyApplication.getAppContext();
    private Calendar cal = Calendar.getInstance();
    private Intent batteryIntent = context.registerReceiver(null,
            new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

    /**
     * Gets the current day
     * @return the day of the week
     */
    public int getDay() {
        return cal.get(Calendar.DAY_OF_WEEK);
    }

    /**
     * get the hour of the day
     * @return the hour of the day
     */
    public int getPeriod() {
        return cal.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * Get the battery level percentage
     * @return the battery level percentage
     */
    public int getBatteryLevel() {
        return batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
    }

    /**
     * Check whether the phone is charging
     * @return the charging status
     */
    public int isCharging() {
        int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        return (status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL) ? 1 : 0;
    }
}
