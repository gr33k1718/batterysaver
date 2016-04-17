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
    private static final int DATABASE_VERSION = 5;
    private static final String KEY_ID = "_id";

    private final SQLOpenHelper mSQLOpenHelper;
    private SQLiteDatabase rdb;
    private SQLiteDatabase wdb;

    private PreferencesUtil prefs = PreferencesUtil.getInstance(MyApplication.getAppContext(),
            Constants.SYSTEM_CONTEXT_PREFS, Context.MODE_PRIVATE);

    public DatabaseLogger(Context context) {
        mSQLOpenHelper = new SQLOpenHelper(context);

        openDBs();
    }

    /**
     * Opens both readable and writable databases connections.
     */
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

    /**
     * Closes the database connections.
     */
    public void close() {
        if (rdb != null)
            rdb.close();
        if (wdb != null)
            wdb.close();
    }

    /**
     *
     */
    public void copyAndCreate() {

        openDBs();

        try {
            getUsagePatterns();
            wdb.execSQL("DROP TABLE IF EXISTS " + Constants.LOG_TABLE_NAME_TWO);
            wdb.execSQL("CREATE TABLE " + Constants.LOG_TABLE_NAME_TWO + " as SELECT  "
                    + Constants.DAY_PREF + " ,"
                    + Constants.PERIOD_PREF + " ,"
                    + Constants.INTERACTION_TIME_PREF + " ,"
                    + Constants.CPU_LOAD_PREF_FLOAT + " ,"
                    + Constants.NETWORK_TRAFFIC_PREF + " ,"
                    + Constants.MOBILE_TRAFFIC_PREF + " ,"
                    + Constants.NETWORK_USAGE_PREF + " ,"
                    + Constants.MOBILE_USAGE_PREF + " FROM " + Constants.LOG_TABLE_NAME_ONE);

            close();

            clearAllLogs(Constants.LOG_TABLE_NAME_ONE);

        } catch (Exception e) {

        }
    }

    /**
     * @param day
     * @return
     */
    public List<String> getAllLogs(int day) {
        List<String> logs = new LinkedList<>();
        openDBs();

        Cursor cursor = wdb.rawQuery("SELECT * FROM " + Constants.LOG_TABLE_NAME_ONE + " WHERE "
                + Constants.DAY_PREF + " = " + day, null);

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
                        + cursor.getString(10) + " "
                        + cursor.getString(11) + " "
                        + cursor.getString(12) + " "
                        + cursor.getString(13);
                logs.add(logItem);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return logs;
    }

    /**
     * @return
     */
    public UsageProfile[][] getUsagePatterns() {
        Log.d("[hi]", "profiles");

        UsageProfile prevUsage = null;
        UsageProfile currentUsage;

        int totalBatteryUsed = 0;

        boolean first = true;

        UsageProfile[][] group = new UsageProfile[8][24];

        openDBs();

        Cursor cursor = wdb.rawQuery("SELECT * FROM " + Constants.LOG_TABLE_NAME_ONE, null);

        if (cursor.moveToFirst()) {
            do {
                int singlePeriod;
                int multiPeriod;
                int day = cursor.getInt(1);
                int period = cursor.getInt(2);
                int charging = cursor.getInt(4);
                int brightness = cursor.getInt(5);
                long timeout = cursor.getLong(6);
                long screen = cursor.getLong(7);
                int cpuLoad = cursor.getInt(8);
                long totalTraffic = cursor.getLong(9) + cursor.getLong(10);
                int totalNetUsageTime = cursor.getInt(13) + cursor.getInt(14);
                String usageType = cursor.getString(12);

                currentUsage = new UsageProfile(day, period, period + 1 != 24 ? period + 1 : 0,
                        charging, brightness, timeout, totalTraffic, totalNetUsageTime,
                        screen, cpuLoad, usageType);

                //Determine usage type/s

                if (prevUsage != null) {

                    if (currentUsage.equals(prevUsage)) {
                        if (first) {
                            totalBatteryUsed = prevUsage.getBatteryLevel();
                            first = false;
                        }
                        prevUsage = currentUsage.merge(prevUsage);

                    } else {
                        //If phone was charging over the period return 0
                        if (prevUsage.getEnd() - prevUsage.getStart() > 1) {
                            multiPeriod = totalBatteryUsed - currentUsage.getBatteryLevel();
                            prevUsage.setBatteryUsed(multiPeriod > 0 ? multiPeriod : 0);
                        } else {
                            singlePeriod = prevUsage.getBatteryLevel() - currentUsage.getBatteryLevel();
                            prevUsage.setBatteryUsed(singlePeriod > 0 ? singlePeriod : 0);
                        }

                        Log.d("[hi]", "\n\nPrev " + prevUsage + " " + totalBatteryUsed);
                        group[prevUsage.getDay()][prevUsage.getStart()] = prevUsage;
                        prevUsage = currentUsage;
                        first = true;
                    }
                } else {
                    prevUsage = currentUsage;
                }

            } while (cursor.moveToNext());
        }

        cursor.close();

        prefs.putUsageProfiles(group);
        prefs.commit();

        return group;
    }

    public void alter() {
        openDBs();
        String DATABASE_ALTER_TEAM_TO_V3 = "ALTER TABLE "
                + Constants.LOG_TABLE_NAME_ONE + " ADD COLUMN " + Constants.MOBILE_USAGE_PREF + " TEXT;";
        String DATABASE_ALTER_TEAM_TO_V4 = "ALTER TABLE "
                + Constants.LOG_TABLE_NAME_ONE + " ADD COLUMN " + Constants.NETWORK_USAGE_PREF + " REAL;";

        wdb.execSQL(DATABASE_ALTER_TEAM_TO_V3);
        wdb.execSQL(DATABASE_ALTER_TEAM_TO_V4);

    }

    /**
     * @param prev
     * @param current
     * @return
     */
    private long getWeightedAverage(long prev, long current, float weight) {
        double weightPrev = 1 + (weight / 100);
        double weightCurrent = 1 - (weight / 100);

        return (long) ((prev * weightPrev) + (current * weightCurrent)) / 2;
    }

    /**
     * @param info
     */
    public void logStatus(LogData info) {
        int period = info.period;
        int day = info.day;
        long prevScreen = 0;
        long prevNetworkTraffic = 0;
        long prevMobileTraffic = 0;
        int prevCpuLoad = 0;

        openDBs();
        Cursor prevCursor = wdb.rawQuery("SELECT * FROM " + Constants.LOG_TABLE_NAME_TWO +
                " WHERE " + Constants.DAY_PREF + " = " + day + " AND "
                + Constants.PERIOD_PREF + " = " + period, null);
        if (prevCursor != null && prevCursor.getCount() > 0) {

            prevCursor.moveToFirst();
            prevScreen = prevCursor.getLong(7);
            prevNetworkTraffic = prevCursor.getLong(9);
            prevMobileTraffic = prevCursor.getLong(10);
            prevCpuLoad = prevCursor.getInt(8);

        }

        try {

            wdb.execSQL("INSERT INTO " + Constants.LOG_TABLE_NAME_ONE + " VALUES (NULL, "
                    + info.day + " ,"
                    + info.period + " ,"
                    + info.batteryLevel + " ,"
                    + info.charging + " ,"
                    + info.brightness + " ,"
                    + info.timeOut + " ,"
                    + getWeightedAverage(prevScreen, info.interactionTime, 30) + " ,"
                    + getWeightedAverage(prevCpuLoad, (int) info.cpuLoad, 30) + " ,"
                    + getWeightedAverage(prevNetworkTraffic, info.networkTraffic, 30) + " ,"
                    + getWeightedAverage(prevMobileTraffic, info.mobileTraffic, 30)
                    + info.usageScore + " ,"
                    + "'" + info.usageType + "' ,"
                    + info.predictedBatteryLevel
                    + ")");

            close();
        } catch (Exception e) {
            Log.e("[Error]", e.toString());
        }


    }

    /**
     * @param id
     */
    public void deleteLog(String id) {

        openDBs();

        try {
            wdb.execSQL("DELETE FROM " + Constants.LOG_TABLE_NAME_ONE + " WHERE " + KEY_ID + " = " + id);
        } catch (Exception e) {
            Log.e("[Error]", e.toString());
        }
    }

    /**
     * @param table
     */
    public void clearAllLogs(String table) {
        mSQLOpenHelper.reset(table);
    }

    private static class SQLOpenHelper extends SQLiteOpenHelper {
        public SQLOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        private String createTable() {
            return "CREATE TABLE IF NOT EXISTS " + Constants.LOG_TABLE_NAME_ONE + " ("
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
                    + Constants.NETWORK_USAGE_PREF + " INTEGER"
                    + Constants.MOBILE_USAGE_PREF + " INTEGER"
                    + Constants.USAGE_TYPE_STRING + " TEXT"
                    + ");";
        }

        private String createBackupTable() {
            return "CREATE TABLE IF NOT EXISTS " + Constants.LOG_TABLE_NAME_TWO + " ("
                    + KEY_ID + " INTEGER PRIMARY KEY,"
                    + Constants.DAY_PREF + " INTEGER, "
                    + Constants.PERIOD_PREF + " INTEGER,"
                    + Constants.INTERACTION_TIME_PREF + " INTEGER,"
                    + Constants.CPU_LOAD_PREF + " REAL,"
                    + Constants.NETWORK_TRAFFIC_PREF + " INTEGER,"
                    + Constants.MOBILE_TRAFFIC_PREF + " INTEGER"
                    + Constants.NETWORK_USAGE_PREF + " INTEGER"
                    + Constants.MOBILE_USAGE_PREF + " INTEGER"
                    + ");";
        }

        /**
         * @param db
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.e("[Error]", "Created");
            db.execSQL(createTable());
            db.execSQL(createBackupTable());
        }

        /**
         * @param db
         * @param oldVersion
         * @param newVersion
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            db.execSQL(createTable());
            db.execSQL(createBackupTable());
        }

        /**
         * @param table
         */
        public void reset(String table) {
            SQLiteDatabase db = getWritableDatabase();
            if (table.equals(Constants.LOG_TABLE_NAME_ONE)) {
                Log.e("[Error]", "Reset");
                db.execSQL("DROP TABLE IF EXISTS " + table);
                db.execSQL(createTable());
            } else {
                db.execSQL("DROP TABLE IF EXISTS " + table);
                db.execSQL(createBackupTable());
            }

        }
    }
}
