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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class CpuMonitor {

    private static Context context = MyApplication.getAppContext();
    private static PreferencesUtil pref = PreferencesUtil.getInstance(context, Constants.SYSTEM_CONTEXT_PREFS, Context.MODE_PRIVATE);
    private Thread cpuMonitor = new Thread(new CpuRunnable());

    Thread cpuThread = new Thread(new Runnable() {
        @Override
        public void run() {
            int cpuLoad = 0;
            int result = 0;

            for(int i = 0; i < 10; i++){

                try {
                    cpuLoad = CpuMonitor.newReadCpuUsage(120);
                } catch (NumberFormatException e) {
                    Log.e("[Error]", "" + e.toString());
                }

                Log.e("[Error]", "" + cpuLoad);

                result += cpuLoad;

                try {
                    Thread.sleep(180000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

            }
            Log.e("[Error]", "Average cpu " + (int) (result / 10.0));
            pref.putInt(Constants.CPU_LOAD_PREF_INT, (int) (result / 10.0));

            pref.commit();


        }
    });

    public void startCpuThread(){
        if(cpuThread.isAlive()){
            cpuThread.interrupt();
            cpuThread.start();
        }
        else{
            cpuThread.start();
        }
    }

    private boolean isAliveCpuThread(){
        return cpuThread.isAlive();
    }

    public void startCpuMonitor() {
        cpuMonitor.start();
    }

    public void stopCpuMonitor() {
        cpuMonitor.interrupt();
    }

    public boolean isAliveCpuMonitor() {
        return cpuMonitor.isAlive();
    }

    public static void clear() {

        pref.remove(Constants.CPU_LOAD_PREF);
        pref.commit();
    }

    public static int getTotalCpuLoad() {

        String prevCpuStats = pref.getString(Constants.CPU_LOAD_PREF, "cpu  0 0 0 0 0 0 0 0 0 0");
        String currentCpuStats = readCpuUsage();

        float result = findDifference(prevCpuStats, currentCpuStats);

        pref.putString(Constants.CPU_LOAD_PREF, currentCpuStats);
        pref.commit();

        return pref.getInt(Constants.CPU_LOAD_PREF_INT, 0);
    }

    private static float findDifference(String start, String end) {
        String[] prev = start.split(" +");
        String[] current = end.split(" +");

        long idle1 = Long.parseLong(prev[4]);
        long cpu1 = Long.parseLong(prev[1]) + Long.parseLong(prev[2]) + Long.parseLong(prev[3]) + Long.parseLong(prev[5])
                + Long.parseLong(prev[6]) + Long.parseLong(prev[7]) + Long.parseLong(prev[8]);

        long idle2 = Long.parseLong(current[4]);
        long cpu2 = Long.parseLong(current[1]) + Long.parseLong(current[2]) + Long.parseLong(current[3]) + Long.parseLong(current[5])
                + Long.parseLong(current[6]) + Long.parseLong(current[7]) + Long.parseLong(current[8]);

        long prevTotal = idle1 + cpu1;
        long total = idle2 + cpu2;

        long totald = total - prevTotal;
        long idled = idle2 - idle1;

        float result = (float) (totald - idled) / totald;

        return result > 0 ? result : (float) idle2 / total;
    }

    private static int spilt(String cpuLoad){
        String[] cpuTotal = cpuLoad.split(" +");

        return Integer.parseInt(cpuTotal[1]) + Integer.parseInt(cpuTotal[2]) + Integer.parseInt(cpuTotal[3]) + Integer.parseInt(cpuTotal[4]);


    }

    public static String readCpuUsage() {
        String usage = "";

        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");

            for (int y = 0; y < 2; y++) {
                usage = reader.readLine();
            }
            reader.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return usage;
    }

    public static int newReadCpuUsage(int delay) {
        String line = "";
        String resultString = "";
        int result = 0;
        String[] usage = null;
        try {
            Process p = Runtime.getRuntime().exec("top -m 1 -d " + delay + " -n 1");

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));

            for(int i = 0; i < 4; i++){
                line = reader.readLine();
            }

            resultString = line.replaceAll("[%a-zA-Z,]", "");
            resultString.trim();

            p.waitFor();

        } catch (Exception e) {
        }
        return spilt(resultString);
    }

    private ArrayList<String> getCpuLoadPerProcess() {
        ArrayList<String> list = new ArrayList<>();
        try {
            Process p = Runtime.getRuntime().exec("top -m 5 -d 420 -n 1");

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

    //TODO add actions for cancel and open
    private void cpuNotify(String title, String text) {

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

        stackBuilder.addParentStack(MainActivity.class);
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

    private boolean isSystemPackage(PackageInfo pkgInfo) {
        return (pkgInfo.applicationInfo.flags &
                ApplicationInfo.FLAG_SYSTEM) != 0;
    }

    private class CpuRunnable implements Runnable {
        private final int MAX_LOAD = 3;
        private final long SLEEP_TIME = 600000;
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
                        cpuNotify("High CPU usage detected", highUsageApps);
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
