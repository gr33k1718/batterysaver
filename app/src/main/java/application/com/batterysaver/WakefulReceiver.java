package application.com.batterysaver;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class WakefulReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if(action.equals("logService")){
            Intent logService = new Intent(context, LogService.class);
            //Log.e("[Error]", "Log Service");
            startWakefulService(context, logService);
        }
        if(action.equals("weeklyService")){
            Intent weeklyUpdate = new Intent(context, WeeklyUpdateService.class);
            //Log.e("[Error]", "WeeklyService");
            startWakefulService(context, weeklyUpdate);
        }


    }
}
