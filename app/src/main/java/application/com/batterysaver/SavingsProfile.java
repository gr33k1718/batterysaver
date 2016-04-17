package application.com.batterysaver;

import android.content.Context;
import android.text.format.Formatter;

public class SavingsProfile {
    private int brightness;
    private long timeout;
    private int sync;
    private long networkWarningLimit;
    private boolean setCpuMonitor = false;
    private boolean setNetworkMonitor = false;
    private Context context = MyApplication.getAppContext();
    private UsageProfile usageProfile;

    public SavingsProfile(UsageProfile usageProfile) {
        this.usageProfile = usageProfile;
    }

    public int getSyncTime() {
        return sync;
    }

    public int getBrightness() {
        return brightness;
    }

    public long getTimeout() {
        return timeout;
    }

    public long getNetworkWarningLimit() {
        return networkWarningLimit;
    }

    public boolean isSetCpuMonitor() {
        return setCpuMonitor;
    }

    private int profileLength() {
        int startTime = usageProfile.getStart();
        int endTime = usageProfile.getEnd();
        int length = endTime - startTime;
        return length > 22 || length == 0 ? 1 : length;
    }

    private float scale(float valueIn, float baseMax, float limitMax) {
        if (valueIn > baseMax) {
            return limitMax;
        }
        if (valueIn < 0) {
            return 0;
        }
        return (valueIn / baseMax) * limitMax;
    }

    private int calculateBrightness(int brightness) {
        float maxPercent = 100;
        int maxBrightness = 255;
        return (int) ((brightness / maxPercent) * maxBrightness);
    }

    private void setDefault(int brightness) {
        timeout = 15000;
        sync = 60;
        this.brightness = calculateBrightness(brightness);
    }

    private int minToMilli(int min) {
        return min * 60 * 1000;
    }

    private int mbToBytes(int mb) {
        return mb * 1000000;
    }

    public SavingsProfile generate() {
        int length = profileLength();
        int cpu = (int) usageProfile.getCpu();
        int maxCpu = 50;
        long traffic = usageProfile.getNetworkUsage();
        long maxTraffic;
        long interaction = usageProfile.getInteractionTime();
        int usage = usageProfile.getNetworkUsageTime();
        String usageType = usageProfile.getUsageType();
        long maxInteraction;
        int minBrightness;
        int brightnessScale;


        if (cpu > maxCpu) {
            setNetworkMonitor = true;
        }

        switch (usageType) {
            case "minimal":
                setDefault(20);
                break;

            case "low":
                setDefault(30);
                break;

            case "medium":
                maxTraffic = mbToBytes(10) * length;
                maxInteraction = minToMilli(15) * length;
                minBrightness = 20;
                brightnessScale = 20;
                setSettings(interaction, maxInteraction, traffic,
                        maxTraffic, usage, brightnessScale, minBrightness);
                break;
            case "high":
                maxTraffic = mbToBytes(15) * length;
                maxInteraction = minToMilli(30) * length;
                minBrightness = 30;
                brightnessScale = 20;
                setSettings(interaction, maxInteraction, traffic,
                        maxTraffic, usage, brightnessScale, minBrightness);
                break;
        }

        return this;
    }

    private void setSettings(long interaction, long maxInteraction, long traffic,
                             long maxTraffic, int usage, int brightnessScale, int minBrightness) {
        double[] trafficReduction = new double[]{0.8, 0.7, 0.6};
        int[] timeoutSetting = new int[]{12000, 6000, 3000, 1500};
        int[] syncSetting = new int[]{60, 30, 15, 5};

        brightness = calculateBrightness((int) scale(interaction, maxInteraction, brightnessScale)
                + minBrightness);
        timeout = timeoutSetting[(int) scale(interaction, maxInteraction, 3)];
        networkWarningLimit = (long) (traffic * trafficReduction[(int) scale(traffic, maxTraffic, 2)]);
        sync = syncSetting[(int) scale(usage, maxInteraction, 3)];
    }

    @Override
    public String toString() {
        return "brightness: " + Utils.round(brightness / 255.0 * 100, 1) + "%" +
                "\ntimeout: " + Utils.msToString(timeout) +
                (setNetworkMonitor ? "\nNetwork Limit: " + Formatter.formatShortFileSize(context, networkWarningLimit) : "") +
                (setCpuMonitor ? "\nCpu monitor: active" : "");
    }
}
