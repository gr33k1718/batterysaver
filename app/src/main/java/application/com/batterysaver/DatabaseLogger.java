package application.com.batterysaver;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.lang.reflect.Array;
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
        UsageProfile currentUsage = null;
        int singlePeriod = 0;
        int multiPeriod = 0;
        int totalBatteryUsed = 0;

        boolean first = true;

        final long MAX_IDLE = 120000;
        final long MIN_HIGH_INTERACTION = 1800000;
        final long MIN_HIGH_NETWORK = 1000000;
        final float MIN_HIGH_CPU = 35;

        UsageProfile[][] group = new UsageProfile[7][24];

        openDBs();

        Cursor cursor = wdb.rawQuery("SELECT * FROM " + name, null);

        if (cursor.moveToFirst()) {
            do {
                    int day = cursor.getInt(1) - 1;
                    int period = cursor.getInt(2);
                    int battery = cursor.getInt(3);
                    int charging = cursor.getInt(4);
                    long screen = cursor.getLong(7);
                    long networkTraffic = cursor.getLong(9) + cursor.getLong(10);
                    float cpuLoad = cursor.getFloat(8);
                    int brightness = cursor.getInt(5);
                    long timeout = cursor.getLong(6);

                    currentUsage = new UsageProfile(day,period,period + 1 != 24 ? period + 1 : 0,
                            charging, brightness,timeout,battery,networkTraffic,screen,cpuLoad);

                    //Determine usage type/s
                    if (screen < MAX_IDLE) {
                        currentUsage.setIdle(true);

                    } else if (screen > MIN_HIGH_INTERACTION) {
                        currentUsage.setHighInteraction(true);
                    }

                    if (networkTraffic > MIN_HIGH_NETWORK) {
                        currentUsage.setHighNetwork(true);
                    }

                    if (cpuLoad > MIN_HIGH_CPU) {
                        currentUsage.setHighCPU(true);
                    }

                    if(prevUsage != null){


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
                    }
                    else{
                        prevUsage = currentUsage;
                    }


            } while (cursor.moveToNext());
        }

        cursor.close();

        prefs.putUsageProfiles(group);
        prefs.commit();

        return group;
    }


    public void fill(SystemContext info, String day) {
        ArrayList<String> shit = new ArrayList<String>();

        shit.add("INSERT INTO " + Constants.LOG_TABLE_NAME_ONE + " VALUES (NULL, " + day + ", 0, 100, 1, 255, 15000, 0, 10, 47331, 0)");
        shit.add("INSERT INTO " + Constants.LOG_TABLE_NAME_ONE + " VALUES (NULL, " + day + ", 1, 100, 1, 255, 15000, 0, 11, 98742, 0)");
        shit.add("INSERT INTO " + Constants.LOG_TABLE_NAME_ONE + " VALUES (NULL, " + day + ", 2, 100, 1, 255, 15000, 0, 12, 12313, 0)");
        shit.add("INSERT INTO " + Constants.LOG_TABLE_NAME_ONE + " VALUES (NULL, " + day + ", 3, 100, 1, 255, 15000, 0, 7, 9064, 0)");
        shit.add("INSERT INTO " + Constants.LOG_TABLE_NAME_ONE + " VALUES (NULL, " + day + ", 4, 100, 1, 255, 15000, 0, 8, 5324, 0)");
        shit.add("INSERT INTO " + Constants.LOG_TABLE_NAME_ONE + " VALUES (NULL, " + day + ", 5, 100, 1, 255, 15000, 0, 5, 4232, 0)");
        shit.add("INSERT INTO " + Constants.LOG_TABLE_NAME_ONE + " VALUES (NULL, " + day + ", 6, 100, 1, 255, 15000, 0, 9, 13441, 0)");
        shit.add("INSERT INTO " + Constants.LOG_TABLE_NAME_ONE + " VALUES (NULL, " + day + ", 7, 100, 1, 255, 15000, 42776, 16, 5532, 0)");
        shit.add("INSERT INTO " + Constants.LOG_TABLE_NAME_ONE + " VALUES (NULL, " + day + ", 8, 98, 0, 255, 15000, 637731, 19, 3123441, 0)");
        shit.add("INSERT INTO " + Constants.LOG_TABLE_NAME_ONE + " VALUES (NULL, " + day + ", 9, 95, 0, 255, 15000, 12331, 15, 33133, 0)");

        shit.add("INSERT INTO " + Constants.LOG_TABLE_NAME_ONE + " VALUES (NULL, " + day + ", 10, 95, 0, 255, 15000, 60003, 13, 44332, 0)");
        shit.add("INSERT INTO " + Constants.LOG_TABLE_NAME_ONE + " VALUES (NULL, " + day + ", 11, 94, 0, 255, 15000, 65544, 19, 88754, 0)");
        shit.add("INSERT INTO " + Constants.LOG_TABLE_NAME_ONE + " VALUES (NULL, " + day + ", 12, 93, 0, 255, 15000, 32455, 14, 65245, 0)");
        shit.add("INSERT INTO " + Constants.LOG_TABLE_NAME_ONE + " VALUES (NULL, " + day + ", 13, 92, 0, 255, 15000, 1904437, 32, 0, 4344552)");
        shit.add("INSERT INTO " + Constants.LOG_TABLE_NAME_ONE + " VALUES (NULL, " + day + ", 14, 88, 0, 255, 15000, 31244, 21, 77644, 0)");
        shit.add("INSERT INTO " + Constants.LOG_TABLE_NAME_ONE + " VALUES (NULL, " + day + ", 15, 87, 0, 255, 15000, 77765, 14, 76543, 0)");
        shit.add("INSERT INTO " + Constants.LOG_TABLE_NAME_ONE + " VALUES (NULL, " + day + ", 16, 86, 0, 255, 15000, 1008390, 19, 54344, 0)");
        shit.add("INSERT INTO " + Constants.LOG_TABLE_NAME_ONE + " VALUES (NULL, " + day + ", 17, 84, 0, 255, 15000, 42776, 14, 55231, 0)");
        shit.add("INSERT INTO " + Constants.LOG_TABLE_NAME_ONE + " VALUES (NULL, " + day + ", 18, 82, 0, 255, 15000, 1033382, 28, 3455515, 0)");
        shit.add("INSERT INTO " + Constants.LOG_TABLE_NAME_ONE + " VALUES (NULL, " + day + ", 19, 78, 0, 255, 15000, 1848470, 35, 985893, 0)");

        shit.add("INSERT INTO " + Constants.LOG_TABLE_NAME_ONE + " VALUES (NULL, " + day + ", 20, 76, 0, 255, 15000, 1198977, 19, 441088, 0)");
        shit.add("INSERT INTO " + Constants.LOG_TABLE_NAME_ONE + " VALUES (NULL, " + day + ", 21, 77, 1, 255, 15000, 32333, 17, 42233, 0)");
        shit.add("INSERT INTO " + Constants.LOG_TABLE_NAME_ONE + " VALUES (NULL, " + day + ", 22, 96, 1, 255, 15000, 20039, 21, 245535, 0)");
        shit.add("INSERT INTO " + Constants.LOG_TABLE_NAME_ONE + " VALUES (NULL, " + day + ", 23, 100, 1, 255, 15000, 0, 16, 321345, 0)");





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
                    + Constants.PERIOD_PREF + " = " + info.period, null);
            if (cursor.getCount() > 0) {

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


                String a = info.day + " ,"
                        + info.period + " ,"
                        + info.batteryLevel + " ,"
                        + info.charging + " ,"
                        + info.brightness + " ,"
                        + info.timeOut + " ,"
                        + info.interactionTime + " ,"
                        + info.cpuLoad + " ,"
                        + info.networkTraffic + " ,"
                        + info.mobileTraffic;
                Log.d("[bollocks]", logItem + "\nCurrent " + a);
            }
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
            Log.d("[bollocks]", "hi");
            close();
        }catch (Exception e) {
            Log.d("[bollocks]", e.toString());
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
