package com.eferrais.idiomatic;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.eferrais.idiomatic.adapter.TranslationsListAdapter;
import com.eferrais.idiomatic.client.ClientCallBack;
import com.eferrais.idiomatic.client.TranslationClient;
import com.eferrais.idiomatic.model.Translation;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private List<Translation> translations = new ArrayList<Translation>();

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            //Declare client to translate
            final TranslationClient client = new TranslationClient(getActivity());


            View rootView = inflater.inflate(R.layout.fragment_search, container, false);
            final EditText editText = (EditText) rootView.findViewById(R.id.fragment_search_edittext);
            ListView listView = (ListView) rootView.findViewById(R.id.fragment_search_listview);
            final TranslationsListAdapter adapter = new TranslationsListAdapter(getActivity(),R.layout.translation_cell, translations);
            listView.setAdapter(adapter);
            editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                        client.translationsForExpression(editText.getText().toString(), TranslationClient.LANGUAGE.FRENCH, TranslationClient.LANGUAGE.ENGLISH, new ClientCallBack<List<Translation>>() {
                            @Override
                            public void onResult(List<Translation> result, Error error) {
                                translations.clear();
                                translations.addAll(result);
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                    return false;
                }
            });

            return rootView;
        }
    }

}
