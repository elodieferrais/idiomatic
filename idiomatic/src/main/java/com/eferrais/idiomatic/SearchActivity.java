package com.eferrais.idiomatic;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.TextView;

import com.eferrais.idiomatic.adapter.TranslationsListAdapter;
import com.eferrais.idiomatic.client.ClientCallBack;
import com.eferrais.idiomatic.client.TranslationClient;
import com.eferrais.idiomatic.model.Translation;

import net.yscs.android.square_progressbar.SquareProgressBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
    public static class PlaceholderFragment extends Fragment implements TextView.OnEditorActionListener, TextWatcher {

        private List<Translation> translations = new ArrayList<Translation>();
        private List<String> suggestions = new ArrayList<String>();
        //Welcome
        private static final int welcomeAnimationDuration = 600;
        private static final int welcomeAnimationDelay = 800;
        //Loader
        private int progress = 0;
        private static final String COLOR_LOADER_START = "#FFFFFF";
        private static final String COLOR_LOADER_END = "#7FDD4C";
        //Views
        private SquareProgressBar progressBarToAnimate;
        private View rootView;
        private AutoCompleteTextView editText;
        private TextView title;
        private TextView description;
        private ListView listView;
        //Client
        private TranslationClient client;
        //Adapters
        private TranslationsListAdapter adapterTranslations;
        private ArrayAdapter<String> autoCompletionAdapter;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            //Declare client to translate
            client = new TranslationClient(getActivity());

            rootView = inflater.inflate(R.layout.fragment_search, container, false);
            editText = (AutoCompleteTextView) rootView.findViewById(R.id.fragment_search_edittext);
            title = (TextView) rootView.findViewById(R.id.fragment_search_welcome_title_textview);
            description = (TextView) rootView.findViewById(R.id.fragment_search_welcome_description_textview);
            listView = (ListView) rootView.findViewById(R.id.fragment_search_listview);

            //Translations listview
            adapterTranslations = new TranslationsListAdapter(getActivity(), R.layout.translation_cell, translations);
            listView.setAdapter(adapterTranslations);

            suggestions = new ArrayList<String>();
            //Autocompletion listview
            autoCompletionAdapter = new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_dropdown_item_1line, suggestions);
            editText.addTextChangedListener(this);
            editText.setAdapter(autoCompletionAdapter);

            //launch the search on DONE button
            editText.setOnEditorActionListener(this);

            //Welcome animation
            launchWelcomeAnimation();

            return rootView;
        }

        public void startProgressBar() {
            //Progress Bar
            progress = 0;
            final SquareProgressBar squareProgressBarStart = (SquareProgressBar) rootView.findViewById(R.id.fragment_search_start_progressbar);
            final SquareProgressBar squareProgressBarEnd = (SquareProgressBar) rootView.findViewById(R.id.fragment_search_end_progressbar);
            squareProgressBarStart.setImage(-1);
            squareProgressBarStart.setColor(COLOR_LOADER_START);
            squareProgressBarStart.setProgress(progress);
            squareProgressBarStart.setWidth(2);
            squareProgressBarEnd.setImage(-1);
            squareProgressBarEnd.setColor(COLOR_LOADER_END);
            squareProgressBarEnd.setProgress(progress);
            squareProgressBarEnd.setWidth(2);

            //Progress BarAnimation
            progressBarToAnimate = squareProgressBarStart;
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    PlaceholderFragment.this.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progress = (progress + 1) % 101;
                            if (progress == 0) {
                                //Switch the color of the loader
                                if (progressBarToAnimate == squareProgressBarStart) {
                                    progressBarToAnimate = squareProgressBarEnd;
                                } else {
                                    progressBarToAnimate = squareProgressBarStart;
                                }
                            }
                            progressBarToAnimate.setProgress(progress);
                            if (progress == 0) {
                                progressBarToAnimate.bringToFront();
                                squareProgressBarEnd.invalidate();
                                squareProgressBarStart.invalidate();
                                rootView.invalidate();
                            }
                        }
                    });

                }
            };
            Timer progressTimer = new Timer();
            progressTimer.schedule(task, new Date(), 10);
        }

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                client.translationsForExpression(editText.getText().toString(), TranslationClient.LANGUAGE.FRENCH, TranslationClient.LANGUAGE.ENGLISH, new ClientCallBack<List<Translation>>() {
                    @Override
                    public void onResult(List<Translation> result, Error error) {
                        if (result != null && result.size() > 0) {
                            title.setVisibility(View.GONE);
                            description.setVisibility(View.GONE);
                        }
                        translations.clear();
                        translations.addAll(result);
                        adapterTranslations.notifyDataSetChanged();
                    }
                });
            }
            return false;
        }

        private void launchWelcomeAnimation() {
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
                    alphaAnimation.setDuration(welcomeAnimationDuration);
                    alphaAnimation.setFillAfter(true);
                    alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            title.setVisibility(View.VISIBLE);
                            description.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                    title.startAnimation(alphaAnimation);
                    description.startAnimation(alphaAnimation);

                }
            };

            Timer timer = new Timer();
            timer.schedule(timerTask, welcomeAnimationDelay);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            Log.d("ELODIE", s.toString());
            client.getSuggestion(s.toString(), TranslationClient.LANGUAGE.FRENCH, TranslationClient.LANGUAGE.ENGLISH, new ClientCallBack<String[]>() {
                @Override
                public void onResult(String[] result, Error error) {
                    Log.d("ELODIE : " + String.valueOf(result.length), Arrays.asList(result).toString());
                    autoCompletionAdapter.clear();
                    autoCompletionAdapter.addAll(result);
                }
            });

        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }
}