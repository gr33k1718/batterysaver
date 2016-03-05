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

    public UsageProfile(){}

    public UsageProfile(int brightness, long timeout) {
        this.brightness = brightness;
        this.timeout = timeout;
    }

    public boolean isHighInteraction() {
        return highInteraction;
    }

    public void setHighInteraction(boolean highInteraction) {
        this.highInteraction = highInteraction;
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

    public void setMinimumProfile(){
        this.brightness = 30;
        this.timeout = 15000;
        Settings.System.putInt(GlobalVars.getContentRes(),
                Settings.System.HAPTIC_FEEDBACK_ENABLED,0);
        ContentResolver.setMasterSyncAutomatically(false);
    }

    @Override
    public String toString() {
        return "UsageProfile{" +
                "idle = " + idle +
                ", brightness = " + brightness +
                ", timeout = " + timeout +
                ", highNetwork = " + highNetwork +
                ", highCPU = " + highCPU +
                ", highInteraction = " + highInteraction +
                '}';
    }

}
