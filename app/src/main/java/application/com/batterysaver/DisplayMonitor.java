package application.com.batterysaver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.widget.Toast;

import java.util.Calendar;

public class DisplayMonitor {
    private static Context context = GlobalVars.getAppContext();

    public static int screenBrightness() {
        return Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 0);
    }

    public static int screenTimeout() {
        return Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 0);
    }

    public static void saveTime(Long time, String type) {
        InteractionTimer.saveTime(time, type);
    }

    public static long loadTime(String type) {
        return InteractionTimer.loadTime(type);
    }

    public static void clearTime() {
        InteractionTimer.clearTime();
    }

    public static class InteractionTimer {
        private static PreferencesUtil pref = PreferencesUtil.getInstance(context, Constants.SCREEN_TIME_PREFS, Context.MODE_PRIVATE);

        private static WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        private static long screenOnStartTime = 0;
        private static long screenOnEndTime = 0;
        private static long screenOnTime = 0;

        public static BroadcastReceiver setupTimer(/*final boolean isIdle*/) {

            BroadcastReceiver screenOnTimerReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {

                    if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                        screenOnStartTime = System.currentTimeMillis();
                        pref.putLong(Constants.SCREEN_ON_START_TIME_PREF, screenOnStartTime);

                        pref.commit();
                        Toast.makeText(context, "Total time " + convertMillisecondsToHMmSs(loadTime(Constants.SCREEN_ON_TIME_PREF)), Toast.LENGTH_SHORT).show();
                       /* if (isIdle) {
                            wifiManager.setWifiEnabled(true);
                        }*/
                    } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                        long startTime = pref.getLong(Constants.SCREEN_ON_START_TIME_PREF, 0);
                        long prevTime = pref.getLong(Constants.SCREEN_ON_TIME_PREF, 0);

                        screenOnEndTime = System.currentTimeMillis();
                        screenOnTime = screenOnEndTime - startTime;

                        pref.putLong(Constants.SCREEN_ON_TIME_PREF, prevTime + screenOnTime);

                        pref.commit();
                       /* if(isIdle){
                            wifiManager.setWifiEnabled(false);
                        }
                        else{
                            wifiManager.setWifiEnabled(true);
                        }*/
                    }
                }
            };
            context.registerReceiver(screenOnTimerReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
            context.registerReceiver(screenOnTimerReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));

            return screenOnTimerReceiver;
        }

        private static void saveTime(Long time, String type) {
            if (type.equals(Constants.SCREEN_ON_TIME_PREF)) {
                pref.putLong(Constants.SCREEN_ON_TIME_PREF, time);
            } else {
                pref.putLong(Constants.SCREEN_ON_START_TIME_PREF, time);
            }
            pref.commit();
        }

        private static long loadTime(String type) {
            if (type.equals(Constants.SCREEN_ON_TIME_PREF)) {
                return pref.getLong(Constants.SCREEN_ON_TIME_PREF, 0);
            } else {
                return pref.getLong(Constants.SCREEN_ON_START_TIME_PREF, 0);
            }
        }

        private static void clearTime() {
            pref.remove(Constants.SCREEN_ON_TIME_PREF);
            pref.commit();
        }

        private static String convertMillisecondsToHMmSs(long milliseconds) {
            int s = (int) (milliseconds / 1000) % 60;
            int m = (int) ((milliseconds / (1000 * 60)) % 60);
            int h = (int) ((milliseconds / (1000 * 60 * 60)) % 24);
            return String.format("%02d:%02d:%02d", h, m, s);
        }
    }
}
