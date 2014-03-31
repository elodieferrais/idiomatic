package com.eferrais.idiomatic.adapter;

import android.content.Context;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.eferrais.idiomatic.R;
import com.eferrais.idiomatic.UI.ClassStyle;
import com.eferrais.idiomatic.model.Translation;

import org.jsoup.helper.StringUtil;
import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TranslationsListAdapter extends ArrayAdapter<Translation> {
    final private int resourceId;
    final private LayoutInflater inflater;
    final private Context context;

    public TranslationsListAdapter(Context context, int resource, List<Translation> objects) {
        super(context, resource, objects);
        this.resourceId = resource;
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

        SpannableStringBuilder initialSpannable = new SpannableStringBuilder(getItem(position).getInitialText());
        SpannableStringBuilder translatedSpannable = new SpannableStringBuilder(getItem(position).getTranslatedText());

        setSpans(initialSpannable);
        setSpans(translatedSpannable);

        initialTextView.setText(initialSpannable);
        translatedTextView.setText(translatedSpannable);

        return view;
    }

    private void setSpans(SpannableStringBuilder text) {
        for (ClassStyle classStyle: ClassStyle.values()) {
            Pattern pattern = Pattern.compile("<span.*?"+classStyle.name+".*?>(.*?)</span>");

            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                BackgroundColorSpan backgroundColorSpan = new BackgroundColorSpan(classStyle.getBackgroundColor());
                text.setSpan(backgroundColorSpan,  matcher.start(1), matcher.end(1), 0);

            }
        }

        Pattern patternTags =  Pattern.compile("<.*?>");
        Matcher matcherTags = patternTags.matcher(text);
        int removedCount = 0;
        while (matcherTags.find()) {
            text.delete(matcherTags.start() - removedCount, matcherTags.end() - removedCount);
            removedCount = removedCount + (matcherTags.end() - matcherTags.start());
        }


    }
}
