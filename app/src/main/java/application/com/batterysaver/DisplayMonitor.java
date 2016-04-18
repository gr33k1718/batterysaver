package application.com.batterysaver;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

/**
 * This class handles all display hardware functionality
 */
public class DisplayMonitor {
    private static Context context = MyApplication.getAppContext();
    private static PreferencesUtil pref = PreferencesUtil.getInstance(context,
            Constants.SCREEN_TIME_PREFS, Context.MODE_PRIVATE);
    private PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

    /**
     * Gets the current screen brightness
     *
     * @return the brightness value
     */
    public int getScreenBrightness() {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS, 0);
    }

    /**
     * Sets a brightness value
     *
     * @param brightness the brightness value
     */
    public void setScreenBrightness(int brightness) {
        Settings.System.putInt(context.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS, brightness);
    }

    /**
     * Gets the current screen timeout value
     *
     * @return the timeout value
     */
    public int getScreenTimeout() {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.SCREEN_OFF_TIMEOUT, 0);
    }

    /**
     * Sets the screen timeout value
     *
     * @param timeout the timeout value
     */
    public void setScreenTimeout(long timeout) {
        Settings.System.putLong(context.getContentResolver(),
                Settings.System.SCREEN_OFF_TIMEOUT, timeout);
    }

    /**
     * Gets the current interaction time. If the screen is on when this data is requested
     * uses the current system time as an end point. Stores the dat within shared preferences
     *
     * @return the interaction time
     */
    public long getInteractionTime() {
        long interactionTime, screenOnTime, start;

        if (!powerManager.isInteractive()) {
            interactionTime = loadTime(Constants.SCREEN_ON_TIME_PREF);
            saveTime(0l, Constants.SCREEN_ON_START_TIME_PREF);
        } else {
            start = loadTime(Constants.SCREEN_ON_START_TIME_PREF);
            screenOnTime = loadTime(Constants.SCREEN_ON_TIME_PREF);
            interactionTime = (System.currentTimeMillis() - start) + screenOnTime;
            saveTime(System.currentTimeMillis(), Constants.SCREEN_ON_START_TIME_PREF);
        }
        clearTime();

        return interactionTime;

    }

    /**
     * Stores in shared preferences the given time
     *
     * @param time the time to save
     * @param type the type of time
     */
    private void saveTime(Long time, String type) {
        if (type.equals(Constants.SCREEN_ON_TIME_PREF)) {
            pref.putLong(Constants.SCREEN_ON_TIME_PREF, time);
        } else {
            pref.putLong(Constants.SCREEN_ON_START_TIME_PREF, time);
        }
        pref.commit();
    }

    /**
     * Fetches from shared preferences the requested time
     *
     * @param type the type of time requested
     * @return the time the requested
     */
    private long loadTime(String type) {
        if (type.equals(Constants.SCREEN_ON_TIME_PREF)) {
            return pref.getLong(Constants.SCREEN_ON_TIME_PREF, 0);
        } else {
            return pref.getLong(Constants.SCREEN_ON_START_TIME_PREF, 0);
        }
    }

    /**
     * Resets all values within shared preferences associated with screen time
     */
    private void clearTime() {
        pref.remove(Constants.SCREEN_ON_TIME_PREF);
        pref.commit();
    }

    /**
     * Handles the restarting on the interaction time service.  Was needed as the service is long
     * running and receiver had to be registered in code.
     */
    public void restartScreenService() {
        context.stopService(new Intent(context, ScreenService.class));
        context.startService(new Intent(context, ScreenService.class));
    }

    /**
     * The ScreenService is responsible for the capturing of the screen interaction time
     */
    public static class ScreenService extends Service {

        BroadcastReceiver screenReceiver = new BroadcastReceiver() {
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
        };

        @Override
        public void onCreate() {
            super.onCreate();
            context.registerReceiver(screenReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
            context.registerReceiver(screenReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            context.unregisterReceiver(screenReceiver);
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }
}

