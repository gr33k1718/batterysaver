package application.com.batterysaver;


/**
 * The UsageProfile class is responsible for the creation of usage profiles
 */
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

    /**
     *
     * @param day the day of the week
     * @param start the start time of the profile
     * @param end the end time of the profile
     * @param charging whether the device was charging
     * @param brightness the screen brightness
     * @param timeout the screen timeout
     * @param networkUsage the Wi-Fi traffic
     * @param networkUsageTIme the Wi-Fi user interaction time
     * @param interactionTime the screen interaction time
     * @param cpu the CPU load
     * @param usageType the type of usage
     */
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

    /**
     * Gets the Wi-Fi interaction time
     * @return the Wi-Fi interaction time
     */
    public int getNetworkUsageTime() {
        return networkUsageTime;
    }

    /**
     * Gets the CPU load
     * @return the CPU load
     */
    public float getCpu() {
        return cpu;
    }

    /**
     * Gets the interaction time
     * @return the interaction time
     */
    public long getInteractionTime() {
        return interactionTime;
    }

    /**
     * Get the usage type
     * @return the usage type
     */
    public String getUsageType() {
        return usageType;
    }

    /**
     * Gets the Wi-Fi traffic
     * @return the Wi-Fi traffic
     */
    public long getNetworkUsage() {
        return networkUsage;
    }

    /**
     * Gets the start time
     * @return the start time
     */
    public int getStart() {
        return start;
    }

    /**
     * Sets the start time
     * @param start the start time
     */
    public void setStart(int start) {
        this.start = start;
    }

    /**
     * Gets the day of the week
     * @return the day of the week
     */
    public int getDay() {
        return day;
    }

    /**
     * Gets the end time
     * @return the end time
     */
    public int getEnd() {
        return end;
    }

    /**
     * Checks for equality between usage profiles
     * @param o the usage profile
     * @return whether they were equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UsageProfile that = (UsageProfile) o;

        if (getDay() != that.getDay()) return false;
        return !(getUsageType() != null ? !getUsageType().equals(that.getUsageType())
                : that.getUsageType() != null);

    }

    /**
     * Merges two usage profiles such that the form a bigger one.
     * @param prev the previous usage profile
     * @return the merged usage profile
     */
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
}
