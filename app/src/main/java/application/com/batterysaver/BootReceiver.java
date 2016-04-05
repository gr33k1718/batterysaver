package application.com.batterysaver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class BootReceiver extends BroadcastReceiver {
    private Context mContext = MyApplication.getAppContext();

    public void onReceive(Context context, Intent intent) {

        hourlyLogAlarm();

        //weeklyProfileAlarm();
    }

    /**
     * Creates a repeatable task that starts at the top of nearest hour. This starts a service that
     * stores the users usage into the database
     */
    private void hourlyLogAlarm() {
        String actionName = "logService";

        Calendar time = Calendar.getInstance();

        time.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY) + 1);
        time.set(Calendar.MINUTE, 0);

        Intent logIntent = new Intent(mContext, LogService.class);
        logIntent.setAction(actionName);

        PendingIntent p = PendingIntent.getService(mContext, 1, logIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 300000, p);
    }

    /**
     * Creates a repeatable task that starts 30 min after the logging service. Activated on a weekly
     * basis which starts a service that clears and copies current usage to another database table.
     */
    private void weeklyProfileAlarm() {
        String actionName = "weeklyService";

        Calendar time = Calendar.getInstance();

        time.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY) + 1);
        time.set(Calendar.MINUTE, 30);

        Intent logIntent = new Intent(mContext, WeeklyUpdateService.class);
        logIntent.setAction(actionName);

        PendingIntent p = PendingIntent.getService(mContext, 1, logIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, p);
    }
}
