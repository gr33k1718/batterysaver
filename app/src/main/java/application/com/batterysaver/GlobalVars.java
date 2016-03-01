package application.com.batterysaver;

import android.app.Application;
import android.content.Context;

public class GlobalVars extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        GlobalVars.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return GlobalVars.context;
    }

}
