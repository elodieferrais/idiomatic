package com.eferrais.idiomatic.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;

/**
 * Created by elodieferrais on 6/10/15.
 */
public class AutocompletionAdapter extends ArrayAdapter<String> {
    public AutocompletionAdapter(Context context, int resource, List<String> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }
}
