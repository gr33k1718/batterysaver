package application.com.batterysaver;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class CpuMonitor {

    private static Context context = MyApplication.getAppContext();
    private static PreferencesUtil pref = PreferencesUtil.getInstance(context, Constants.SYSTEM_CONTEXT_PREFS, Context.MODE_PRIVATE);
    private Thread cpuMonitor = new Thread(new CpuRunnable());

    /**
     * Parses the string representation of CPU load and calculates total load.
     *
     * @param cpuLoad string CPU representation
     * @return total CPU load
     */
    private static int parseCpuLoad(String cpuLoad) {
        String[] cpuTotal = cpuLoad.split(" +");

        return Integer.parseInt(cpuTotal[1]) + Integer.parseInt(cpuTotal[2])
                + Integer.parseInt(cpuTotal[3]) + Integer.parseInt(cpuTotal[4]);
    }

    public static void monitorCpuUsage(int count) {
        int prevCpuLoad = pref.getInt(Constants.CPU_LOAD_PREF_INT, 0);

        int curCpuLoad = readCpuUsage(5);

        int load = (int) Math.round(movingAvg(count, prevCpuLoad, curCpuLoad));

        pref.putInt(Constants.CPU_LOAD_PREF_INT, load);
        pref.commit();

    }

    public static int readCpuUsage(int delay) {
        String line = "";
        String resultString = "";
        int result = 0;

        try {
            Process p = Runtime.getRuntime().exec("top -m 1 -d " + delay + " -n 1");

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));

            for (int i = 0; i < 4; i++) {
                line = reader.readLine();
            }

            resultString = line.replaceAll("[%a-zA-Z,]", "");
            resultString.trim();
            result = parseCpuLoad(resultString);

            p.waitFor();

        } catch (Exception e) {
        }
        return result;
    }

    private static double movingAvg(int count, int prev, int cur) {
        return ((prev * count) + cur) / (double) (count + 1);
    }

    /**
     * Retrieves the total CPU load from shared preferences.
     *
     * @return total CPU load
     */
    public int getTotalCpuLoad() {

        pref.commit();
        return pref.getInt(Constants.CPU_LOAD_PREF_INT, 0);
    }

    /**
     * Starts the CPU monitor thread
     */
    public void startCpuMonitor() {
        cpuMonitor.start();
    }

    /**
     * Causes the CPU monitor thread to be interrupted allowing the garbage collector to
     * remove it from memory.
     */
    public void stopCpuMonitor() {
        cpuMonitor.interrupt();
    }

    /**
     * Determines if the CPU monitor thread is active.
     *
     * @return whether the thread is active
     */
    public boolean isAlive() {
        return cpuMonitor.isAlive();
    }

    public void activateNetworkMonitor() {
        if (!isAlive()) {
            startCpuMonitor();
        } else {
            stopCpuMonitor();
            startCpuMonitor();
        }
    }

    /**
     * Determines the CPU load of the top five processes over a 7 minute period.
     *
     * @return the list of processes
     */
    private ArrayList<String> getCpuLoadPerProcess() {
        String line;
        String trimmedString;
        String resultString;
        ArrayList<String> list = new ArrayList<>();

        try {
            Process p = Runtime.getRuntime().exec("top -m 5 -d 5 -n 1");

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));


            while ((line = reader.readLine()) != null) {
                trimmedString = line.trim();
                if (!line.isEmpty() && Character.isDigit(trimmedString.charAt(0))) {
                    resultString = trimmedString.replace("%", "");
                    Log.e("Output ", resultString);
                    list.add(resultString);
                }
            }

            p.waitFor();

        } catch (Exception e) {
        }
        return list;
    }

    /**
     * Creates a notification that launches when CPU load is exceeded. Ensures correct management
     * of the backstack allowing for correct navigation
     *
     * @param text applications that exceed CPU load
     */
    private void cpuNotify(String text) {

        final Notification.Builder mBuilder = new Notification.Builder(context);
        mBuilder.setStyle(new Notification.BigTextStyle(mBuilder)
                .bigText("The following applications could be rouge:\n" + text)
                .setBigContentTitle("High CPU load")
                .setSummaryText("Big summary"))
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle("High CPU load")
                .setContentText("Please review")
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

    /**
     * Gets the package name of the process id
     *
     * @param context the application context
     * @param pid     process id
     * @return package name associated with the process id
     */
    private String getAppNameByPID(Context context, int pid) {
        PackageManager pm = context.getPackageManager();
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

    /**
     * Determines whether the given package is an Android application or third party
     *
     * @param pkgInfo given package to check
     * @return whether system flag has been set
     */
    private boolean isSystemPackage(PackageInfo pkgInfo) {
        return (pkgInfo.applicationInfo.flags &
                ApplicationInfo.FLAG_SYSTEM) != 0;
    }

    private class CpuRunnable implements Runnable {
        private final int MAX_LOAD = 10;
        private final long SLEEP_TIME = 300000;
        private String highUsageApps = "";
        private String[] processInfo;
        private String appName;
        private int processId;
        private int processLoad;

        @Override
        public void run() {
            while (!Thread.interrupted()) {
                try {
                    for (String i : getCpuLoadPerProcess()) {
                        processInfo = i.split(" +");
                        processId = Integer.parseInt(processInfo[0]);
                        processLoad = Integer.parseInt(processInfo[2]);
                        appName = getAppNameByPID(context, processId);
                        if (!appName.equals("") && processLoad > MAX_LOAD) {

                            highUsageApps += appName + "\n";
                        }
                    }
                    if (!highUsageApps.equals("")) {
                        cpuNotify(highUsageApps);
                        highUsageApps = "";
                    }

                } catch (Exception e) {
                    Log.e("[Error]", e.toString());
                }
                try {
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    Log.e("[Error]", e.toString());
                }
            }
        }
    }
}
