package application.com.batterysaver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ScreenOnReceiver extends BroadcastReceiver {

    private Context context = MyApplication.getAppContext();
    private PreferencesUtil pref = PreferencesUtil.getInstance(context, Constants.SCREEN_TIME_PREFS, Context.MODE_PRIVATE);

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            long screenOnStartTime = System.currentTimeMillis();

            pref.putLong(Constants.SCREEN_ON_START_TIME_PREF, screenOnStartTime);
            Log.e("[Error]", "" + screenOnStartTime);
            pref.commit();

        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            long startTime = pref.getLong(Constants.SCREEN_ON_START_TIME_PREF, 0);
            long prevTime = pref.getLong(Constants.SCREEN_ON_TIME_PREF, 0);

            long screenOnEndTime = System.currentTimeMillis();
            long screenOnTime = screenOnEndTime - startTime;

            pref.putLong(Constants.SCREEN_ON_TIME_PREF, prevTime + screenOnTime);

            pref.commit();
        }
    }
}
