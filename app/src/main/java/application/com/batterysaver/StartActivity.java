package application.com.batterysaver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * The start activity displayed when the user first opens the application. Displays a menu
 * split by day of the week
 */
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
        textView = (TextView) findViewById(R.id.textView);

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

    /**
     * Gets the data associated with the day clicked
     */
    public void viewData() {
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, final long id) {
                Intent myIntent = new Intent(StartActivity.this, LogsActivity.class);
                myIntent.putExtra("key", position - 1);
                StartActivity.this.startActivity(myIntent);

            }
        });
    }

    /**
     * Sets up the list of days
     */
    private void setupAdapter() {

        listAdapter = new Adapter(this, R.layout.list_item, menu(), false);

        lv.setAdapter(listAdapter);
    }

    /**
     * Grabs the days of the week
     *
     * @return the list of days
     */
    private ArrayList<String> menu() {
        ArrayList<String> menu = new ArrayList<>();
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
