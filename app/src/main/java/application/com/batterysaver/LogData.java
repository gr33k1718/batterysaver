package application.com.batterysaver;

/**
 * Helper class for related user data
 */
public class LogData {

    public int period;
    public int brightness;
    public int batteryLevel;
    public int timeOut;
    public long networkTraffic;
    public long mobileTraffic;
    public long interactionTime;
    public float cpuLoad;
    public int day;
    public int charging;
    public int networkUsageTime;
    public int mobileUsageTime;

    /**
     * Handles the creation of the log data inserted into the database
     *
     * @param day              the day of the week
     * @param period           the time period
     * @param charging         the charging value
     * @param brightness       the screen brightness
     * @param batteryLevel     the battery level
     * @param timeOut          the screen timeout
     * @param networkTraffic   the Wi-Fi traffic
     * @param mobileTraffic    the mobile data traffic
     * @param interactionTime  the screen interaction time
     * @param cpuLoad          the CPU load
     * @param networkUsageTime the Wi-Fi user interaction time
     * @param mobileUsageTime  the mobile network user interaction time
     */
    public LogData(int day, int period, int charging, int brightness, int batteryLevel,
                   int timeOut, long networkTraffic, long mobileTraffic, long interactionTime,
                   float cpuLoad, int networkUsageTime, int mobileUsageTime) {
        this.day = day;
        this.period = period;
        this.brightness = brightness;
        this.batteryLevel = batteryLevel;
        this.timeOut = timeOut;
        this.networkTraffic = networkTraffic;
        this.mobileTraffic = mobileTraffic;
        this.interactionTime = interactionTime;
        this.cpuLoad = cpuLoad;
        this.charging = charging;
        this.networkUsageTime = networkUsageTime;
        this.mobileUsageTime = mobileUsageTime;
    }
}
