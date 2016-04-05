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
    public void copyAndMerge() {

        openDBs();

        try {
            Cursor cursor = wdb.rawQuery("SELECT * FROM " + Constants.LOG_TABLE_NAME_ONE, null);

            if (cursor.moveToFirst()) {
                do {
                    int day = cursor.getInt(1);
                    int period = cursor.getInt(2);
                    int battery = cursor.getInt(3);
                    int charging = cursor.getInt(4);
                    int brightness = cursor.getInt(5);
                    long timeout = cursor.getLong(6);
                    long screen = cursor.getLong(7);
                    int cpuLoad = cursor.getInt(8);
                    long networkTraffic = cursor.getLong(9);
                    long mobileTraffic = cursor.getLong(10);

                    long prevScreen = 0;
                    long prevNetworkTraffic = 0;
                    long prevMobileTraffic = 0;
                    int prevCpuLoad = 0;

                    Cursor prevCursor = wdb.rawQuery("SELECT * FROM " + Constants.LOG_TABLE_NAME_TWO +
                            " WHERE " + Constants.DAY_PREF + " = " + day + " AND "
                            + Constants.PERIOD_PREF + " = " + period, null);
                    if (prevCursor != null && prevCursor.getCount() > 0) {

                        prevCursor.moveToFirst();
                        prevScreen = prevCursor.getLong(7);
                        prevNetworkTraffic = prevCursor.getLong(9);
                        prevMobileTraffic = prevCursor.getLong(10);
                        prevCpuLoad = prevCursor.getInt(8);
                        Log.e("[Error]", "" + screen + " " + networkTraffic);

                    }
                    wdb.execSQL("INSERT INTO " + Constants.LOG_TABLE_NAME_TWO + " VALUES (NULL, "
                            + day + " ,"
                            + period + " ,"
                            + battery + " ,"
                            + charging + " ,"
                            + brightness + " ,"
                            + timeout + " ,"
                            + getWeightedAverage(prevScreen, screen) + " ,"
                            + getWeightedAverage(prevCpuLoad, cpuLoad) + " ,"
                            + getWeightedAverage(prevNetworkTraffic, networkTraffic) + " ,"
                            + getWeightedAverage(prevMobileTraffic, mobileTraffic)
                            + ")");
                } while (cursor.moveToNext());
            }

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

    /**
     * @return
     */
    public UsageProfile[][] getUsagePatterns() {
        Log.d("[hi]", "profiles");

        UsageProfile prevUsage = null;
        UsageProfile currentUsage;

        int totalBatteryUsed = 0;

        boolean multi = false;
        boolean first = true;


        final long MAX_IDLE = 60000;
        final long MIN_HIGH_INTERACTION = 1800000;
        final long MIN_HIGH_NETWORK = 4000000;
        final float MIN_HIGH_CPU = 34;

        UsageProfile[][] group = new UsageProfile[7][24];

        openDBs();

        Cursor cursor = wdb.rawQuery("SELECT * FROM " + Constants.LOG_TABLE_NAME_TWO, null);

        if (cursor.moveToFirst()) {
            do {
                int singlePeriod = 0;
                int multiPeriod = 0;
                int batteryUsage = 0;
                int day = cursor.getInt(1);
                int period = cursor.getInt(2);
                int battery = cursor.getInt(3);
                int charging = cursor.getInt(4);
                int brightness = cursor.getInt(5);
                long timeout = cursor.getLong(6);
                long screen = cursor.getLong(7);
                int cpuLoad = cursor.getInt(8);
                long networkTraffic = cursor.getLong(9);
                long mobileTraffic = cursor.getLong(10);
                long totalTraffic = networkTraffic + mobileTraffic;

                currentUsage = new UsageProfile(day, period, period + 1 != 24 ? period + 1 : 0,
                        charging, brightness, timeout, battery, networkTraffic, mobileTraffic, screen, cpuLoad);

                //Determine usage type/s
                if (screen < MAX_IDLE && totalTraffic < 100000 && cpuLoad < 10) {
                    currentUsage.setIdle(true);

                } else if (screen > MIN_HIGH_INTERACTION) {
                    currentUsage.setHighInteraction(true);
                }

                if (totalTraffic > MIN_HIGH_NETWORK) {
                    currentUsage.setHighNetwork(true);
                }

                if (cpuLoad > MIN_HIGH_CPU) {
                    currentUsage.setHighCPU(true);
                }

                if (prevUsage != null) {


                    if(currentUsage.equals(prevUsage)){
                        if(first){
                            totalBatteryUsed = prevUsage.getBatteryLevel();
                            first = false;
                        }
                        prevUsage = currentUsage.merge(prevUsage);

                    }
                    else{
                        //If phone was charging over the period return 0
                        if(prevUsage.getEnd() - prevUsage.getStart() > 1){
                            multiPeriod  = totalBatteryUsed - currentUsage.getBatteryLevel();
                            prevUsage.setBatteryUsed(multiPeriod > 0 ? multiPeriod : 0);
                        }
                        else{
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

    //TODO get charging times and implement generateStats setting wich allows user to take them into account
    //TODO determine battery used per stat

    /**
     * @param info
     * @param day
     */
    public void fill(SystemContext info, String day) {
        ArrayList<String> shit = new ArrayList<String>();
        shit.add("INSERT INTO " + Constants.LOG_TABLE_NAME_ONE + " VALUES (NULL, " + day + ", 0, 100, 1, 255, 15000, 0, 10, 47331, 0)");

        openDBs();

        try {
            for (String i : shit) {
                wdb.execSQL(i);
            }

            close();
        } catch (Exception e) {
        }
    }

    //TODO fix this

    /**
     * @param prev
     * @param current
     * @return
     */
    private long getWeightedAverage(long prev, long current) {
        double weightPrev = 1.25;
        double weightCurrent = 0.75;

        return prev != 0 ? (long) ((prev * weightPrev) + (current * weightCurrent)) / 2 : current;
    }

    /**
     * @param info
     */
    public void logStatus(SystemContext info) {

        openDBs();

        //Get previous row and determine weighted average

        try {

            wdb.execSQL("INSERT INTO " + Constants.LOG_TABLE_NAME_ONE + " VALUES (NULL, "
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

        private String createTable(String name) {
            return "CREATE TABLE IF NOT EXISTS " + name + " ("
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

        /**
         * @param db
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(createTable(Constants.LOG_TABLE_NAME_TWO));
            db.execSQL(createTable(Constants.LOG_TABLE_NAME_ONE));
        }

        /**
         * @param db
         * @param oldVersion
         * @param newVersion
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            db.execSQL(createTable(Constants.LOG_TABLE_NAME_TWO));
        }

        /**
         * @param table
         */
        public void reset(String table) {
            SQLiteDatabase db = getWritableDatabase();
            if (table.equals(Constants.LOG_TABLE_NAME_ONE)) {
                db.execSQL("DROP TABLE IF EXISTS " + table);
                db.execSQL(createTable(Constants.LOG_TABLE_NAME_ONE));
            } else {
                db.execSQL("DROP TABLE IF EXISTS " + table);
                db.execSQL(createTable(Constants.LOG_TABLE_NAME_TWO));
            }

        }
    }
}
