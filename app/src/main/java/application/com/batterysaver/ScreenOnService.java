package application.com.batterysaver;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;


public class ScreenOnService extends Service {
    boolean isIdle;
    int day;
    int period;
    Calendar cal;
    BroadcastReceiver screenOnTimer;
    private int mInterval = 5000;
    Context context = GlobalVars.getAppContext();
    private Handler mHandler;


    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
        startRepeatingTask();
        PreferencesUtil prefs = PreferencesUtil.getInstance(context, Constants.SYSTEM_CONTEXT_PREFS, Context.MODE_PRIVATE);
        UsageProfile[][] profile = prefs.getUsageProfiles();
        cal = Calendar.getInstance();
        period = cal.get(Calendar.HOUR_OF_DAY);
        day = cal.get(Calendar.DAY_OF_WEEK) - 2;
        //isIdle = profile[day][period].isIdle();
    }



    Thread monitorThread = new Thread(new Runnable() {
        String highUsageApps = "";
        String[] processInfo;
        String appName;
        int processId;
        int processLoad;

        @Override
        public void run() {
            int a = 10000;
            while (!Thread.interrupted()) {
                a -= 1000;
                try {
                    Log.d("[hi]", "\n\nPrev " + a);
                    if(a < 0){
                        Notify("High CPU usage detected", highUsageApps);
                        stopRepeatingTask();
                    }


                } catch (Exception e) {
                    Log.d("[hi]", "\n\nPrev " + e.toString());
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Log.d("[hi]", "\n\nPrev " + e.toString());
                    Thread.currentThread().interrupt();
                }
            }
        }
    });

    public void startRepeatingTask(){
        monitorThread.start();
    }

    public void stopRepeatingTask() {
        monitorThread.interrupt();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (isIdle) {
            Notify("Idle", "shit");
        }
        screenOnTimer = DisplayMonitor.InteractionTimer.setupTimer();
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

    private void Notify(String title, String text) {

        final Notification.Builder mBuilder = new Notification.Builder(this);
        mBuilder.setStyle(new Notification.BigTextStyle(mBuilder)
                .bigText("The following applications could be rouge:\n" + text)
                .setBigContentTitle("High CPU detected")
                .setSummaryText("Big summary"))
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle("High CPU detected")
                .setContentText("Summary")
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true);
        /*NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setContentTitle(title)
                        .setContentText(text);*/
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        Intent resultIntent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);

        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_CANCEL_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());

        // ** intent for data usage settings ** resultIntent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity"));
    }

    public ArrayList<String> getCPU() {
        ArrayList<String> list = new ArrayList<String>();
        try {
            Process p = Runtime.getRuntime().exec("top -m 5 -d 250 -n 1");

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));

            String line;
            String finalString;
            String resultString;
            while ((line = reader.readLine()) != null) {
                finalString = line.trim();
                if (!line.isEmpty() && Character.isDigit(finalString.charAt(0))) {
                    resultString = finalString.replace("%", "");
                    Log.e("Output ", resultString);
                    list.add(resultString);
                }
            }

            p.waitFor();

        } catch (Exception e) {
        }
        return list;
    }

    public String getAppNameByPID(Context context, int pid) {
        PackageManager pm = this.getPackageManager();
        ActivityManager manager
                = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
            if (processInfo.pid == pid) {
                try {
                    PackageInfo pkgInfo = pm.getPackageInfo(processInfo.pkgList[0],
                            PackageManager.GET_ACTIVITIES);

                    if (!isSystemPackage(pkgInfo)) {
                        String appName = (String) pm.getApplicationLabel
                                (pm.getApplicationInfo(processInfo.processName, PackageManager.GET_META_DATA));
                        return appName;
                    }
                } catch (PackageManager.NameNotFoundException e) {
                }

            }
        }
        return "";
    }

    private boolean isSystemPackage(PackageInfo pkgInfo) {
        return (pkgInfo.applicationInfo.flags &
                ApplicationInfo.FLAG_SYSTEM) != 0;
    }
}
