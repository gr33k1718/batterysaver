package application.com.batterysaver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;

public class WeeklyStatsActivity extends Activity {
    public static int width;
    public static int height;
    private GridView gridview;
    private LinearLayout mainLayout;
    private PieChart mChart;
    private UsageProfile usageProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);


        //ListView lv = (ListView) findViewById(R.id.listView1);
/*
        double[][] powerPerDay = new UsageProfile().getPowerUsagePerDay();


        usageProfile = new UsageProfile();
        usageProfile.generateStats();


        PieChart pieChart = (PieChart) findViewById(R.id.chart);
        pieChart.setTouchEnabled(false);
        // creating data values

        PieDataSet dataset = new PieDataSet(entries(usageProfile.getPowerUsageWeek()), "");

        dataset.setValueTextSize(10f);

        PieData data = new PieData(getLabels(), dataset); // initialize Piedata
        pieChart.setData(data);
        pieChart.setDescriptionTextSize(10f);
        pieChart.setDescription("Battery used per state");

        Legend l = pieChart.getLegend();
        l.setEnabled(false);


        dataset.setColors(ColorTemplate.COLORFUL_COLORS);

        PieChart pieChart2 = (PieChart) findViewById(R.id.chart2);
        pieChart2.setTouchEnabled(false);
        // creating data values

        PieDataSet dataset2 = new PieDataSet(entries(usageProfile.getTimeUsageWeek()), "");

        dataset2.setValueTextSize(10f);

        PieData data2 = new PieData(getLabels(), dataset2); // initialize Piedata
        pieChart2.setData(data2);
        pieChart2.setDescriptionTextSize(10f);
        pieChart2.setDescription("Time in each state");



        dataset2.setColors(ColorTemplate.COLORFUL_COLORS);*/
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
                Intent myIntent = new Intent(WeeklyStatsActivity.this, LogsActivity.class);
                myIntent.putExtra("key", position + 1);
                WeeklyStatsActivity.this.startActivity(myIntent);

            }
        });
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
}