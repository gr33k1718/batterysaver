package application.com.batterysaver;

/**
 * Created by gr33k1718 on 09/02/2016.
 */
public class LogData {

    public int period;
    public int brightness;
    public int batteryLevel;
    public int predictedBatteryLevel;
    public int timeOut;
    public long networkTraffic;
    public long mobileTraffic;
    public long interactionTime;
    public float cpuLoad;
    public int day;
    public int charging;
    public String usageType;
    public int networkUsageTime;
    public int mobileUsageTime;

    public LogData(int day, int period, int charging, int brightness, int batteryLevel,
                   int timeOut, long networkTraffic, long mobileTraffic, long interactionTime,
                   float cpuLoad, int networkUsageTime, int mobileUsageTime, String usageType) {
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
        this.usageType = usageType;
        this.networkUsageTime = networkUsageTime;
        this.mobileUsageTime = mobileUsageTime;
    }
}
