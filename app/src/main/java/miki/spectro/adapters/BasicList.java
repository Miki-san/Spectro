package miki.spectro.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.ArrayList;

public abstract class BasicList extends BaseAdapter {

    private final ArrayList<?> items;
    private final int R_layout_IdView;
    private final Activity act;

    public BasicList(Activity act, int R_layout_IdView, ArrayList<?> items) {
        super();
        this.act   = act;
        this.items = items;
        this.R_layout_IdView = R_layout_IdView;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        if (view == null) {
            LayoutInflater vi = (LayoutInflater) act.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R_layout_IdView, null);
        }
        onItem(items.get(position), view, position);
        return view;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public abstract void onItem(Object item, View view, int position);

}
