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

    private static Context context = GlobalVars.getAppContext();
    private static PreferencesUtil pref = PreferencesUtil.getInstance(context, Constants.SYSTEM_CONTEXT_PREFS, Context.MODE_PRIVATE);

    public static int getLoad() {

        String prevCpuStats = pref.getString(Constants.CPU_LOAD_PREF, "cpu  0 0 0 0 0 0 0 0 0 0");
        String currentCpuStats = readCpuUsage().cpu;

        float result = findDifference(prevCpuStats, currentCpuStats);

        pref.putString(Constants.CPU_LOAD_PREF, currentCpuStats);
        pref.commit();

        //Log.d("[shit]", result +"\n" + prevCpuStats + "\n" + currentCpuStats);

        return  Math.round(result * 100);
    }

    public static void clear() {

        pref.remove(Constants.CPU_LOAD_PREF);
        pref.commit();
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

    public static CPUInfo readCpuUsage() {
        CPUInfo info = new CPUInfo();
        String cpu = "";
        int count = 0;
        try {

            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
            /*for (int i = 0; i < 9; i++) {
                String s = reader.readLine();
                if (s.contains("cpu")) {
                    count++;
                } else {
                    break;
                }
            }*/
            // reader.seek(0);
            for (int y = 0; y < 2; y++) {
                cpu = reader.readLine();
            }

            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        info.numProc = count;
        info.cpu = cpu;

        //Log.d("[shit]", cpu);
        return info;
    }

    private static class CPUInfo {
        public static int numProc;
        public static String cpu;

    }

    public void startCpuMonitor(){
        monitorThread.start();
    }

    public void stopCpuMonitor() {
        monitorThread.interrupt();
    }

    public boolean isAlive(){
        return monitorThread.isAlive();
    }

    Thread monitorThread = new Thread(new Runnable() {
        String highUsageApps = "";
        String[] processInfo;
        String appName;
        int processId;
        int processLoad;

        @Override
        public void run() {
            while (!Thread.interrupted()) {
                try {
                    for (String i : getCPU()) {
                        processInfo = i.split(" +");
                        processId = Integer.parseInt(processInfo[0]);
                        processLoad = Integer.parseInt(processInfo[2]);
                        appName = getAppNameByPID(context, processId);
                        if (!appName.equals("") && processLoad > 3) {
                            Log.d("[shit]", "\nTime: " + appName + " " + processInfo[2]);
                            highUsageApps += appName + "\n";
                        }
                    }
                    if (!highUsageApps.equals("")) {
                        cpuNotify("High CPU usage detected", highUsageApps);
                        highUsageApps = "";
                    }

                } catch (Exception e) {

                }
                try {
                    Thread.sleep(600000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });
    //TODO add actions for cancel and open
    private void cpuNotify(String title, String text) {

        final Notification.Builder mBuilder = new Notification.Builder(context);
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

        // ** intent for data usage settings ** resultIntent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity"));
    }

    public ArrayList<String> getCPU() {
        ArrayList<String> list = new ArrayList<String>();
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

    public String getAppNameByPID(Context context, int pid) {
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





}
