package application.com.batterysaver;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class ScreenOnService extends Service {

    BroadcastReceiver screenOnTimer;
    Context context = GlobalVars.getAppContext();

    public void onCreate() {
        super.onCreate();
        screenOnTimer = DisplayContext.InteractionTimer.setupTimer();
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

}
