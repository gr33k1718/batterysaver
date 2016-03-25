package application.com.batterysaver;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class WeeklyUpdateService extends IntentService {
    private Context context = MyApplication.getAppContext();
    private DatabaseLogger databaseLogger = new DatabaseLogger(context);

    public WeeklyUpdateService(){
        super("Weekly update");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        databaseLogger.copyTable();

        databaseLogger.clearAllLogs(Constants.LOG_TABLE_NAME_ONE);

        //databaseLogger.getUsagePatterns();

        WakefulBroadcastReceiver.completeWakefulIntent(intent);
    }
}
