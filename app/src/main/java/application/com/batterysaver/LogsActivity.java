package application.com.batterysaver;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

public class LogsActivity extends Activity {
    private ListView lv;
    private int value;
    private List<String> logs;
    private Adapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.logs_main);
        lv = (ListView) findViewById(R.id.mylist);

        Intent intent = getIntent();
        value = intent.getIntExtra("key", 0);

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
                List<ApplicationInfo> packages;
                PackageManager pm;
                pm = getPackageManager();
                //get a list of installed apps.
                packages = pm.getInstalledApplications(0);

                ActivityManager mActivityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);

                for (ApplicationInfo packageInfo : packages) {
                    if ((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1) continue;
                    if (packageInfo.packageName.equals("application.com.test")) continue;
                    Log.d("[hmm]", packageInfo.packageName);
                }
            }
        });

    }

    private void setupAdapter() {
        logs = new DatabaseLogger(getApplicationContext()).getAllLogs(value);
        listAdapter = new Adapter(this, R.layout.list_item, logs, false);

        lv.setAdapter(listAdapter);
    }

}
