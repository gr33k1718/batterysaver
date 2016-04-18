package application.com.batterysaver;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import com.google.gson.Gson;

/**
 * Helper class for saving data into shared preferences
 */
public class PreferencesUtil {

    private static PreferencesUtil sInstance;
    private SharedPreferences prefs;
    private SharedPreferences.Editor prefsEditor;
    private Context mAppContext;
    private boolean mUseApply;

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

    public int getInt(String key, int defValue) {
        return prefs.getInt(key, defValue);
    }

    public long getLong(String key, long defValue) {
        return prefs.getLong(key, defValue);
    }

    public UsageProfile[][] getUsageProfiles() {
        String jsonUsageProfiles = prefs.getString("USAGE_PROFILES", null);
        Gson gson = new Gson();
        UsageProfile[][] usageProfiles = gson.fromJson(jsonUsageProfiles,
                UsageProfile[][].class);

        return usageProfiles;
    }

    public void putInt(String key, int value) {
        prefsEditor.putInt(key, value);
    }

    public void putLong(String key, long value) {
        prefsEditor.putLong(key, value);
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
            prefsEditor.apply();

        else
            prefsEditor.commit();
    }
}
