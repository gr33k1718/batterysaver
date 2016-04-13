package application.com.batterysaver;

import android.content.Context;
import android.text.format.Formatter;

public class SavingsProfile {
    private int day;
    private int startTime;
    private int endtime;
    private int brightness;
    private long timeout;
    private long networkWarningLimit;
    private boolean setCpuMonitor = false;
    private boolean setNetworkMonitor = false;
    private Context context = MyApplication.getAppContext();
    private UsageProfile usageProfile;

    public SavingsProfile(UsageProfile usageProfile) {
        this.usageProfile = usageProfile;
    }

    public SavingsProfile generate() {

        return this;
    }

    public int getDay() {
        return day;
    }

    public int getStartTime() {
        return startTime;
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

    public boolean isSetNetworkMonitor() {
        return setNetworkMonitor;
    }
    /*
    public SavingsProfile generate() {
        String usageType = usageProfile.getUsageType();

        int brightness = usageProfile.getBrightness();
        long timeout = usageProfile.getTimeout();

        this.day = usageProfile.getDay();
        this.startTime = usageProfile.getStart();
        this.endtime = usageProfile.getEnd();
        this.timeout = 15000;


        if (usageProfile.getUsageType()) {
            this.brightness = 30;
        } else {
            if (brightness > 168) {
                this.brightness = 168;
            } else {
                this.brightness = brightness;
            }
        }
        if (usageProfile.isHighInteraction()) {
            if (brightness > 110) {
                this.brightness = (int) (brightness * 0.7);
            } else {
                this.brightness = brightness;
            }
            if (timeout < 45000) {
                this.timeout = 15000;
            } else if (timeout < 75000) {
                this.timeout = 30000;
            } else if (timeout < 210000) {
                this.timeout = 60000;
            } else if (timeout < 450000) {
                this.timeout = 120000;
            } else {
                this.timeout = 300000;
            }

        }
        if (usageProfile.isHighCPU()) {
            this.setCpuMonitor = true;
        }
        if (usageProfile.isHighNetwork()) {
            this.setNetworkMonitor = true;
            this.networkWarningLimit = (long) (usageProfile.getNetworkUsage() * 0.75);
        }

        return this;
    }
    */
    @Override
    public String toString() {
        return "start: " + startTime +
                "\nbrightness: " + Utils.round(brightness / 255.0 * 100, 1) + "%" +
                "\ntimeout: " + Utils.msToString(timeout) +
                (setNetworkMonitor ? "\nNetwork Limit: " + Formatter.formatShortFileSize(context, networkWarningLimit) : "") +
                (setCpuMonitor ? "\nCpu monitor: active" : "");
    }
}
