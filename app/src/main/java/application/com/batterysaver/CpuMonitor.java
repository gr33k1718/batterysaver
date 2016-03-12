package application.com.batterysaver;

import android.content.Context;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

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


}
