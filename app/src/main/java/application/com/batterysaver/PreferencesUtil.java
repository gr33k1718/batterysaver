package application.com.batterysaver;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import com.google.gson.Gson;

/**
 * Created by gr33k1718 on 20/02/2016.
 */
public class PreferencesUtil {

    private static PreferencesUtil sInstance;
    SharedPreferences prefs;
    SharedPreferences.Editor prefsEditor;
    private Context mAppContext;
    private boolean mUseApply;

    //Set to private
    private PreferencesUtil(Context context, String pref, int type) {
        mAppContext = context.getApplicationContext();
        prefs = mAppContext.getSharedPreferences(pref, type);
        prefsEditor = prefs.edit();

        mUseApply = Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    public static PreferencesUtil getInstance(Context context, String pref, int type) {
        if (sInstance == null) {
            sInstance = new PreferencesUtil(context, pref, type);
        }
        return sInstance;
    }

    public boolean getBoolean(String key, boolean defValue) {
        return prefs.getBoolean(key, defValue);
    }

    public int getInt(String key, int defValue) {
        return prefs.getInt(key, defValue);
    }

    public long getLong(String key, long defValue) {
        return prefs.getLong(key, defValue);
    }

    public String getString(String key, String defValue) {
        return prefs.getString(key, defValue);
    }

    public String getString(String key) {
        return prefs.getString(key, "");
    }

    public UsageProfile[][] getUsageProfiles() {
        String jsonUsageProfiles = prefs.getString("USAGE_PROFILES", null);
        Gson gson = new Gson();
        UsageProfile[][] usageProfiles = gson.fromJson(jsonUsageProfiles,
                UsageProfile[][].class);

        return usageProfiles;
    }

    public void putBoolean(String key, boolean value) {
        prefsEditor.putBoolean(key, value);
    }

    public void putInt(String key, int value) {
        prefsEditor.putInt(key, value);
    }

    public void putLong(String key, long value) {
        prefsEditor.putLong(key, value);
    }

    public void putString(String key, String value) {
        prefsEditor.putString(key, value);
    }

    public void putUsageProfiles(UsageProfile[][] usageProfiles) {
        Gson gson = new Gson();
        String usageProfilesFlat = gson.toJson(usageProfiles);

        prefsEditor.putString("USAGE_PROFILES", usageProfilesFlat);
    }

    public void remove(String key) {
        prefsEditor.remove(key);
    }

    public void commit() {
        if (mUseApply)
            //Since API Level 9, apply() is provided for asynchronous operations
            prefsEditor.apply();
        else
            //Fallback to syncrhonous if not available
            prefsEditor.commit();
    }
}
