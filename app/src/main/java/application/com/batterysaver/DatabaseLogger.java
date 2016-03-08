package application.com.batterysaver;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;


public class DatabaseLogger {
    private static final String DATABASE_NAME = "logs.db";
    private static final int DATABASE_VERSION = 4;
    private static final String LOG_TABLE_NAME = "logs";
    private static final String KEY_ID = "_id";
    private final SQLOpenHelper mSQLOpenHelper;
    PreferencesUtil prefs = PreferencesUtil.getInstance(GlobalVars.getAppContext(), Constants.SYSTEM_CONTEXT_PREFS, Context.MODE_PRIVATE);
    private SQLiteDatabase rdb;
    private SQLiteDatabase wdb;
    private int count;

    public DatabaseLogger(Context context) {
        mSQLOpenHelper = new SQLOpenHelper(context);

        openDBs();
    }

    /* My cursor adapter was getting a bit complicated since it could only see one datum at a time, and
       how I want to present the data depends on several interrelated factors.  Storing all three of
       these items together simplifies things. */
    private static int encodeStatus(int status, int plugged, int status_age) {
        return status + (plugged * 10) + (status_age * 100);
    }

    /* Returns [status, plugged, status_age] */
    public static int[] decodeStatus(int statusCode) {
        int[] a = new int[3];

        a[2] = statusCode / 100;
        statusCode -= a[2] * 100;
        a[1] = statusCode / 10;
        statusCode -= a[1] * 10;
        a[0] = statusCode;

        return a;
    }

    private void openDBs() {
        if (rdb == null || !rdb.isOpen()) {
            try {
                rdb = mSQLOpenHelper.getReadableDatabase();
            } catch (SQLiteException e) {
                rdb = null;
            }
        }

        if (wdb == null || !wdb.isOpen()) {
            try {
                wdb = mSQLOpenHelper.getWritableDatabase();
            } catch (SQLiteException e) {
                rdb = null;
            }
        }
    }

    public void close() {
        if (rdb != null)
            rdb.close();
        if (wdb != null)
            wdb.close();
    }

    public List<String> getAllLogs(int day) {
        int count = 0;
        long totalScreen = 0;
        long totalWifi = 0;
        long totalMobile = 0;
        List<String> logs = new LinkedList<>();
        openDBs();

        Cursor cursor = wdb.rawQuery("SELECT * FROM " + LOG_TABLE_NAME + " WHERE " + Constants.DAY_PREF + " = " + day, null);

        String logItem;
        if (cursor.moveToFirst()) {
            do {
                logItem = cursor.getString(0) + " "
                        + cursor.getString(1) + " "
                        + cursor.getString(2) + " "
                        + cursor.getString(3) + " "
                        + cursor.getString(4) + " "
                        + cursor.getString(5) + " "
                        + cursor.getString(6) + " "
                        + cursor.getString(7) + " "
                        + cursor.getString(8) + " "
                        + cursor.getString(9) + " "
                        + cursor.getString(10);
                count++;
                //Log.d("[data]",cursor.getLong(10) + " " + cursor.getString(10));
                totalMobile += cursor.getLong(10);
                totalScreen += cursor.getLong(7);
                totalWifi += cursor.getLong(9);
                Log.d("[data]", totalMobile + " " + cursor.getLong(10));
                logs.add(logItem);
            } while (cursor.moveToNext());
        }

        cursor.close();

        //editor.putLong(Constants.AVG_WIFI, totalWifi / count);
        //editor.putLong(Constants.AVG_MOBILE, totalMobile/count);
        //editor.putLong(Constants.AVG_SCREEN, totalScreen / count);
        //editor.commit();

        return logs;
    }

