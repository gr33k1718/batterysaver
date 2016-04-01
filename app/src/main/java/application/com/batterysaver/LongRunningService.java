package application.com.batterysaver;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class LongRunningService extends Service {
    private DisplayMonitor displayMonitor;
    private CpuMonitor cpuMonitor;

    @Override
    public void onCreate() {
        super.onCreate();
        displayMonitor = new DisplayMonitor();
        cpuMonitor = new CpuMonitor();

        displayMonitor.registerReceiver();
        cpuMonitor.monitorCpuLoad();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        displayMonitor.unregisterReceiver();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
