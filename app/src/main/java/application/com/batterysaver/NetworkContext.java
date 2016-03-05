package application.com.batterysaver;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

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

        long currentNetworkStats = readFile(TX_FILE) + readFile(RX_FILE);
        long currentMobileStats = TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes();

        long currentTotalBytes = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes();

        pref.putLong("TOTAL_TRAFFIC", currentTotalBytes);

        if(currentMobileStats == 0){
            currentMobileStats = prevMobileStats;
        }

        if(currentNetworkStats == 0){
            currentNetworkStats = prevNetworkStats;
        }

        long wifiTraffic = currentNetworkStats - prevNetworkStats;
        long mobileTraffic = currentMobileStats - prevMobileStats;
        long totalBytes = currentTotalBytes - prevTotalBytes;


        if(is3g){
            long mobile = totalBytes - mobileTraffic;
            stats[0] = mobile > 0 ? mobile : 0;
            stats[1] = mobileTraffic;
            pref.putLong(Constants.MOBILE_TRAFFIC_PREF, currentMobileStats);
        }
        else if(isWifi){
            long wifi = totalBytes - wifiTraffic;
            stats[0] = wifiTraffic;
            stats[1] = wifi > 0 ? wifi : 0;
            pref.putLong(Constants.NETWORK_TRAFFIC_PREF, currentNetworkStats);
        }
        else {
            stats[0] = 0;
            stats[1] = 0;
        }
        pref.commit();

        /*Log.d("[shit]", "Previous mobile " + prevMobileStats + "\nPrevious network " + prevNetworkStats +
                "\nCurrent mobile " + currentMobileStats + "\nCurrent network " + currentNetworkStats + "\nTotal network " + stats[1] +
                "\nTotal mobile " + stats[0] + "\nTotal bytes " + totalBytes + "\nMobile " + (totalBytes - mobileTraffic)
                + "\nWifi " + (totalBytes - wifiTraffic));
*/
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
