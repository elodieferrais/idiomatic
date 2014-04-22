package com.eferrais.idiomatic.adapter;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

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

        final TextView initialTextView = (TextView) view.findViewById(R.id.translation_cell_initial_textview);
        final TextView translatedTextView = (TextView) view.findViewById(R.id.translation_cell_translated_textview);

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Activity activity = (Activity)context;
                String message = "";
                if (initialTextView != null) {
                    message = initialTextView + "\n";
                }
                if (translatedTextView != null) {
                    message += translatedTextView;
                }
                activity.startActionMode(new ActionBarCallBack(message));
                return true;
            }
        });

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

    class ActionBarCallBack implements ActionMode.Callback
    {
        private String message;
        ActionBarCallBack(String message) {
            this.message = message;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item)
        {
            // TODO Auto-generated method stub
            switch(item.getItemId()) {
                case R.id.copyText:
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("translation", message);
                    clipboard.setPrimaryClip(clip);
                    return true;
            }
            return false;

        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu)
        {
            mode.getMenuInflater().inflate(R.menu.clipboard_menu, menu);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode)
        {}

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu)
        {
            return false;
        }
    }

}
