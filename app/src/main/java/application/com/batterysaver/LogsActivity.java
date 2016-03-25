package application.com.batterysaver;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LogsActivity extends Activity {
    private ListView lv;
    private int value;
    private List<String> logs;
    private Adapter listAdapter;
    private UsageProfile usageProfile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.logs_main);
        lv = (ListView) findViewById(R.id.mylist);

        Intent intent = getIntent();
        value = intent.getIntExtra("key", 0);

        removeItem();
        /*usageProfile = new UsageProfile();
        usageProfile.generateStats();






        PieChart pieChart2 = (PieChart) findViewById(R.id.chart3);
        pieChart2.setTouchEnabled(false);
        // creating data values

        PieDataSet dataset2 = new PieDataSet(entries(usageProfile.getPowerUsagePerDay()[value]), "");

        dataset2.setValueTextSize(10f);

        PieData data2 = new PieData(getLabels(), dataset2); // initialize Piedata
        pieChart2.setData(data2);
        pieChart2.setDescriptionTextSize(10f);
        pieChart2.setDescription("Battery used per state");



        dataset2.setColors(ColorTemplate.COLORFUL_COLORS);*/

        setupAdapter();
    }


    private ArrayList<Entry> entries(double[] usage) {
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry((float) usage[0], 0));
        entries.add(new Entry((float) usage[1], 1));
        entries.add(new Entry((float) usage[2], 2));
        entries.add(new Entry((float) usage[3], 3));
        entries.add(new Entry((float) usage[4], 4));

        return entries;
    }

    private ArrayList<String> getLabels() {

        ArrayList<String> labels = new ArrayList<String>();
        labels.add("Idle");
        labels.add("Casual");
        labels.add("High Interaction");
        labels.add("High Network");
        labels.add("High Cpu");


        return labels;
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

    private void setupAdapter() {
        DatabaseLogger databaseLogger = new DatabaseLogger(this);
        Log.e("[Error]", "" + value);
        Arrays.asList(databaseLogger.getUsagePatterns()[value]);
        logs = new DatabaseLogger(getApplicationContext()).getAllLogs(value + 1);
        listAdapter = new Adapter(this, R.layout.list_item2, logs, false);

        lv.setAdapter(listAdapter);
    }

    private ArrayList<String> savingLogs() {
        DatabaseLogger databaseLogger = new DatabaseLogger(this);
        UsageProfile[] u = databaseLogger.getUsagePatterns()[value];
        ArrayList<String> menu = new ArrayList<>();

        for (UsageProfile usageProfile : u) {
            if (usageProfile != null) {
                menu.add(new SavingsProfile(usageProfile).generate().toString());
            }
        }

        return menu;
    }

}
