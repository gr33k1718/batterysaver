package application.com.batterysaver;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class LogsActivity extends Activity {
    private ListView lv;
    private int value;
    private List<String> logs;
    private Adapter listAdapter;
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.logs_main);
        lv = (ListView) findViewById(R.id.mylist);
        tv = (TextView) findViewById(R.id.textView);

        Intent intent = getIntent();
        value = intent.getIntExtra("key", 0);

        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);

        tv.setText(""+ status);
        setupAdapter();

        removeItem();

        setupButtons();
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

    public void removeItem() {
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, final long id) {
                AlertDialog.Builder alert = new AlertDialog.Builder(LogsActivity.this);
                alert.setTitle("Caution!!");
                alert.setMessage("Once deleted gone forever");
                alert.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Toast.makeText(getApplicationContext(), String.valueOf(position), Toast.LENGTH_SHORT).show();
                        String a = (String) lv.getItemAtPosition(position);
                        String[] b = a.split(" +");
                        new DatabaseLogger(getApplicationContext()).deleteLog(String.valueOf(b[0]));
                        logs.remove(position);
                        listAdapter.notifyDataSetChanged();
                        dialog.dismiss();

                    }
                });
                alert.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });

                alert.show();
            }
        });
    }

    public void setupButtons() {

        Button mUpdateButton = (Button) findViewById(R.id.updateButton);
        Button mUsageButton = (Button) findViewById(R.id.usageButton);

        mUpdateButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                List<String> logs = new DatabaseLogger(getApplicationContext()).getAllLogs(value);
                listAdapter.clear();
                listAdapter.addAll(logs);
                listAdapter.notifyDataSetChanged();

            }
        });

        mUsageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    public void run() {
                        for (String i : getCPU()) {
                            String[] bla = i.split(" +");
                            Log.d("[shit]", "\nTime: " + bla[2]);

                        }
                    }
                }).start();

                /*Intent intentAirplaneMode = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
                intentAirplaneMode.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentAirplaneMode);*/

                //setMobileDataEnabled(getApplicationContext(), false);

                /*Calendar cal = Calendar.getInstance();
                int day = cal.get(Calendar.DAY_OF_WEEK) - 2;
                //Notify();
*/
/*
                DatabaseLogger d = new DatabaseLogger(getApplicationContext());
                //d.copyTable();
                //d.fill(null, "2");

                //d.getUsagePatterns(Constants.LOG_TABLE_NAME_ONE);
                UsageProfile[] b = d.getUsagePatterns(Constants.LOG_TABLE_NAME_ONE)[2];

                for (UsageProfile c : b) {
                    if (c != null) {
                        Log.d("[shit]", "\nTime: " + c.toString());

                    }
                }*/


                //Log.d("[shit]", "\nTime: " + b[3][8].toString());
                /*List<ApplicationInfo> packages;
                PackageManager pm;
                pm = getPackageManager();
                //get a list of installed apps.
                packages = pm.getInstalledApplications(0);

                ActivityManager mActivityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);

                for (ApplicationInfo packageInfo : packages) {
                    if ((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1) continue;
                    if (packageInfo.packageName.equals("application.com.test")) continue;
                    Log.d("[hmm]", packageInfo.packageName);
                }*/
            }
        });

    }

    private void setupAdapter() {
        logs = new DatabaseLogger(getApplicationContext()).getAllLogs(value);
        listAdapter = new Adapter(this, R.layout.list_item, logs, false);

        lv.setAdapter(listAdapter);
    }


    private void Notify() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setContentTitle("My notification")
                        .setContentText("Hello World!");
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        Intent resultIntent = new Intent(this, MainActivity.class);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(0, mBuilder.build());
    }

    public ArrayList<String> getCPU(){
        ArrayList<String> list = new ArrayList<String>();
        try {
            // -m 10, how many entries you want, -d 1, delay by how much, -n 1,
            // number of iterations
            Process p = Runtime.getRuntime().exec("top -m 5 -d 5 -n 1");

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));
            int i = 0;
            String line ;
            String finalString;
            while ((line = reader.readLine()) != null) {
                finalString = line.trim();
                if(!line.isEmpty() && Character.isDigit(finalString.charAt(0))){
                    finalString = line.replace("%","");
                    Log.e("Output ", finalString);
                    list.add(finalString);
                }

                //line = reader.readLine();
            }

            p.waitFor();

            //Toast.makeText(getBaseContext(), "Got update",Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            //e.printStackTrace();
            //Toast.makeText(getBaseContext(), "Caught", Toast.LENGTH_SHORT).show();
        }
        return list;
    }

}
