package application.com.batterysaver;

/**
 * Created by gr33k1718 on 09/02/2016.
 */
public class SystemContext {

    public int period;
    public int brightness;
    public int batteryLevel;
    public int timeOut;
    public long networkTraffic;
    public long mobileTraffic;
    public long interactionTime;
    public double cpuLoad;
    public int day;
    public int charging;

    public SystemContext(int day, int period, int charging, int brightness, int batteryLevel, int timeOut, long networkTraffic,
                         long mobileTraffic, long interactionTime, double cpuLoad) {
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
    }
}
