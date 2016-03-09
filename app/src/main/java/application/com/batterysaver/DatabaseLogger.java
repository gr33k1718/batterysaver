package application.com.batterysaver;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class DatabaseLogger {
    private static final String DATABASE_NAME = "logs.db";
    private static final int DATABASE_VERSION = 5;
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

    public void copyTable(){

        openDBs();

        try {
            wdb.execSQL("DROP TABLE IF EXISTS " + Constants.LOG_TABLE_NAME_TWO);
            wdb.execSQL("CREATE TABLE " + Constants.LOG_TABLE_NAME_TWO + " as SELECT * FROM " + Constants.LOG_TABLE_NAME_ONE);
            close();
        } catch (Exception e) {

        }
    }

    public List<String> getAllLogs(int day) {
        List<String> logs = new LinkedList<>();
        openDBs();

        Cursor cursor = wdb.rawQuery("SELECT * FROM " + Constants.LOG_TABLE_NAME_ONE + " WHERE " + Constants.DAY_PREF + " = " + day, null);

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
                logs.add(logItem);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return logs;
    }

    public UsageProfile[][] getUsagePatterns(String name) {
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

        Cursor cursor = wdb.rawQuery("SELECT * FROM " + name, null);

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
                        group[day][period] = usageProfile;
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



            } while (cursor.moveToNext());
        }

        cursor.close();

        prefs.putUsageProfiles(group);
        prefs.commit();

        return group;
    }

    public void fill(SystemContext info) {
        ArrayList<String> shit = new ArrayList<String>();

        shit.add("INSERT INTO " + Constants.LOG_TABLE_NAME_ONE + " VALUES (25, 3, 0, 100, 1, 255, 15000, 42776, 14, 330537, 0)");
        shit.add("INSERT INTO " + Constants.LOG_TABLE_NAME_ONE + " VALUES (26, 5, 1, 100, 1, 255, 15000, 0, 9, 233511, 0)");
        shit.add("INSERT INTO " + Constants.LOG_TABLE_NAME_ONE + " VALUES (27, 5, 2, 100, 1, 255, 15000, 0, 10, 91200, 0)");
        shit.add("INSERT INTO " + Constants.LOG_TABLE_NAME_ONE + " VALUES (28, 5, 3, 100, 1, 255, 15000, 0, 12, 388407, 0)");
        shit.add("INSERT INTO " + Constants.LOG_TABLE_NAME_ONE + " VALUES (29, 5, 4, 100, 1, 255, 15000, 0, 11, 146543, 0)");
        shit.add("INSERT INTO " + Constants.LOG_TABLE_NAME_ONE + " VALUES (30, 5, 5, 100, 1, 255, 15000, 0, 14, 144819, 0)");
        shit.add("INSERT INTO " + Constants.LOG_TABLE_NAME_ONE + " VALUES (31, 5, 6, 100, 1, 255, 15000, 5647, 17, 93199, 0)");
        shit.add("INSERT INTO " + Constants.LOG_TABLE_NAME_ONE + " VALUES (32, 5, 7, 100, 1, 255, 15000, 140837, 18, 147725, 0)");
        shit.add("INSERT INTO " + Constants.LOG_TABLE_NAME_ONE + " VALUES (33, 5, 8, 100, 1, 255, 15000, 0, 12, 161333, 0)");
        shit.add("INSERT INTO " + Constants.LOG_TABLE_NAME_ONE + " VALUES (35, 5, 11, 100, 1, 255, 15000, 0, 16, 341244, 0)");





        openDBs();

        try {
            for(String i : shit){
                wdb.execSQL(i);
            }

            close();
        } catch (Exception e) {}
    }

    private long getWeightedAverage(long prev, long current){
        double weightPrev = 0.75;
        double weightCurrent = 1.25;

        return prev != 0 ? (long) ((prev * weightPrev) + (current * weightCurrent)) /2 : current;
    }

    public void logStatus(SystemContext info) {

        openDBs();

        long screen = 0;
        long networkTraffic = 0;
        long mobileTraffic = 0;
        long cpuLoad = 0;
        String logItem;

        //Get previous row and determine weighted average
        try {
            Cursor cursor = wdb.rawQuery("SELECT * FROM " + Constants.LOG_TABLE_NAME_TWO +
                    " WHERE " + Constants.DAY_PREF + " = " + info.day + " AND "
                    + Constants.PERIOD_PREF + " = " + info.period,null);
            if(cursor.getCount() > 0) {

                cursor.moveToFirst();
                 screen = cursor.getLong(7);
                 networkTraffic = cursor.getLong(9);
                 mobileTraffic = cursor.getLong(10);
                 cpuLoad = cursor.getInt(8);

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

                Log.d("[data]", logItem + "\nCurrent " + info.cpuLoad);


            }
        } catch (Exception e) {
            Log.d("[data]", e.toString());
        }

        try {
            wdb.execSQL("INSERT INTO " + Constants.LOG_TABLE_NAME_ONE + " VALUES (NULL, "
                    + info.day + " ,"
                    + info.period + " ,"
                    + info.batteryLevel + " ,"
                    + info.charging + " ,"
                    + info.brightness + " ,"
                    + info.timeOut + " ,"
                    + getWeightedAverage(screen, info.interactionTime) + " ,"
                    + getWeightedAverage(cpuLoad, info.cpuLoad) + " ,"
                    + getWeightedAverage(networkTraffic, info.networkTraffic) + " ,"
                    + getWeightedAverage(mobileTraffic, info.mobileTraffic)
                    + ")");

            close();
        } catch (Exception e) {
            Log.d("[data]", e.toString());
        }


    }

    public void deleteLog(String id) {

        openDBs();

        try {
            wdb.execSQL("DELETE FROM " + Constants.LOG_TABLE_NAME_ONE + " WHERE " + KEY_ID + " = " + id);
        } catch (Exception e) {
        }
    }

    public void clearAllLogs() {
        mSQLOpenHelper.reset();
    }

    private static class SQLOpenHelper extends SQLiteOpenHelper {
        public SQLOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        private String createTable(String name){
            return "CREATE TABLE " + name + " ("
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
                    + ");";
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(createTable(Constants.LOG_TABLE_NAME_ONE));
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

                db.execSQL(createTable(Constants.LOG_TABLE_NAME_TWO));


        }

        public void reset() {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("DROP TABLE IF EXISTS " + Constants.LOG_TABLE_NAME_ONE);
            onCreate(db);
        }
    }
}
