package application.com.batterysaver;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;

/**
 * Helper class to provide access to application context
 */
public class MyApplication extends Application {

    private static Context context;

    /**
     * Grabs the application context
     * @return the application context
     */
    public static Context getAppContext() {
        return MyApplication.context;
    }

    public void onCreate() {
        super.onCreate();
        MyApplication.context = getApplicationContext();
    }

}
