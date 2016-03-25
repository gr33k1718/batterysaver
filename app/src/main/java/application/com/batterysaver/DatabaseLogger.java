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

    public void copyTable() {

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

    public UsageProfile[][] getUsagePatterns() {
        Log.d("[hi]", "profiles");

        UsageProfile prevUsage = null;
        UsageProfile currentUsage;

        int singlePeriod;
        int multiPeriod;
        int totalBatteryUsed = 0;

        boolean multi = false;


        final long MAX_IDLE = 10000;
        final long MIN_HIGH_INTERACTION = 1800000;
        final long MIN_HIGH_NETWORK = 4000000;
        final float MIN_HIGH_CPU = 34;

        UsageProfile[][] group = new UsageProfile[7][24];

        openDBs();

        Cursor cursor = wdb.rawQuery("SELECT * FROM " + Constants.LOG_TABLE_NAME_TWO, null);

        if (cursor.moveToFirst()) {
            do {
                int day = cursor.getInt(1);
                int period = cursor.getInt(2);
                int battery = cursor.getInt(3);
                int charging = cursor.getInt(4);
                long screen = cursor.getLong(7);
                int batteryUsage = 0;
                int cpuLoad = cursor.getInt(8);
                int brightness = cursor.getInt(5);
                long timeout = cursor.getLong(6);
                long networkTraffic = cursor.getLong(9);
                long mobileTraffic = cursor.getLong(10);
                long totalTraffic = networkTraffic + mobileTraffic;

                currentUsage = new UsageProfile(day, period, period + 1 != 24 ? period + 1 : 0,
                        charging, brightness, timeout, battery, networkTraffic, mobileTraffic, screen, cpuLoad);

                //Determine usage type/s
                if (screen < MAX_IDLE && totalTraffic < 100000) {
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


                    if (currentUsage.equals(prevUsage)) {
                        multi = true;
                        prevUsage = currentUsage.merge(prevUsage);

                        batteryUsage = Predictor.predictBatteryUsage(networkTraffic,mobileTraffic,cpuLoad,screen,brightness);
                        //Log.d("[hi]", "\n\nPrev " + batteryUsage);
                        prevUsage.setBatteryUsed(batteryUsage + prevUsage.getBatteryUsed());


                    } else {
                        if(!multi){
                            int cpu = (int)prevUsage.getCpu();
                            long inter = prevUsage.getInteractionTime();
                            long network = prevUsage.getNetworkUsage();
                            long mobile = prevUsage.getMobileUsage();
                            int bright = prevUsage.getBrightness();

                            int batteryUsages = Predictor.predictBatteryUsage(network,mobile,cpu,inter,bright);
                            prevUsage.setBatteryUsed(batteryUsages);

                        }
                        multi = false;

                        Log.d("[hi]", "\n\nPrev " + prevUsage + " " + prevUsage.getDay() + " " + totalBatteryUsed);
                        group[prevUsage.getDay() - 1][prevUsage.getStart()] = prevUsage;
                        prevUsage = currentUsage;

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
            for (String i : shit) {
                wdb.execSQL(i);
            }

            close();
        } catch (Exception e) {
        }
    }

    //TODO fix this
    private long getWeightedAverage(long prev, long current) {
        double weightPrev = 0.75;
        double weightCurrent = 1.25;

        return prev != 0 ? (long) ((prev * weightPrev) + (current * weightCurrent)) / 2 : current;
    }

    public void logStatus(SystemContext info) {

        openDBs();

        long screen = 0;
        long networkTraffic = 0;
        long mobileTraffic = 0;
        int cpuLoad = 0;

        //Get previous row and determine weighted average

        try {
            Cursor cursor = wdb.rawQuery("SELECT * FROM " + Constants.LOG_TABLE_NAME_TWO +
                    " WHERE " + Constants.DAY_PREF + " = " + info.day + " AND "
                    + Constants.PERIOD_PREF + " = " + info.period, null);
            if (cursor != null && cursor.getCount() > 0) {

                cursor.moveToFirst();
                screen = cursor.getLong(7);
                networkTraffic = cursor.getLong(9);
                mobileTraffic = cursor.getLong(10);
                cpuLoad = cursor.getInt(8);
                Log.e("[Error]", "" + screen + " " + networkTraffic);

            }

            wdb.execSQL("INSERT INTO " + Constants.LOG_TABLE_NAME_ONE + " VALUES (NULL, "
                    + info.day + " ,"
                    + info.period + " ,"
                    + info.batteryLevel + " ,"
                    + info.charging + " ,"
                    + info.brightness + " ,"
                    + info.timeOut + " ,"
                    + getWeightedAverage(screen, info.interactionTime)+ " ,"
                    + getWeightedAverage(cpuLoad,info.cpuLoad) + " ,"
                    + getWeightedAverage(networkTraffic,info.networkTraffic) + " ,"
                    + getWeightedAverage(mobileTraffic,info.mobileTraffic)
                    + ")");

            close();
        } catch (Exception e) {
            Log.e("[Error]", e.toString());
        }


    }

    public void deleteLog(String id) {

        openDBs();

        try {
            wdb.execSQL("DELETE FROM " + Constants.LOG_TABLE_NAME_ONE + " WHERE " + KEY_ID + " = " + id);
        } catch (Exception e) {
            Log.e("[Error]", e.toString());
        }
    }

    public void clearAllLogs(String table) {
        mSQLOpenHelper.reset(table);
    }

    private static class SQLOpenHelper extends SQLiteOpenHelper {
        public SQLOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        private String createTable(String name) {
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

        public void reset(String table) {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("DROP TABLE IF EXISTS " + table);
            onCreate(db);
        }
    }
}
