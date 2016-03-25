package application.com.batterysaver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity2 extends Activity {
    private ListView lv;
    private Adapter listAdapter;
    private DatabaseLogger database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main2);
        lv = (ListView) findViewById(R.id.mylist2);

        //database = new DatabaseLogger(this);

        //database.fill(null,"1");

        setupAdapter();

        viewShit();
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

    public void viewShit() {
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, final long id) {
                if (position == 0) {
                    Intent myIntent = new Intent(MainActivity2.this, MainActivity.class);
                    MainActivity2.this.startActivity(myIntent);
                } else {
                    Intent myIntent = new Intent(MainActivity2.this, LogsActivity.class);
                    myIntent.putExtra("key", position - 1);
                    MainActivity2.this.startActivity(myIntent);
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
