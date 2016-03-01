package application.com.batterysaver;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class MainActivity extends Activity {
    public static int width;
    public static int height;
    private GridView gridview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        gridview = (GridView) findViewById(R.id.gridview);

        setupAdapter();

        getWindowSize();

        viewLogs();

        scheduleAlarm();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public void viewLogs() {
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, final long id) {
                Intent myIntent = new Intent(MainActivity.this, LogsActivity.class);
                myIntent.putExtra("key", position + 1);
                MainActivity.this.startActivity(myIntent);

            }
        });
    }

    public void scheduleAlarm() {
        Calendar time = Calendar.getInstance();
        Toast.makeText(this, "Alarm set", Toast.LENGTH_SHORT).show();
        time.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY) + 1);
        time.set(Calendar.MINUTE, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        PendingIntent p = PendingIntent.getService(this, 1, new Intent(this, LogService.class), PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.cancel(p);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), AlarmManager.INTERVAL_HOUR, p);
    }

    private void getWindowSize() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        width = metrics.widthPixels;
        height = metrics.heightPixels;
    }

    private void setupAdapter() {
        ArrayList<String> days = new ArrayList<>(Arrays.asList(new String[]{"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"}));
        Adapter gridAdapter = new Adapter(this, R.layout.grid_item, days, true);
        gridview.setAdapter(gridAdapter);
    }
}
