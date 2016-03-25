package application.com.batterysaver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.Calendar;

public class BootReceiver extends BroadcastReceiver {
    private Context mContext = MyApplication.getAppContext();
    private BroadcastReceiver broadcastReceiver;
    private CpuMonitor cpuMonitor = new CpuMonitor();

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
                Log.e("[Error]", "Screen on thread ");

                broadcastReceiver = new ScreenOnReceiver();

                mContext.registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
                mContext.registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        }
    };

    private Thread receiverThread = new Thread(runnable);

    public void onReceive(Context context, Intent intent) {

        scheduleAlarms();

        startReceiver();

        cpuMonitor.startCpuThread();
    }

    public void scheduleAlarms() {
        hourlyLogAlarm();

        //weeklyProfileAlarm();
    }

    private void restartReceiver(){
        mContext.unregisterReceiver(broadcastReceiver);

        receiverThread.interrupt();
    }

    public void startReceiver(){
        if(receiverThread.isAlive()){

            restartReceiver();
            receiverThread.start();
        }
        else{
            receiverThread.start();
        }
    }

    private void hourlyLogAlarm(){
        String actionName = "logService";

        Calendar time = Calendar.getInstance();

        time.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY) + 1);
        time.set(Calendar.MINUTE, 0);

        Intent logIntent = new Intent(mContext, WakefulReceiver.class);
        logIntent.setAction(actionName);

        PendingIntent p = PendingIntent.getBroadcast(mContext, 1, logIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), AlarmManager.INTERVAL_HOUR, p);
    }

    private void weeklyProfileAlarm(){
        String actionName = "weeklyService";

        Calendar time = Calendar.getInstance();

        time.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY) + 1);
        time.set(Calendar.MINUTE, 5);

        Intent logIntent = new Intent(mContext, WakefulReceiver.class);
        logIntent.setAction(actionName);

        PendingIntent p = PendingIntent.getBroadcast(mContext, 1, logIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), AlarmManager.INTERVAL_DAY * 7, p);
    }
}
