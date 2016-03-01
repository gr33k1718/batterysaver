package application.com.batterysaver;

import android.content.Context;
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
                v.setMinimumWidth(MainActivity.width / 3);
                v.setMinimumHeight(MainActivity.height / 4);
            }
        }

        T p = getItem(position);

        if (p != null) {
            TextView tt1 = (TextView) v.findViewById(R.id.text);

            if (tt1 != null) {
                tt1.setText(p.toString());
            }
        }

        return v;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}
