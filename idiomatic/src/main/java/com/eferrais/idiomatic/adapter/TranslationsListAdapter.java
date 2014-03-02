package com.eferrais.idiomatic.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.eferrais.idiomatic.R;
import com.eferrais.idiomatic.model.Translation;

import java.util.List;

/**
 * Created by elodieferrais on 2/27/14.
 */
public class TranslationsListAdapter extends ArrayAdapter<Translation> {
    private List<Translation> translations;
    private int resourceId;
    private Context context;
    private LayoutInflater inflater;

    public TranslationsListAdapter(Context context, int resource, List<Translation> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resourceId = resource;
        this.translations = objects;
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = inflater.inflate(resourceId, parent, false);
        } else {
            view = convertView;
        }

        TextView initialTextView = (TextView) view.findViewById(R.id.translation_cell_initial_textview);
        TextView translatedTextView = (TextView) view.findViewById(R.id.translation_cell_translated_textview);
        initialTextView.setText(getItem(position).getInitialText());
        translatedTextView.setText(getItem(position).getTranslatedText());

        return view;
    }
}
