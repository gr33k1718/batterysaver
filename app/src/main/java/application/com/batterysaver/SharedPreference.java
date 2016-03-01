package application.com.batterysaver;

import android.content.Context;

import java.util.Calendar;

public class SharedPreference {

    private static String PREFS_NAME = "LOCATIONS";
    private static String SCREEN_TIME_PREFS = "SCRREN_TIME";
    private static String CHARGE_POINTS = "Charge_Points";


    private Calendar time = Calendar.getInstance();
    private Context context = GlobalVars.getAppContext();


    public SharedPreference() {
        super();
    }

    /*public void saveLocations(Context context, List<ChargeLocation> locations) {
        SharedPreferences settings;
        Editor editor;

        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();

        Gson gson = new Gson();
        String chargeLocations = gson.toJson(locations);

        editor.putString(CHARGE_POINTS, chargeLocations);

        editor.commit();
    }

    public ArrayList<ChargeLocation> getChargeLocations(Context context) {
        SharedPreferences settings;
        List<ChargeLocation> locations;

        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        if (settings.contains(CHARGE_POINTS)) {
            String jsonLocations = settings.getString(CHARGE_POINTS, null);
            Gson gson = new Gson();
            ChargeLocation[] chargeLocations = gson.fromJson(jsonLocations,
                    ChargeLocation[].class);

            locations = Arrays.asList(chargeLocations);
            locations = new ArrayList<>(locations);
        } else
            return null;

        return (ArrayList<ChargeLocation>) locations;
    }

    public void removeLocation(int index) {
        List<ChargeLocation> chargeLocations = getChargeLocations(this.context);
        chargeLocations.remove(index);

        saveLocations(context, chargeLocations);

    }

    public void addLocation(ChargeLocation location) {
        float currentDistance;
        float minDistance = 100;
        String date = DateFormat.getDateTimeInstance().format(new Date());
        List<ChargeLocation> chargeLocations = getChargeLocations(this.context);

        location.setLastChargeDate(date);

        if (chargeLocations == null) {
            chargeLocations = new ArrayList<>();
            chargeLocations.add(location);
        } else {
            if (!locationExists(location)) {
                chargeLocations.add(location);
            } else {
                for (ChargeLocation l : chargeLocations) {
                    currentDistance = l.getLocation().distanceTo(location.getLocation());
                    if (currentDistance < minDistance) {
                        l.increaseVisits();
                        l.setChargingTime(time.get(Calendar.HOUR_OF_DAY));
                        l.setLastChargeDate(date);
                        break;
                    }
                }
            }
        }

        saveLocations(context, chargeLocations);
    }


    public void removePrefItem(String fileName, String pref) {
        SharedPreferences settings;
        Editor editor;

        settings = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        editor = settings.edit();
        editor.remove(pref);
        editor.commit();
    }

    public String displayVisits() {
        String result = "";
        ArrayList<ChargeLocation> chargeLocations = getChargeLocations(this.context);

        for (ChargeLocation chargePoint : chargeLocations) {
            result += chargePoint;
            result += "\nCharge times  " + Arrays.toString(chargePoint.getChargeTimes());
        }

        result += "\nNum locations " + chargeLocations.size();

        return result;
    }

    public boolean locationExists(ChargeLocation loc) {
        for (ChargeLocation l : getChargeLocations(this.context)) {
            if (l.getLocation().distanceTo(loc.getLocation()) < 100) {
                return true;
            }
        }
        return false;
    }


    public long daysBetween(Long lastChargeDate, Long currentDate) {
        long lastCharge = lastChargeDate / 1000 / 60 / 60 / 24;
        long current = currentDate / 1000 / 60 / 60 / 24;
        if (lastChargeDate == null || currentDate == null) {
            return -1;
        } else {
            return lastCharge - current;
        }
    }

    public void save(String value) {
        SharedPreferences settings;
        SharedPreferences.Editor editor;

        settings = context.getSharedPreferences(Constants.SYSTEM_CONTEXT_PREFS, Context.MODE_PRIVATE);
        editor = settings.edit();

        editor.putString(Constants.PERIOD_PREF, value);


        editor.commit();
    }

    public String load() {
        SharedPreferences settings;
        settings = context.getSharedPreferences(Constants.SYSTEM_CONTEXT_PREFS, Context.MODE_PRIVATE);

        return settings.getString(Constants.PERIOD_PREF, "Shit");

    }

    public void saveLongInfo(long value, String type) {
        SharedPreferences settings;
        SharedPreferences.Editor editor;

        settings = context.getSharedPreferences(Constants.SYSTEM_CONTEXT_PREFS, Context.MODE_PRIVATE);
        editor = settings.edit();

        switch (type) {
            case "interactionTime":
                editor.putLong(Constants.INTERACTION_TIME_PREF, value);
                break;
            case " networkTraffic":
                editor.putLong(Constants.NETWORK_TRAFFIC_PREF, value);
                break;
            case "mobileTraffic":
                editor.putLong(Constants.MOBILE_TRAFFIC_PREF, value);
                break;
        }
        editor.commit();
    }

    public void setCharging(boolean status){
        SharedPreferences settings;
        SharedPreferences.Editor editor;

        settings = context.getSharedPreferences(Constants.SYSTEM_CONTEXT_PREFS, Context.MODE_PRIVATE);
        editor = settings.edit();

        editor.putBoolean(Constants.IS_CHARGING_PREF, status);

        editor.commit();
    }

    public boolean getCharging(){
        SharedPreferences settings;
        settings = context.getSharedPreferences(Constants.SYSTEM_CONTEXT_PREFS, Context.MODE_PRIVATE);

        return settings.getBoolean(Constants.IS_CHARGING_PREF, false);
    }*/

}
