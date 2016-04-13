package application.com.batterysaver;

import android.util.Log;
public class Predictor {

    public static int predictBatteryUsage(long wifiTraffic, long mobileTraffic, int cpuLoad, long interactionTime, int brightness) {
        double batteryCapacity = 2550.0;

        double cpu = predictCpuBattery(cpuLoad);

        double network = predictNetworkBattery(wifiTraffic, mobileTraffic);

        double screen = predictInteractionBattery(interactionTime, brightness);

        double result = (cpu + network + screen) / batteryCapacity;

        return (int) (Math.round(result * 100));
    }

    public static int milliAmpPerHour(long wifiTraffic, long mobileTraffic, int cpuLoad, long interactionTime, int brightness) {

        double cpu = predictCpuBattery(cpuLoad);

        double network = predictNetworkBattery(wifiTraffic, mobileTraffic);

        double screen = predictInteractionBattery(interactionTime, brightness);

        double result = (cpu + network + screen);

        Log.e("[Error]", "Cpu " + cpu + " Network " + network + " Screen " + screen);

        return (int) result;
    }

    public static double predictCpuBattery(int cpuLoad) {
        return 90 * (cpuLoad/100.0);
        /*
        if (cpuLoad <= 2) {
            return 14;
        } else if (cpuLoad <= 25) {
            return 14 + (cpuLoad - 2) * 2.8;
        } else if (cpuLoad <= 50) {
            return 78 + (cpuLoad - 25) * 1.5;
        } else if (cpuLoad <= 75) {
            return 115 + (cpuLoad - 50) * 0.96;
        } else {
            return 140 + (cpuLoad - 75) * 0.5;
        }*/
    }

    public static double predictInteractionBattery(long interactionTime, int brightness) {
        double hourInMilli = 3600000.0;
        double maxBright = 255.0;
        int mahScreenOn = 73;
        int mahScreenBrightness = 227;

        double brightPercent = brightness / maxBright;
        double timeOnPercent = interactionTime / hourInMilli;

        return (mahScreenOn * timeOnPercent) + (mahScreenBrightness * brightPercent) * timeOnPercent;
    }

    public static double predictNetworkBattery(long wifiTraffic, long mobileTraffic) {
        long wifiBits = wifiTraffic * 8;
        long mobileBits = mobileTraffic * 8;
        double wifiSpeed = 4500000;
        double mobileSpeed = 1000000;
        double secInHour = 3600;
        double wifiMaxMah = 362;
        double mobileMaxMah = 350;
        double wifiIdleMah = 14;
        double mobileIdleMah = 5;


        double wifiTransferTime = wifiBits / wifiSpeed;
        double mobileTransferTime = mobileBits / mobileSpeed;

        double timeWifiPercent = wifiTransferTime / secInHour;
        double timeMobilePercent = mobileTransferTime / secInHour;

        double wifiIdleTime = 1 - timeWifiPercent;
        double mobileIdleTime = 1 - timeMobilePercent;

        double idleTimeMah = wifiTraffic > mobileTraffic ? (wifiIdleMah * wifiIdleTime) :
                (mobileIdleMah * mobileIdleTime);

        return (timeWifiPercent * wifiMaxMah) + (timeMobilePercent * mobileMaxMah) + idleTimeMah;


    }
}