    public UsageProfile[][] getUsagePatterns() {
        UsageProfile prevUsage = null;
        int totalBrightness = 0;
        long totalTimeout = 0;
        long totalInteraction = 0;
        long totalNetwork = 0;
        float totalCPU = 0;
        int totalBatteryUsed;

        final long MAX_IDLE = 120000;
        final long MIN_HIGH_INTERACTION = 1800000;
        final long MIN_HIGH_NETWORK = 1000000;
        final float MIN_HIGH_CPU = 35;

        UsageProfile[][] group = new UsageProfile[8][24];

        openDBs();

        Cursor cursor = wdb.rawQuery("SELECT * FROM " + LOG_TABLE_NAME, null);

        if (cursor.moveToFirst()) {
            do {
                int day = cursor.getInt(1) - 1;
                int period = cursor.getInt(2);
                int battery = cursor.getInt(3);
                long screen = cursor.getLong(7);
                long networkTraffic = cursor.getLong(9);
                float cpuLoad = cursor.getFloat(8);
                int brightness = cursor.getInt(5);
                long timeout = cursor.getLong(6);

                UsageProfile usageProfile = new UsageProfile();
                usageProfile.setDay(day);
                usageProfile.setStart(period);
                usageProfile.setBatteryLevel(battery);

                //Determine usage type/s
                if (screen < MAX_IDLE) {
                    usageProfile.setIdle(true);

                } else if (screen > MIN_HIGH_INTERACTION) {
                    usageProfile.setHighInteraction(true);
                }

                if (networkTraffic > MIN_HIGH_NETWORK) {
                    usageProfile.setHighNetwork(true);
                }

                if (cpuLoad > MIN_HIGH_CPU) {
                    usageProfile.setHighCPU(true);
                }

                //Group similar profiles together and get sum/averages of stats
                if (usageProfile.equals(prevUsage)) {
                    count++;
                    totalInteraction += screen;
                    totalNetwork += (networkTraffic + cursor.getLong(10));
                    totalBrightness += brightness;
                    totalTimeout += timeout;
                    totalCPU += cpuLoad;


                } else {
                    //Fix for under and overflow for periods
                    int startDefault = period + 1;
                    int endDefault = period - count;
                    usageProfile.setEnd(startDefault == 24 ? 0 : startDefault);
                    usageProfile.setStart(endDefault < 0 ? 24 - count : endDefault);

                    if (prevUsage != null) {

                        if (count > 0) {
                            usageProfile.setBrightness(totalBrightness / count);
                            usageProfile.setTimeout(totalTimeout / count);
                            usageProfile.setCpu(totalCPU / count);
                            usageProfile.setInteractionTime(totalInteraction);
                            usageProfile.setNetworkUsage(totalNetwork);


                        } else {
                            usageProfile.setBrightness(brightness);
                            usageProfile.setTimeout(timeout);
                            usageProfile.setCpu(cpuLoad);
                            usageProfile.setInteractionTime(screen);
                            usageProfile.setNetworkUsage(networkTraffic);

                        }

                        //Determine battery usage for each profile. Default to 0 if charging
                        totalBatteryUsed = prevUsage.getBatteryLevel() - usageProfile.getBatteryLevel();
                        usageProfile.setBatteryUsed(totalBatteryUsed > -1 ? totalBatteryUsed : 0);
                        //Log.d("[hi]", "Prev " + prevUsage);
                        Log.d("[hi]", "Current " + usageProfile);
                    }

                    //Reset totals
                    totalInteraction = 0;
                    totalNetwork = 0;
                    totalBrightness = 0;
                    totalTimeout = 0;
                    totalCPU = 0;
                    count = 0;
                    prevUsage = usageProfile;

                }

                group[day][period] = usageProfile;

            } while (cursor.moveToNext());
        }

        cursor.close();

        prefs.putUsageProfiles(group);
        prefs.commit();

        return group;
    }

    public void logStatus(SystemContext info) {

        openDBs();

        try {
            wdb.execSQL("INSERT INTO " + LOG_TABLE_NAME + " VALUES (NULL, "
                    + info.day + " ,"
                    + info.period + " ,"
                    + info.batteryLevel + " ,"
                    + info.charging + " ,"
                    + info.brightness + " ,"
                    + info.timeOut + " ,"
                    + info.interactionTime + " ,"
                    + info.cpuLoad + " ,"
                    + info.networkTraffic + " ,"
                    + info.mobileTraffic
                    + ")");
            Log.d("[Database]", "Success " + info.period);
            close();
        } catch (Exception e) {
            Log.d("[Database] " + info.period, e.toString());
        }
    }

    public void deleteLog(String id) {

        openDBs();
        //String item = Integer.parseInt(id);
        try {
            wdb.execSQL("DELETE FROM " + LOG_TABLE_NAME + " WHERE " + KEY_ID + " = " + id);
        } catch (Exception e) {
            // Maybe storage is full?  Okay to just return rather than crash.
        }
    }

    public void clearAllLogs() {
        mSQLOpenHelper.reset();
    }

    private static class SQLOpenHelper extends SQLiteOpenHelper {
        public SQLOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + LOG_TABLE_NAME + " ("
                    + KEY_ID + " INTEGER PRIMARY KEY,"
                    + Constants.DAY_PREF + " INTEGER, "
                    + Constants.PERIOD_PREF + " INTEGER,"
                    + Constants.BATTERY_LEVEL_PREF + " INTEGER,"
                    + Constants.IS_CHARGING_PREF + " INTEGER,"
                    + Constants.BRIGHTNESS_PREF + " INTEGER,"
                    + Constants.TIMEOUT_PREF + " INTEGER,"
                    + Constants.INTERACTION_TIME_PREF + " INTEGER,"
                    + Constants.CPU_LOAD_PREF + " REAL,"
                    + Constants.NETWORK_TRAFFIC_PREF + " INTEGER,"
                    + Constants.MOBILE_TRAFFIC_PREF + " INTEGER"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + LOG_TABLE_NAME);
            onCreate(db);

        }

        public void reset() {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("DROP TABLE IF EXISTS " + LOG_TABLE_NAME);
            onCreate(db);
        }
    }
}
