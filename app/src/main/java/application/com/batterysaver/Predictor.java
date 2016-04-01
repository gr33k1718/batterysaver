package application.com.batterysaver;

public class Predictor {

    public static int predictBatteryUsage(long wifiTraffic, long mobileTraffic, int cpuLoad, long interactionTime, int brightness) {
        double batteryCapacity = 2550.0;

        double cpu = predictCpuBattery(cpuLoad);

        double network = predictNetworkBattery(wifiTraffic, mobileTraffic);

        double screen = predictInteractionBattery(interactionTime, brightness);

        double result = (cpu + network + screen) / batteryCapacity;

        return (int) (Math.round(result * 100));
    }

    public static int milliAmpPerHpur(long wifiTraffic, long mobileTraffic, int cpuLoad, long interactionTime, int brightness) {

        double cpu = predictCpuBattery(cpuLoad);

        double network = predictNetworkBattery(wifiTraffic, mobileTraffic);

        double screen = predictInteractionBattery(interactionTime, brightness);

        double result = (cpu + network + screen);

        return (int) result;
    }

    public static double predictCpuBattery(int cpuLoad) {
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
        }
    }

    public static double predictInteractionBattery(long interactionTime, int brightness) {
        double hourInMilli = 3600000.0;
        double maxBright = 255.0;
        int brightPercent = (int) (brightness / maxBright) * 100;
        double timeOnPercent = interactionTime / hourInMilli;

        if (brightPercent <= 20) {
            return timeOnPercent * 49.0;
        } else if (brightPercent <= 60) {
            return timeOnPercent * 64.0;
        } else {
            return timeOnPercent * 153.0;
        }
    }

    public static double predictNetworkBattery(long wifiTraffic, long mobileTraffic) {
        long wifiBits = wifiTraffic * 8;
        long mobileBits = mobileTraffic * 8;
        double wifiSpeed = 4500000.0;
        double mobileSpeed = 1000000.0;
        double secInHour = 3600.0;
        double wifiMaxMah = 362;
        double mobileMaxMah = 350;

        double timeWifi = (wifiBits / wifiSpeed) / secInHour;
        double timeMobile = (mobileBits / mobileSpeed) / secInHour;

        return (timeWifi * wifiMaxMah) + (timeMobile * mobileMaxMah);

    }
}
