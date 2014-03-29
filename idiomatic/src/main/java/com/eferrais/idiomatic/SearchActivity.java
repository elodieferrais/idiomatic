package com.eferrais.idiomatic;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.eferrais.idiomatic.adapter.TranslationsListAdapter;
import com.eferrais.idiomatic.client.ClientCallBack;
import com.eferrais.idiomatic.client.TranslationClient;
import com.eferrais.idiomatic.model.Translation;

import net.yscs.android.square_progressbar.SquareProgressBar;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SearchActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);

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

        private static final String KEY_SEARCH_VALUE = "com.eferrais.idiomatic:PlaceholderFragment:key_search_value";
        private static final String KEY_RESULT_VALUE = "com.eferrais.idiomatic:PlaceholderFragment:key_result_value";
        private static final String KEY_IS_SEARCHING = "com.eferrais.idiomatic:PlaceholderFragment:key_is_searching";

        private ArrayList<Translation> translations = new ArrayList<Translation>();
        private List<String> suggestions = new ArrayList<String>();
        //Welcome
        private static final int welcomeAnimationDuration = 600;
        private static final int welcomeAnimationDelay = 800;
        //Loader
        private int progress = 0;
        private static final String COLOR_LOADER_START = "#FFFFFF";
        private static final String COLOR_LOADER_END = "#227FBB";
        private Timer progressTimer;
        private static final int loaderFadeAnimationDuration = 400;
        //Views
        private SquareProgressBar progressBarToAnimate;
        private SquareProgressBar squareProgressBarStart;
        private SquareProgressBar squareProgressBarEnd;
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

            rootView = inflater.inflate(R.layout.fragment_search, container, false);
            init();

            if (savedInstanceState != null) {
                String searchText = savedInstanceState.getString(KEY_SEARCH_VALUE);
                editText.setText(searchText);

                if (savedInstanceState.getBoolean(KEY_IS_SEARCHING, false)) {
                    startProgressBar();
                } else {
                    ArrayList<Translation>  translationArrayList = savedInstanceState.getParcelableArrayList(KEY_RESULT_VALUE);
                    translations.addAll(translationArrayList);
                    adapterTranslations.notifyDataSetChanged();
                }


            } else {
                //Welcome animation
                launchWelcomeAnimation();
            }

            return rootView;
        }

        private void init() {
            //Declare client to translate
            client = new TranslationClient(getActivity());

            editText = (AutoCompleteTextView) rootView.findViewById(R.id.fragment_search_edittext);
            editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
            title = (TextView) rootView.findViewById(R.id.fragment_search_welcome_title_textview);
            description = (TextView) rootView.findViewById(R.id.fragment_search_welcome_description_textview);
            listView = (ListView) rootView.findViewById(R.id.fragment_search_listview);
            squareProgressBarStart = (SquareProgressBar) rootView.findViewById(R.id.fragment_search_start_progressbar);
            squareProgressBarEnd = (SquareProgressBar) rootView.findViewById(R.id.fragment_search_end_progressbar);

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
        }

        private void startProgressBar() {
            if (isSearching()) {
                return;
            }

            //Progress Bar
            progress = 0;
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
                                rootView.invalidate();
                            }
                        }
                    });

                }
            };
            progressTimer = new Timer();
            progressTimer.schedule(task, new Date(), 10);
        }

        private void stopProgressBar() {
            if (progressTimer != null) {
                progressTimer.cancel();
                progressTimer.purge();
                progressTimer = null;
            }

            AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
            alphaAnimation.setDuration(loaderFadeAnimationDuration);
            alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    progress = 0;
                    squareProgressBarEnd.setProgress(progress);
                    squareProgressBarStart.setProgress(progress);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            squareProgressBarEnd.startAnimation(alphaAnimation);
            squareProgressBarStart.startAnimation(alphaAnimation);

        }

        private boolean isSearching() {
            return (progressTimer != null);
        }

        private void launchWelcomeAnimation() {
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
                    alphaAnimation.setDuration(welcomeAnimationDuration);
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

        /****TextView.OnEditorActionListener****/
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                editText.dismissDropDown();
                if (editText.getText().toString().length() <= 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.error_title_empty_search)
                            .setMessage(R.string.error_message_empty_search)
                            .setIcon(android.R.drawable.ic_popup_reminder);
                    builder.create().show();
                    return true;
                }
                if (isSearching()) {
                    client.removeTranslationsRequests();
                }
                startProgressBar();
                client.translationsForExpression(editText.getText().toString(), TranslationClient.LANGUAGE.LANG1, TranslationClient.LANGUAGE.LANG2, new ClientCallBack<List<Translation>>() {
                    @Override
                    public void onResult(List<Translation> result, Error error) {
                        stopProgressBar();
                        title.setVisibility(View.GONE);
                        if (result != null && result.size() > 0) {
                            description.setVisibility(View.GONE);
                        } else {
                            description.setVisibility(View.VISIBLE);
                            description.setText(R.string.search_no_results);
                        }
                        translations.clear();
                        translations.addAll(result);
                        adapterTranslations.notifyDataSetChanged();
                    }
                });
            }
            return false;
        }

        /****TextWatcher****/
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            client.getSuggestion(s.toString(), TranslationClient.LANGUAGE.LANG1, TranslationClient.LANGUAGE.LANG2, new ClientCallBack<String[]>() {
                @Override
                public void onResult(String[] result, Error error) {
                    autoCompletionAdapter.clear();
                    autoCompletionAdapter.addAll(result);
                }
            });

        }

        @Override
        public void afterTextChanged(Editable s) {
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putString(KEY_SEARCH_VALUE, editText.getText().toString());
            outState.putParcelableArrayList(KEY_RESULT_VALUE, translations);
            outState.putBoolean(KEY_IS_SEARCHING, isSearching());
        }
    }


}
