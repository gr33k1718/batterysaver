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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

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


/*
        float[] coreValues = new float[10];
        //get how many cores there are from function
        int numCores = getNumCores();
        for(byte i = 0; i < numCores; i++)
        {
            coreValues[i] = readCore(i);
            Log.e("[Error]", "Core " + i + " has load " + coreValues[i]);
        }
*/

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

    public double scale(final double valueIn, final double baseMin, final double baseMax, final double limitMin, final double limitMax) {
        return ((limitMax - limitMin) * (valueIn - baseMin) / (baseMax - baseMin)) + limitMin;
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

    /*private ArrayList<String> savingLogs() {
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
*/

    private int getNumCores() {
        //Private Class to display only CPU devices in the directory listing
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                //Check if filename is "cpu", followed by a single digit number
                if (Pattern.matches("cpu[0-9]+", pathname.getName())) {
                    return true;
                }
                return false;
            }
        }

        try {
            //Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            //Filter to only list the devices we care about
            File[] files = dir.listFiles(new CpuFilter());
            //Return the number of cores (virtual CPU devices)
            return files.length;
        } catch (Exception e) {
            //Default to return 1 core
            return 1;
        }
    }

    // for multi core value
    private float readCore(int i) {
    /*
     * how to calculate multicore this function reads the bytes from a
     * logging file in the android system (/proc/stat for cpu values) then
     * puts the line into a string then spilts up each individual part into
     * an array then(since he know which part represents what) we are able
     * to determine each cpu total and work then combine it together to get
     * a single float for overall cpu usage
     */
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
            // skip to the line we need
            for (int ii = 0; ii < i + 1; ++ii) {
                String line = reader.readLine();
            }
            String load = reader.readLine();

            // cores will eventually go offline, and if it does, then it is at
            // 0% because it is not being
            // used. so we need to do check if the line we got contains cpu, if
            // not, then this core = 0
            if (load.contains("cpu")) {
                Log.e("[Error]", "Core start " + load);
                String[] toks = load.split(" ");

                // we are recording the work being used by the user and
                // system(work) and the total info
                // of cpu stuff (total)
                // http://stackoverflow.com/questions/3017162/how-to-get-total-cpu-usage-in-linux-c/3017438#3017438

                long work1 = Long.parseLong(toks[1]) + Long.parseLong(toks[2])
                        + Long.parseLong(toks[3]);
                long total1 = Long.parseLong(toks[1]) + Long.parseLong(toks[2])
                        + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
                        + Long.parseLong(toks[5]) + Long.parseLong(toks[6])
                        + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

                try {
                    // short sleep time = less accurate. But android devices
                    // typically don't have more than
                    // 4 cores, and I'n my app, I run this all in a second. So,
                    // I need it a bit shorter
                    Thread.sleep(2000);
                } catch (Exception e) {
                }

                reader.seek(0);
                // skip to the line we need
                for (int ii = 0; ii < i + 1; ++ii) {
                    reader.readLine();
                }
                load = reader.readLine();

                // cores will eventually go offline, and if it does, then it is
                // at 0% because it is not being
                // used. so we need to do check if the line we got contains cpu,
                // if not, then this core = 0%
                if (load.contains("cpu")) {
                    Log.e("[Error]", "Core end " + load);
                    reader.close();
                    toks = load.split(" ");

                    long work2 = Long.parseLong(toks[1]) + Long.parseLong(toks[2])
                            + Long.parseLong(toks[3]);
                    long total2 = Long.parseLong(toks[1]) + Long.parseLong(toks[2])
                            + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
                            + Long.parseLong(toks[5]) + Long.parseLong(toks[6])
                            + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

                    // here we find the change in user work and total info, and
                    // divide by one another to get our total
                    // seems to be accurate need to test on quad core
                    // http://stackoverflow.com/questions/3017162/how-to-get-total-cpu-usage-in-linux-c/3017438#3017438

                    if ((total2 - total1) == 0)
                        return 0;
                    else
                        return (float) (work2 - work1) / ((total2 - total1));

                } else {
                    reader.close();
                    return 0;
                }

            } else {
                reader.close();
                return 0;
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return 0;
    }
}
