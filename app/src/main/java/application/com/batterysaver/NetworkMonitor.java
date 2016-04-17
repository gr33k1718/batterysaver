package application.com.batterysaver;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class NetworkMonitor {
    private static final String RX_FILE = "/sys/class/net/wlan0/statistics/rx_bytes";
    private static final String TX_FILE = "/sys/class/net/wlan0/statistics/tx_bytes";
    private static Context context = MyApplication.getAppContext();
    private static PreferencesUtil pref = PreferencesUtil.getInstance(context,
            Constants.NETWORK_PREFS, Context.MODE_PRIVATE);
    private Thread networkMonitor = new Thread(new NetworkRunnable());
    private long networkLimit;

    public NetworkMonitor(long networkLimit) {
        this.networkLimit = networkLimit;
    }

    public static long[] getTrafficStats() {

        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        boolean is3g = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                .isConnectedOrConnecting();

        boolean isWifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                .isConnectedOrConnecting();

        long[] stats = new long[2];
        long prevNetworkStats = pref.getLong(Constants.NETWORK_TRAFFIC_PREF, 0);
        long prevMobileStats = pref.getLong(Constants.MOBILE_TRAFFIC_PREF, 0);
        long prevTotalBytes = pref.getLong("TOTAL_TRAFFIC", 0);

        long currentNetworkStats = getWifiTraffic(TX_FILE) + getWifiTraffic(RX_FILE);
        long currentMobileStats = TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes();

        long currentTotalBytes = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes();

        pref.putLong("TOTAL_TRAFFIC", currentTotalBytes);

        if (currentMobileStats == 0) {
            currentMobileStats = prevMobileStats;
        }

        if (currentNetworkStats == 0) {
            currentNetworkStats = prevNetworkStats;
        }

        long wifiTraffic = currentNetworkStats - prevNetworkStats;
        long mobileTraffic = currentMobileStats - prevMobileStats;
        long totalBytes = currentTotalBytes - prevTotalBytes;


        if (is3g) {
            long mobile = totalBytes - mobileTraffic;
            stats[0] = mobile > 0 ? mobile : 0;
            stats[1] = mobileTraffic;
            pref.putLong(Constants.MOBILE_TRAFFIC_PREF, currentMobileStats);
        } else if (isWifi) {
            long wifi = totalBytes - wifiTraffic;
            stats[0] = wifiTraffic;
            stats[1] = wifi > 0 ? wifi : 0;
            pref.putLong(Constants.NETWORK_TRAFFIC_PREF, currentNetworkStats);
        } else {
            stats[0] = 0;
            stats[1] = 0;
        }
        pref.commit();

        return stats;
    }

    public static void clearTraffic() {
        pref.remove(Constants.MOBILE_TRAFFIC_PREF);
        pref.remove(Constants.NETWORK_TRAFFIC_PREF);
        pref.commit();
    }

    private static long getWifiTraffic(String fileName) {
        File file = new File(fileName);
        BufferedReader br = null;
        long bytes = 0;
        try {
            br = new BufferedReader(new FileReader(file));
            String line = "";
            line = br.readLine();
            bytes = Long.parseLong(line);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;

        } finally {
            if (br != null)
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return bytes;
    }

    public static int[] getUsageTimes() {
        int[] usageTimes = new int[2];
        usageTimes[0] = pref.getInt("WiFiTotalTimes", 0);
        usageTimes[1] = pref.getInt("MobileTotalTime", 0);
        return usageTimes;
    }

    public static void monitorUsageTimes() {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int mobileDataActivity = telephonyManager.getDataActivity();
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        long currentWiFiBytes = getWifiTraffic(TX_FILE) + getWifiTraffic(RX_FILE);

        long prevTotalBytes = pref.getLong("WiFiTotalBytes", currentWiFiBytes);

        pref.putLong("WiFiTotalBytes", currentWiFiBytes);

        long totalBytes = currentWiFiBytes - prevTotalBytes;
        if (totalBytes > 500000 & powerManager.isInteractive()) {
            int prevWifiTime = pref.getInt("WiFiTotalTimes", 0);
            pref.putInt("WiFiTotalTimes", prevWifiTime + 5);
            Log.e("[Error]", "WiFi time " + prevWifiTime);
        }

        if (mobileDataActivity != 0 && mobileDataActivity != 4 & powerManager.isInteractive()) {
            int prevMobileTime = pref.getInt("MobileTotalTime", 0);
            pref.putInt("MobileTotalTime", prevMobileTime + 5);
            Log.e("[Error]", "Mobile time " + prevMobileTime);
        }

        //Log.e("[Error]", "Total Bytes " + (currentWiFiBytes - prevTotalBytes));

        pref.commit();
    }

    public static void clearUsageTimes() {
        pref.putInt("WiFiTotalTimes", 0);
        pref.putInt("MobileTotalTime", 0);
    }

    public void activateNetworkMonitor() {
        if (!isAlive()) {
            startNetworkMonitor();
        } else {
            stopNetworkMonitor();
            startNetworkMonitor();
        }
    }

    public void startNetworkMonitor() {
        networkMonitor.start();
    }

    public void stopNetworkMonitor() {
        networkMonitor.interrupt();
    }

    public boolean isAlive() {
        return networkMonitor.isAlive();
    }

    private void networkNotify(String title, String text) {

        final Notification.Builder mBuilder = new Notification.Builder(context);
        mBuilder.setStyle(new Notification.BigTextStyle(mBuilder)
                .bigText("High network traffic detected. Please review your current usage to conserve battery")
                .setBigContentTitle("Network traffic limit reached")
                .setSummaryText("Big summary"))
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle("High CPU detected")
                .setContentText("Please limit network use")
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        Intent resultIntent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);

        stackBuilder.addParentStack(WeeklyStatsActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_CANCEL_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());
    }

    public void activateSyncSetting(int delayMin) {

        int delayMilli = delayMin * 60 * 1000;

        Intent logIntent = new Intent(context, SyncService.class);

        PendingIntent p = PendingIntent.getService(context, 1, logIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), delayMilli, p);
    }

    public static class SyncService extends Service {
        private PowerManager pm;
        private PowerManager.WakeLock mWakeLock;

        @Override
        public void onCreate() {
            super.onCreate();
            pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "lock");
            mWakeLock.acquire();

            boolean masterSyncAutomatically = ContentResolver.getMasterSyncAutomatically();
            ContentResolver.setMasterSyncAutomatically(!masterSyncAutomatically);
            Log.e("[Error]", "" + masterSyncAutomatically);
            stopSelf();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mWakeLock.release();
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }

    private class NetworkRunnable implements Runnable {
        private final long SLEEP_TIME = 5000;
        private long currentTraffic;
        private long currentTotalBytes;

        @Override
        public void run() {
            long prevTotalBytes = pref.getLong("TOTAL_TRAFFIC", 0);

            while (!Thread.interrupted()) {
                currentTotalBytes = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes();
                currentTraffic = (currentTotalBytes - prevTotalBytes);

                try {
                    if (currentTraffic > networkLimit) {
                        networkNotify("", "");
                        stopNetworkMonitor();
                    }
                } catch (Exception e) {
                    Log.e("[Error]", e.toString());
                }
                try {
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e) {
                    Log.e("[Error]", e.toString());
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
