package application.com.batterysaver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;


public class ScreenOnService extends Service {

    BroadcastReceiver screenOnTimer;
    Context context = GlobalVars.getAppContext();
    TelephonyManager        Tel;
    MyPhoneStateListener    MyListener;



    public void onCreate() {
        super.onCreate();
        screenOnTimer = DisplayContext.InteractionTimer.setupTimer();
        MyListener   = new MyPhoneStateListener();
        Tel       = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        Tel.listen(MyListener ,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

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

    private class MyPhoneStateListener extends PhoneStateListener
    {
        boolean wait = false;
        /* Get the Signal strength from the provider, each tiome there is an update */

        public void onSignalStrengthsChanged(SignalStrength signalStrength)
        {
            Calendar cal = Calendar.getInstance();
            super.onSignalStrengthsChanged(signalStrength);

            Log.d("[shit]", cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND) + " " +signalStrength.getGsmSignalStrength());
        }

    }

    private void Notify(){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setContentTitle("My notification")
                        .setContentText("Hello World!");
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        Intent resultIntent = new Intent(this, MainActivity.class);
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
