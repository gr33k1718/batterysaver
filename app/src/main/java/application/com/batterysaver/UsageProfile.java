package application.com.batterysaver;

import android.provider.Settings;
import android.util.Log;

public class UsageProfile {

    private int predictedUsage;
    private int brightness;
    private long timeout;
    private int day;
    private int start;
    private int end;
    private long networkUsage;
    private long interactionTime;
    private float cpu;
    private int batteryLevel;
    private int batteryUsed;
    private int period;
    private long mobileUsage;
    private int charging;
    private SavingsProfile savingsProfile;
    private double[][] powerUsagePerDay = new double[7][5];
    private double[] powerUsageWeek = new double[5];
    private double[] timeUsageWeek = new double[5];
    private String usageType;


    public UsageProfile(int day, int start, int end, int charging, int brightness, long timeout,
                        int batteryLevel, int predictedUsage, long networkUsage, long mobileUsage,
                        long interactionTime, float cpu, String usageType) {
        this.usageType = usageType;

        this.day = day;
        this.start = start;
        this.end = end;

        this.batteryLevel = batteryLevel;
        this.predictedUsage = predictedUsage;

        this.interactionTime = interactionTime;
        this.brightness = brightness;
        this.timeout = timeout;

        this.cpu = cpu;

        this.networkUsage = networkUsage;
        this.mobileUsage = mobileUsage;

        this.charging = charging;

    }

    public long getMobileUsage() {
        return mobileUsage;
    }

    public void setMobileUsage(long mobileUsage) {
        this.mobileUsage = mobileUsage;
    }

    public double[] getTimeUsageWeek() {
        return timeUsageWeek;
    }

    public double[] getPowerUsageWeek() {
        return powerUsageWeek;
    }

    public double[][] getPowerUsagePerDay() {
        return powerUsagePerDay;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public int getBrightness() {
        return brightness;
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }

    public long getTimeout() {
        return timeout;
    }

    public int getPredictedUsage() {
        return predictedUsage;
    }

    public void setPredictedUsage(int predictedUsage) {
        this.predictedUsage = predictedUsage;
    }

    public String getUsageType() {
        return usageType;
    }

    public void setUsageType(String usageType) {
        this.usageType = usageType;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public long getNetworkUsage() {
        return networkUsage;
    }

    public void setNetworkUsage(long networkUsage) {
        this.networkUsage = networkUsage;
    }

    public long getInteractionTime() {
        return interactionTime;
    }

    public void setInteractionTime(long interactionTime) {
        this.interactionTime = interactionTime;
    }

    public float getCpu() {
        return cpu;
    }

    public void setCpu(float cpu) {
        this.cpu = cpu;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getBatteryUsed() {
        return batteryUsed;
    }

    public void setBatteryUsed(int batteryUsed) {
        this.batteryUsed = batteryUsed;
    }

    public SavingsProfile getSavingsProfile() {
        return savingsProfile;
    }

    public void setSavingsProfile(SavingsProfile savingsProfile) {
        this.savingsProfile = savingsProfile;
    }

    public void setMinimumProfile() {
        this.brightness = 30;
        this.timeout = 15000;
        Settings.System.putInt(MyApplication.getContentRes(),
                Settings.System.HAPTIC_FEEDBACK_ENABLED, 0);
        //ContentResolver.setMasterSyncAutomatically(false);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UsageProfile that = (UsageProfile) o;

        if (getDay() != that.getDay()) return false;
        return !(getUsageType() != null ? !getUsageType().equals(that.getUsageType()) : that.getUsageType() != null);

    }

    public UsageProfile merge(UsageProfile prev) {
        prev.setNetworkUsage(this.getNetworkUsage() + prev.getNetworkUsage());
        prev.setMobileUsage(this.getMobileUsage() + prev.getMobileUsage());
        prev.setInteractionTime((this.getInteractionTime() + prev.getInteractionTime()));
        prev.setCpu((int) (this.getCpu() + prev.getCpu()));
        prev.setBrightness((this.getBrightness() + prev.getBrightness()));
        prev.setTimeout((this.getTimeout() + prev.getTimeout()));
        prev.setStart(prev.getStart());
        prev.setBatteryLevel(this.getBatteryLevel());
        prev.setEnd(this.getEnd());
        prev.setDay(this.getDay());

        return prev;
    }




    @Override
    public String toString() {
        return "UsageProfile{" +
                "day=" + day +
                ", start=" + start +
                ", end=" + end +
                ", battery used =" + batteryUsed +
                ", networkUsage=" + networkUsage +
                ", mobileUsage=" + mobileUsage +
                ", interactionTime=" + interactionTime +
                ", cpu=" + cpu +
                ", brightness=" + brightness +
                ", timeout=" + timeout +
                '}';
    }
}
