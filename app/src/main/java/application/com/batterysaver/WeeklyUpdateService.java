package application.com.batterysaver;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;

/**
 * The WeeklyUpdateService is responsible the creation, copying of data  to the backup table
 * and the resetting on the current table for a new weeks data.  This happens on a weekly basis
 */
public class WeeklyUpdateService extends Service {
    PowerManager pm;
    PowerManager.WakeLock mWakeLock;
    private Context context = MyApplication.getAppContext();
    private DatabaseLogger databaseLogger = new DatabaseLogger(context);

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "lock");

        mWakeLock.acquire();

        databaseLogger.copyAndCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mWakeLock.release();
    }
}
