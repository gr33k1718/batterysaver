package application.com.batterysaver;

public class UsageProfile {

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
    private int charging;
    private String usageType;
    private int networkUsageTime;


    public UsageProfile(int day, int start, int end, int charging, int brightness, long timeout,
                        long networkUsage, int networkUsageTIme, long interactionTime,
                        float cpu, String usageType) {
        this.usageType = usageType;

        this.day = day;
        this.start = start;
        this.end = end;

        this.interactionTime = interactionTime;
        this.brightness = brightness;
        this.timeout = timeout;

        this.cpu = cpu;

        this.networkUsage = networkUsage;
        this.networkUsageTime = networkUsageTIme;

        this.charging = charging;

    }

    public int getNetworkUsageTime() {
        return networkUsageTime;
    }

    public float getCpu() {
        return cpu;
    }

    public long getInteractionTime() {
        return interactionTime;
    }


    public int getBatteryLevel() {
        return batteryLevel;
    }

    public String getUsageType() {
        return usageType;
    }

    public long getNetworkUsage() {
        return networkUsage;
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

    public int getEnd() {
        return end;
    }

    public void setBatteryUsed(int batteryUsed) {
        this.batteryUsed = batteryUsed;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UsageProfile that = (UsageProfile) o;

        if (getDay() != that.getDay()) return false;
        return !(getUsageType() != null ? !getUsageType().equals(that.getUsageType())
                : that.getUsageType() != null);

    }

    public UsageProfile merge(UsageProfile prev) {
        prev.networkUsage += this.networkUsage;
        prev.networkUsageTime += this.networkUsageTime;
        prev.interactionTime += this.interactionTime;
        prev.cpu += this.cpu;
        prev.brightness += this.brightness;
        prev.timeout += this.timeout;
        prev.setStart(prev.getStart());
        prev.batteryLevel = this.batteryLevel;
        prev.end = this.end;
        prev.day = this.day;

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
                ", networkUsageTime=" + networkUsage +
                ", interactionTime=" + networkUsageTime +
                ", cpu=" + cpu +
                ", brightness=" + brightness +
                ", timeout=" + timeout +
                '}';
    }
}
