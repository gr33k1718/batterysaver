package application.com.batterysaver;

import android.content.Context;
import android.net.TrafficStats;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class NetworkContext {
    private static Context context = GlobalVars.getAppContext();
    private static PreferencesUtil pref = PreferencesUtil.getInstance(context, Constants.NETWORK_PREFS, Context.MODE_PRIVATE);

    private static final String RX_FILE = "/sys/class/net/wlan0/statistics/rx_bytes";
    private static final String TX_FILE = "/sys/class/net/wlan0/statistics/tx_bytes";


    public static long[] getTrafficStats() {
        long[] stats = new long[2];
        long prevNetworkStats = pref.getLong(Constants.NETWORK_TRAFFIC_PREF, 0);
        long prevMobileStats = pref.getLong(Constants.MOBILE_TRAFFIC_PREF, 0);

        long currentNetworkStats = readFile(TX_FILE) + readFile(RX_FILE);
        long currentMobileStats = TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes();

        long wifiTraffic = currentNetworkStats - prevNetworkStats;
        long mobileTraffic = currentMobileStats - prevMobileStats;

        pref.putLong(Constants.MOBILE_TRAFFIC_PREF, currentMobileStats);
        pref.putLong(Constants.NETWORK_TRAFFIC_PREF, currentNetworkStats);

        pref.commit();

        if (wifiTraffic >= 0 && mobileTraffic >= 0) {
            stats[0] = wifiTraffic;
            stats[1] = mobileTraffic;
        } else {
            stats[0] = 0;
            stats[1] = 0;
        }

        return stats;
    }

    public static void clearTraffic() {
        pref.remove(Constants.MOBILE_TRAFFIC_PREF);
        pref.remove(Constants.NETWORK_TRAFFIC_PREF);
        pref.commit();
    }

    public static int wifiSpeed() {
        Integer linkSpeed = 0;
        WifiManager wifiManager = (WifiManager) GlobalVars.getAppContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            linkSpeed = wifiInfo.getLinkSpeed();
        }

        return linkSpeed;
    }

    public static long readFile(String fileName) {
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
}
