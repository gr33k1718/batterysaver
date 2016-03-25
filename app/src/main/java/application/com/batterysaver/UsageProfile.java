package application.com.batterysaver;

import android.provider.Settings;
import android.util.Log;

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
    private long mobileUsage;
    private int charging;
    private SavingsProfile savingsProfile;
    private double[][] powerUsagePerDay = new double[7][5];
    private double[] powerUsageWeek = new double[5];
    private double[] timeUsageWeek = new double[5];

    public UsageProfile() {
    }

    public UsageProfile(int day, int start, int end, int charging, int brightness, long timeout, int batteryLevel, long networkUsage, long mobileUsage, long interactionTime, float cpu) {
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
        this.mobileUsage = mobileUsage;
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

    public boolean isHighNetwork() {
        return highNetwork;
    }

    public void setHighNetwork(boolean highNetwork) {
        this.highNetwork = highNetwork;
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
        Settings.System.putInt(MyApplication.getContentRes(),
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

    public UsageProfile merge(UsageProfile prev) {
        prev.setNetworkUsage(this.getNetworkUsage() + prev.getNetworkUsage());
        prev.setMobileUsage(this.getMobileUsage() + prev.getMobileUsage());
        prev.setInteractionTime((this.getInteractionTime() + prev.getInteractionTime()) / 2);
        prev.setCpu((int) (this.getCpu() + prev.getCpu()) / 2);
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

    public void generateStats() {

        DatabaseLogger d = new DatabaseLogger(MyApplication.getAppContext());

        int idleTime = 0;
        int casualTime = 0;
        int highInterTime = 0;
        int highCpuTime = 0;
        int highNetTime = 0;

        int batteryDay = 0;

        int totalIdleTime = 0;
        int totalCasualTime = 0;
        int totalHighInterTime = 0;
        int totalHighCpuTime = 0;
        int totalHighNetTime = 0;

        int batteryIdle = 0;
        int batteryCasual = 0;
        int batteryHighInter = 0;
        int batteryHighCpu = 0;
        int batteryHighNet = 0;

        int totalBattery = 0;

        int totalBatteryIdle = 0;
        int totalBatteryCasual = 0;
        int totalBatteryHighInter = 0;
        int totalBatteryHighCpu = 0;
        int totalBatteryHighNet = 0;

        UsageProfile[][] b = d.getUsagePatterns();

        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 24; j++) {
                UsageProfile a = b[i][j];
                if (a != null) {
                    int end = a.getEnd() == 0 ? 24 : a.getEnd();

                    if (a.isIdle()) {
                        if (a.isHighCPU() && a.isHighNetwork()) {
                            highCpuTime += (end - a.getStart()) / 2;
                            highNetTime += (end - a.getStart()) / 2;

                            batteryHighCpu += a.getBatteryUsed() / 2;
                            batteryHighNet += a.getBatteryUsed() / 2;
                        } else if (a.isHighNetwork()) {
                            highNetTime += (end - a.getStart());
                            batteryHighNet += a.getBatteryUsed();
                        } else if (a.isHighCPU()) {
                            highCpuTime += (end - a.getStart());
                            batteryHighCpu += a.getBatteryUsed();
                        } else {
                            idleTime += (end - a.getStart());
                            batteryIdle += a.getBatteryUsed();
                        }

                    } else if (a.isHighInteraction()) {
                        if (a.isHighCPU() && a.isHighNetwork()) {
                            highCpuTime += (end - a.getStart()) / 3;
                            highNetTime += (end - a.getStart()) / 3;

                            batteryHighCpu += a.getBatteryUsed() / 3;
                            batteryHighNet += a.getBatteryUsed() / 3;

                            highInterTime += (end - a.getStart()) / 3;
                            batteryHighInter += a.getBatteryUsed() / 3;
                        } else if (a.isHighNetwork()) {
                            highNetTime += (end - a.getStart()) / 2;
                            batteryHighNet += a.getBatteryUsed() / 2;

                            highInterTime += (end - a.getStart() / 2);
                            batteryHighInter += a.getBatteryUsed() / 2;
                        } else if (a.isHighCPU()) {
                            highCpuTime += (end - a.getStart()) / 2;
                            batteryHighCpu += a.getBatteryUsed() / 2;

                            highInterTime += (end - a.getStart()) / 2;
                            batteryHighInter += a.getBatteryUsed() / 2;
                        } else {
                            highInterTime += (end - a.getStart());
                            batteryHighInter += a.getBatteryUsed();
                        }

                    } else {
                        if (a.isHighCPU() && a.isHighNetwork()) {
                            highCpuTime += (end - a.getStart()) / 2;
                            highNetTime += (end - a.getStart()) / 2;

                            batteryHighNet += a.getBatteryUsed() / 2;
                            batteryHighNet += a.getBatteryUsed() / 2;
                        } else if (a.isHighNetwork()) {
                            highNetTime += (end - a.getStart());
                            batteryHighNet += a.getBatteryUsed();
                        } else if (a.isHighCPU()) {
                            highCpuTime += (end - a.getStart());
                            batteryHighCpu += a.getBatteryUsed();
                        } else {
                            casualTime += (end - a.getStart());
                            batteryCasual += a.getBatteryUsed();
                        }
                    }
                    batteryDay += a.getBatteryUsed();
                    totalBattery += a.getBatteryUsed();
                    Log.d("[Savings]", "Savings Profile: " + a + " " + a.isIdle() + " " + a.isHighInteraction() + " " + a.isHighNetwork() + " " + a.isHighCPU());
                }
            }
            powerUsagePerDay[i][0] = Utils.round(batteryIdle / (double) batteryDay * 100, 2);
            powerUsagePerDay[i][1] = Utils.round(batteryCasual / (double) batteryDay * 100, 2);
            powerUsagePerDay[i][2] = Utils.round(batteryHighInter / (double) batteryDay * 100, 2);
            powerUsagePerDay[i][3] = Utils.round(batteryHighNet / (double) batteryDay * 100, 2);
            powerUsagePerDay[i][4] = Utils.round(batteryHighCpu / (double) batteryDay * 100, 2);
            Log.d("[Savings]", "Savings Profile: " + batteryIdle + " " + batteryCasual + " " + batteryHighInter + " " + batteryHighNet + " " + batteryHighCpu + " " + batteryDay);

            totalHighNetTime += highNetTime;
            totalCasualTime += casualTime;
            totalIdleTime += idleTime;
            totalHighInterTime += highCpuTime;
            totalHighCpuTime += highInterTime;
            totalBatteryHighNet += batteryHighNet;
            totalBatteryCasual += batteryCasual;
            totalBatteryIdle += batteryIdle;
            totalBatteryHighInter += batteryHighInter;
            totalBatteryHighCpu += batteryHighCpu;
            idleTime = 0;
            casualTime = 0;
            highInterTime = 0;
            highCpuTime = 0;
            highNetTime = 0;

            batteryHighNet = 0;
            batteryCasual = 0;
            batteryIdle = 0;
            batteryHighInter = 0;
            batteryHighCpu = 0;

            batteryDay = 0;


        }
        powerUsageWeek[0] = Utils.round(totalBatteryIdle / (double) totalBattery * 100, 2);
        powerUsageWeek[1] = Utils.round(totalBatteryCasual / (double) totalBattery * 100, 2);
        powerUsageWeek[2] = Utils.round(totalBatteryHighInter / (double) totalBattery * 100, 2);
        powerUsageWeek[3] = Utils.round(totalBatteryHighNet / (double) totalBattery * 100, 2);
        powerUsageWeek[4] = Utils.round(totalBatteryHighCpu / (double) totalBattery * 100, 2);

        timeUsageWeek[0] = Utils.round(totalIdleTime / 168.0 * 100, 2);
        timeUsageWeek[1] = Utils.round(totalCasualTime / 168.0 * 100, 2);
        timeUsageWeek[2] = Utils.round(totalBatteryHighInter / 168.0 * 100, 2);
        timeUsageWeek[3] = Utils.round(totalBatteryHighNet / 168.0 * 100, 2);
        timeUsageWeek[4] = Utils.round(totalHighCpuTime / 168.0 * 100, 2);

        Log.d("[Savings]", "Battery consumed: " + powerUsageWeek[0] + " " + powerUsageWeek[1] + " " + powerUsageWeek[2] + " " + powerUsageWeek[3] + " " + powerUsageWeek[4]);
        //battery = 0;

        Log.d("[Savings]", "Percentage in each state: " + totalIdleTime / 168.0 + " " + totalCasualTime / 168.0 + " " + totalBatteryHighInter / 168.0 + " " + totalBatteryHighNet / 168.0 + " " + totalHighCpuTime / 168.0);
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
