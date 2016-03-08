package application.com.batterysaver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import java.util.Calendar;


public class ScreenOnService extends Service {
    boolean isIdle;
    int day;
    int period;
    Calendar cal;
    BroadcastReceiver screenOnTimer;
    Context context = GlobalVars.getAppContext();
    //TelephonyManager        Tel;
    //MyPhoneStateListener    MyListener;


    public void onCreate() {
        super.onCreate();
        PreferencesUtil prefs = PreferencesUtil.getInstance(context, Constants.SYSTEM_CONTEXT_PREFS, Context.MODE_PRIVATE);
        UsageProfile[][] profile = prefs.getUsageProfiles();
        cal = Calendar.getInstance();
        period = cal.get(Calendar.HOUR_OF_DAY);
        day = cal.get(Calendar.DAY_OF_WEEK)-2;
        //isIdle = profile[day][period].isIdle();



        //  MyListener   = new MyPhoneStateListener();
        //Tel       = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        //Tel.listen(MyListener ,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(isIdle){
            Notify();
        }
        screenOnTimer = DisplayContext.InteractionTimer.setupTimer();
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        context.unregisterReceiver(screenOnTimer);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
/*
    private class MyPhoneStateListener extends PhoneStateListener
    {
        boolean wait = false;


        public void onSignalStrengthsChanged(SignalStrength signalStrength)
        {
            Calendar cal = Calendar.getInstance();
            super.onSignalStrengthsChanged(signalStrength);

            Log.d("[shit]", cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND) + " " +signalStrength.getGsmSignalStrength());
        }

    }
*/
    private void Notify(){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setContentTitle("Idle period detected")
                        .setContentText("1: Wifi will be switched off when screen is off\n" +
                                        "2: Switch off mobile data");
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        Intent resultIntent = new Intent();
        resultIntent.setComponent(new ComponentName("com.android.settings","com.android.settings.Settings$DataUsageSummaryActivity"));
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(0, mBuilder.build());
    }
}
