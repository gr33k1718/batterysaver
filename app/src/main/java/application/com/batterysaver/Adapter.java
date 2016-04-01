package application.com.batterysaver;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class Adapter<T> extends ArrayAdapter<T> {

    private int resource;
    private boolean isGridView;

    public Adapter(Context context, int resource, List<T> items, boolean isGridView) {
        super(context, resource, items);
        this.resource = resource;
        this.isGridView = isGridView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;


        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(resource, null);

            if (isGridView) {
                v.setMinimumWidth(WeeklyStatsActivity.width / 2);
                v.setMinimumHeight(WeeklyStatsActivity.height / 6);
            }

        }

        T p = getItem(position);

        if (p != null) {
            TextView tt1 = (TextView) v.findViewById(R.id.text);
            TextView tt2 = (TextView) v.findViewById(R.id.text2);
            TextView tt3 = (TextView) v.findViewById(R.id.text3);


            if (tt1 != null) {
                tt1.setText(p.toString());

            }

            if (tt3 != null) {

                tt3.setText(p.toString());
            }
            if (tt2 != null && position == 0) {
                tt2.setText("Various stats about usage");
                tt2.setBackgroundColor(Color.parseColor("#7986CB"));
                tt1.setBackgroundColor(Color.parseColor("#7986CB"));
            } else if (tt2 != null) {
                tt2.setText("Profiles patterns,etc");
                tt2.setBackgroundColor(Color.parseColor("#90CAF9"));
                tt1.setBackgroundColor(Color.parseColor("#90CAF9"));
            }
        }

        return v;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    public class Holder {
        TextView tv;
    }
}
