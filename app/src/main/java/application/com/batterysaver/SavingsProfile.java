package application.com.batterysaver;

public class SavingsProfile {
    private int day;
    private int startTime;
    private int endtime;
    private int brightness;
    private long timeout;
    private long networkWarningLimit;
    private boolean setCpuMonitor = false;
    private boolean setNetworkMonitor = false;
    private UsageProfile u;
    private long[] timeoutValue = new long[]{15000, 30000, 60000, 120000, 300000, 600000};

    public SavingsProfile(UsageProfile u){
        this.u = u;
    }

    public SavingsProfile generate(){
        long interactionTime = u.getInteractionTime();
        int brightness = u.getBrightness();
        long timeout = u.getTimeout();
        this.day = u.getDay();
        this.startTime = u.getStart();
        this.endtime = u.getEnd();
        this.timeout = 15000;

        if(u.isIdle()){
            this.brightness = 30;
        }
        else{
            if(brightness > 168){
                this.brightness = 168;
            }
            else{
                this.brightness = brightness;
            }
        }
        if(u.isHighInteraction()){
            if(interactionTime < 2700000){
                if(brightness > 110){
                    this.brightness = (int)(brightness * 0.7);
                }
                else{
                    this.brightness = brightness;
                }
                if(timeout < 45000){
                    this.timeout = 15000;
                }
                else if(timeout < 75000){
                    this.timeout = 30000;
                }
                else if(timeout < 210000){
                    this.timeout = 60000;
                }
                else if(timeout < 450000){
                    this.timeout = 120000;
                }
                else{
                    this.timeout = 300000;
                }
            }
        }
        if(u.isHighCPU()){
            this.setCpuMonitor = true;
        }
        if(u.isHighNetwork()){
            this.setNetworkMonitor = true;
            this.networkWarningLimit = (long)(u.getNetworkUsage() * 0.75);
        }

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

    public long[] getTimeoutValue() {
        return timeoutValue;
    }

    @Override
    public String toString() {
        return "SavingsProfile{" +
                "day=" + day +
                ", startTime=" + startTime +
                ", brightness=" + brightness +
                ", timeout=" + timeout +
                ", networkWarningLimit=" + networkWarningLimit +
                ", setCpuMonitor=" + setCpuMonitor +
                ", setNetworkMonitor=" + setNetworkMonitor +
                '}';
    }
}
