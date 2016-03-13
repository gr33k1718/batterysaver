package application.com.batterysaver;

import android.content.ContentResolver;
import android.provider.Settings;

public class UsageProfile {

    private boolean idle = false;
    private int brightness;
    private long timeout;
    private boolean highNetwork = false;
    private boolean highCPU = false;
    private boolean highInteraction = false;
    private int day;
    private int start;
    private int end;
    private long networkUsage;
    private long interactionTime;
    private float cpu;
    private int batteryLevel;
    private int batteryUsed;
    private int period;
    private int charging;
    private SavingsProfile savingsProfile;

    public UsageProfile(){}

    public UsageProfile(int day, int start, int end, int charging, int brightness, long timeout,  int batteryLevel, long networkUsage, long interactionTime, float cpu) {
        this.brightness = brightness;
        this.timeout = timeout;
        this.day = day;
        this.start = start;
        this.networkUsage = networkUsage;
        this.interactionTime = interactionTime;
        this.cpu = cpu;
        this.batteryLevel = batteryLevel;
        this.end = end;
        this.charging = charging;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public boolean isHighInteraction() {
        return highInteraction;
    }

    public void setHighInteraction(boolean highInteraction) {
        this.highInteraction = highInteraction;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public boolean isIdle() {
        return idle;
    }

    public void setIdle(boolean idle) {
        this.idle = idle;
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

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public void setHighNetwork(boolean highNetwork) {
        this.highNetwork = highNetwork;
    }

    public boolean isHighNetwork() {
        return highNetwork;
    }

    public boolean isHighCPU() {
        return highCPU;
    }

    public void setHighCPU(boolean highCPU) {
        this.highCPU = highCPU;
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
        Settings.System.putInt(GlobalVars.getContentRes(),
                Settings.System.HAPTIC_FEEDBACK_ENABLED, 0);
        //ContentResolver.setMasterSyncAutomatically(false);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UsageProfile that = (UsageProfile) o;

        if (isIdle() != that.isIdle()) return false;
        if (isHighNetwork() != that.isHighNetwork()) return false;
        if (isHighCPU() != that.isHighCPU()) return false;
        if (isHighInteraction() != that.isHighInteraction()) return false;
        return getDay() == that.getDay();

    }

    public UsageProfile merge(UsageProfile prev){
        prev.setNetworkUsage((this.getNetworkUsage() + prev.getNetworkUsage()));
        prev.setInteractionTime((this.getInteractionTime() + prev.getInteractionTime())/2);
        prev.setCpu((int)(this.getCpu() + prev.getCpu()) / 2);
        prev.setBrightness((this.getBrightness() + prev.getBrightness()) / 2);
        prev.setTimeout((this.getTimeout() + prev.getTimeout()) / 2);
        prev.setStart(prev.getStart());
        prev.setBatteryLevel(this.getBatteryLevel());
        prev.setEnd(this.getEnd());
        prev.setDay(this.getDay());
        prev.setIdle(this.isIdle());
        prev.setHighCPU(this.isHighCPU());
        prev.setHighNetwork(this.isHighNetwork());
        prev.setHighInteraction(this.isHighInteraction());

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
                ", interactionTime=" + interactionTime +
                ", cpu=" + cpu +
                ", brightness=" + brightness +
                ", timeout=" + timeout +
                '}';
    }

}
