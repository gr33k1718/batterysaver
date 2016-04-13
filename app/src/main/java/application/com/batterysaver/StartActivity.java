package application.com.batterysaver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class StartActivity extends Activity {
    private ListView lv;
    private TextView textView;
    private Adapter listAdapter;
    private DatabaseLogger database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main2);
        lv = (ListView) findViewById(R.id.mylist2);
        textView = (TextView)findViewById(R.id.textView);

        BatteryManager connectivityManager = (BatteryManager)MyApplication.getAppContext().getSystemService(Context.BATTERY_SERVICE);
        //long capacity = connectivityManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER);
        int avgMah = connectivityManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE);
        int mAh = connectivityManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);

        database = new DatabaseLogger(this);

        setupAdapter();

        viewData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main2, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void viewData() {
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, final long id) {
                if (position == 0) {
                    Intent myIntent = new Intent(StartActivity.this, WeeklyStatsActivity.class);
                    StartActivity.this.startActivity(myIntent);
                } else {
                    Intent myIntent = new Intent(StartActivity.this, LogsActivity.class);
                    myIntent.putExtra("key", position - 1);
                    StartActivity.this.startActivity(myIntent);
                }
            }
        });
    }

    private void setupAdapter() {

        listAdapter = new Adapter(this, R.layout.list_item, menu(), false);

        lv.setAdapter(listAdapter);
    }

    private ArrayList<String> menu() {
        ArrayList<String> menu = new ArrayList<>();
        menu.add("Weekly");
        menu.add("Sunday");
        menu.add("Monday");
        menu.add("Tuesday");
        menu.add("Wednesday");
        menu.add("Thursday");
        menu.add("Friday");
        menu.add("Saturday");

        return menu;
    }
}
