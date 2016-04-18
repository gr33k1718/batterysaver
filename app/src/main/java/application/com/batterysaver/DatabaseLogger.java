package application.com.batterysaver;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

/**
 * The DatabaseLogger is responsible for all database related activity.
 */

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
     * Creates the users usages patterns and resets data in the current table
     * copying the relevant rows over to the backup table
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
     * Retrieves all data from table one within the database
     *
     * @param day the week day
     * @return list of all data within table one
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
     * Assigns a usage type to the value provided
     *
     * @param usageScore the value to be categorised
     * @return the classification given
     */
    private String usageType(double usageScore) {

        if (usageScore <= 1) {
            return "minimal";
        } else if (usageScore <= 3) {
            return "low";
        } else if (usageScore <= 6) {
            return "medium";
        } else {
            return "high";
        }
    }

    /**
     * Analyses the data stored and creates usage patterns over the time periods.
     * Stores data within shared preferences
     *
     * @return the collection of usage patterns over the week
     */
    public UsageProfile[][] getUsagePatterns() {
        UsageProfile prevUsage = null;
        UsageProfile currentUsage;

        UsageProfile[][] weeklyUsage = new UsageProfile[7][24];

        openDBs();

        Cursor cursor = wdb.rawQuery("SELECT * FROM " + Constants.LOG_TABLE_NAME_ONE, null);

        if (cursor.moveToFirst()) {
            do {
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

                if (prevUsage != null) {

                    if (currentUsage.equals(prevUsage)) {

                        prevUsage = currentUsage.merge(prevUsage);

                    } else {
                        weeklyUsage[prevUsage.getDay() - 1][prevUsage.getStart()] = prevUsage;
                        prevUsage = currentUsage;
                    }
                } else {
                    prevUsage = currentUsage;
                }

            } while (cursor.moveToNext());
        }

        cursor.close();

        prefs.putUsageProfiles(weeklyUsage);
        prefs.commit();

        return weeklyUsage;
    }


    /**
     * This weights the user data such that past data is considered more
     * relevant than current data
     *
     * @param prev    previous data point
     * @param current current data point
     * @param weight  the weighting value
     * @return the weighted data
     */
    private long getWeightedAverage(long prev, long current, float weight) {
        double weightPrev = 1 + (weight / 100);
        double weightCurrent = 1 - (weight / 100);

        return (long) ((prev * weightPrev) + (current * weightCurrent)) / 2;
    }

    /**
     * Weights the current data with the previous data and inserts the result into the
     * database.  Also assigns a usage type based on values provided.
     *
     * @param info the users data
     */
    public void logStatus(LogData info) {
        int period = info.period;
        int day = info.day;
        long prevScreen = 0;
        long prevNetworkTraffic = 0;
        long prevMobileTraffic = 0;
        long prevNetworkUsage = 0;
        long prevMobileUsage = 0;
        int prevCpuLoad = 0;

        openDBs();
        Cursor prevCursor = wdb.rawQuery("SELECT * FROM " + Constants.LOG_TABLE_NAME_TWO +
                " WHERE " + Constants.DAY_PREF + " = " + day + " AND "
                + Constants.PERIOD_PREF + " = " + period, null);
        if (prevCursor != null && prevCursor.getCount() > 0) {

            prevCursor.moveToFirst();
            prevScreen = prevCursor.getLong(4);
            prevNetworkTraffic = prevCursor.getLong(6);
            prevMobileTraffic = prevCursor.getLong(7);
            prevCpuLoad = prevCursor.getInt(5);
            prevNetworkUsage = prevCursor.getInt(8);
            prevMobileUsage = prevCursor.getInt(9);

        }

        long interactionTime = getWeightedAverage(prevScreen, info.interactionTime, 30);
        long cpuLoad = getWeightedAverage(prevCpuLoad, (int) info.cpuLoad, 30);
        long networkTraffic = getWeightedAverage(prevNetworkTraffic, info.networkTraffic, 30);
        long mobileTraffic = getWeightedAverage(prevMobileTraffic, info.mobileTraffic, 30);
        long networkUsage = getWeightedAverage(prevNetworkUsage, info.networkUsageTime, 30);
        long mobileUsage = getWeightedAverage(prevMobileUsage, info.mobileUsageTime, 30);

        double usageScore = Predictor.predictBatteryUsage(networkTraffic,
                mobileTraffic, (int) cpuLoad,
                interactionTime, info.brightness,
                networkUsage, mobileUsage);

        String usageType = usageType(usageScore);

        try {

            wdb.execSQL("INSERT INTO " + Constants.LOG_TABLE_NAME_ONE + " VALUES (NULL, "
                    + info.day + " ,"
                    + info.period + " ,"
                    + info.batteryLevel + " ,"
                    + info.charging + " ,"
                    + info.brightness + " ,"
                    + info.timeOut + " ,"
                    + interactionTime + " ,"
                    + cpuLoad + " ,"
                    + networkTraffic + " ,"
                    + mobileTraffic + " ,"
                    + networkUsage + " ,"
                    + mobileUsage + " ,"
                    + usageType
                    + ")");

            close();
        } catch (Exception e) {
            Log.e("[Error]", e.toString());
        }
    }

    /**
     * Removes a row for the database
     *
     * @param id the id of the row
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
     * Clear all data in the given table
     *
     * @param table the name of the table
     */
    public void clearAllLogs(String table) {
        mSQLOpenHelper.reset(table);
    }

    /**
     * This class is used to create the database object which handles all database related calls.
     */
    private static class SQLOpenHelper extends SQLiteOpenHelper {
        public SQLOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        /**
         * Table creation statement for the main database table
         *
         * @return the SQL create table statement
         */
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

        /**
         * Database creation statement for the backup table
         *
         * @return the SQL create table statement
         */
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
         * Creates the tables for the given database for the given database
         *
         * @param db the database
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(createTable());
            db.execSQL(createBackupTable());
        }

        /**
         * @param db         the database
         * @param oldVersion the old database version
         * @param newVersion the new database version
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(createTable());
            db.execSQL(createBackupTable());
        }

        /**
         * Remakes the the database table
         *
         * @param table the name of the table
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
