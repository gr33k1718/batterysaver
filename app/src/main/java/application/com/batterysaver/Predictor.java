package application.com.batterysaver;

import android.util.Log;

/**
 * The Predictor class is responsible for estimating the battery consumption from the users data
 */
public class Predictor {

    /**
     * Calculates the estimated battery usage of the values given in terms of battery
     * life percentage lost
     * @param wifiTraffic the Wi-Fi traffic
     * @param mobileTraffic the mobile data traffic
     * @param cpuLoad the CPU load
     * @param interactionTime the screen interaction time
     * @param brightness the brightness value
     * @param wifiUsage the Wi-Fi user interaction time
     * @param mobileUsage the mobile network user interaction time
     * @return estimated battery usage
     */
    public static double predictBatteryUsage(long wifiTraffic, long mobileTraffic, int cpuLoad,
                                          long interactionTime, int brightness,
                                          long wifiUsage, long mobileUsage) {
        double batteryCapacity = 2550;

        double cpu = predictCpuBattery(cpuLoad);

        double network = predictNetworkBattery(wifiTraffic, mobileTraffic, wifiUsage, mobileUsage);

        double screen = predictInteractionBattery(interactionTime, brightness);

        double result = (cpu + network + screen) / batteryCapacity;

        return result * 100;
    }

    /**
     * Estimates the CPU load milliampere cost
     * @param cpuLoad the CPU load
     * @return the milliamphere cost
     */
    public static double predictCpuBattery(int cpuLoad) {

        if (cpuLoad <= 2) {
            return cpuLoad * 7;
        } else if (cpuLoad <= 25) {
            return 14 + (cpuLoad - 2) * 2.8;
        } else if (cpuLoad <= 50) {
            return 78 + (cpuLoad - 25) * 1.5;
        } else if (cpuLoad <= 75) {
            return 115 + (cpuLoad - 50) * 0.96;
        } else {
            return 140 + (cpuLoad - 75) * 0.5;
        }

    }

    /**
     * Estimates the display hardware milliampere cost
     * @param interactionTime the device interaction time
     * @param brightness the screen brightness
     * @return the milliampere cost
     */
    public static double predictInteractionBattery(long interactionTime, int brightness) {
        double hourInMilli = 3600000.0;
        double maxBright = 255.0;
        int mahScreenOn = 73;
        int mahScreenBrightness = 227;

        double brightPercent = brightness / maxBright;
        double timeOnPercent = interactionTime / hourInMilli;

        return (mahScreenOn * timeOnPercent) + (mahScreenBrightness * brightPercent) * timeOnPercent;
    }

    /**
     * Estimates the netwrok interface milliampere cost
     * @param wifiTraffic the traffic over Wi-Fi
     * @param mobileTraffic the traffic over mobile data network
     * @param wifiUsage the Wi-Fi user interaction time
     * @param mobileUsage the mobile network user interaction time
     * @return the milliampere cost
     */
    public static double predictNetworkBattery(long wifiTraffic, long mobileTraffic,
                                               long wifiUsage, long mobileUsage) {
        long wifiBits = wifiTraffic * 8;
        long mobileBits = mobileTraffic * 8;
        double wifiSpeed = 4500000;
        double mobileSpeed = 1000000;
        double secInHour = 3600;
        double minInHour = 60;
        double wifiMaxMah = 362;
        double mobileMaxMah = 350;
        double wifiIdleMah = 14;
        double mobileIdleMah = 5;
        double wifiMaxUsageMah = 275;
        double mobileMaxUsageMah = 150;

        double wifiTransferTime = wifiBits / wifiSpeed;
        double mobileTransferTime = mobileBits / mobileSpeed;

        double timeWifiPercent = wifiTransferTime / secInHour;
        double timeMobilePercent = mobileTransferTime / secInHour;

        double wifiIdleTime = 1 - timeWifiPercent;
        double mobileIdleTime = 1 - timeMobilePercent;

        double wifiUsageTime = (wifiUsage / minInHour) * wifiMaxUsageMah;
        double mobileUsageTime = (mobileUsage / minInHour) * mobileMaxUsageMah;

        double idleTimeMah = wifiTraffic > mobileTraffic ? (wifiIdleMah * wifiIdleTime) :
                (mobileIdleMah * mobileIdleTime);

        return (timeWifiPercent * wifiMaxMah) +
                (timeMobilePercent * mobileMaxMah)
                + idleTimeMah + wifiUsageTime + mobileUsageTime;


    }
}
