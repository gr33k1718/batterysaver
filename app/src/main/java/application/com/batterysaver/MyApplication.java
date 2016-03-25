package application.com.batterysaver;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;

public class MyApplication extends Application {

    private static Context context;

    public static Context getAppContext() {
        return MyApplication.context;
    }

    public static ContentResolver getContentRes() {
        return context.getContentResolver();
    }

    public void onCreate() {
        super.onCreate();
        MyApplication.context = getApplicationContext();
    }

}
