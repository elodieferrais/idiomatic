package com.eferrais.idiomatic;

import com.eferrais.idiomatic.adapter.TranslationsListAdapter;
import com.eferrais.idiomatic.client.ClientCallBack;
import com.eferrais.idiomatic.client.TranslationClient;
import com.eferrais.idiomatic.model.Translation;
import com.elodieferrais.mobile.android.asyncautocompletelibrary.AutoCompleteHandler;
import com.elodieferrais.mobile.android.asyncautocompletelibrary.EasyAutoCompleteTextView;

import net.yscs.android.square_progressbar.SquareProgressBar;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class SearchFragment extends Fragment {

    private static final String KEY_SEARCH_VALUE = "com.eferrais.idiomatic:PlaceholderFragment:key_search_value";
    private static final String KEY_RESULT_VALUE = "com.eferrais.idiomatic:PlaceholderFragment:key_result_value";
    private static final String KEY_IS_SEARCHING = "com.eferrais.idiomatic:PlaceholderFragment:key_is_searching";
    //Welcome
    private static final int welcomeAnimationDuration = 600;
    private static final int welcomeAnimationDelay = 800;
    private static final String COLOR_LOADER_START = "#FFFFFF";
    private static final String COLOR_LOADER_END = "#227FBB";
    private static final int loaderFadeAnimationDuration = 400;
    private ArrayList<Translation> translations = new ArrayList<Translation>();
    //Loader
    private int progress = 0;
    private Timer progressTimer;
    //Views
    private SquareProgressBar progressBarToAnimate;
    private SquareProgressBar squareProgressBarStart;
    private SquareProgressBar squareProgressBarEnd;
    private View rootView;
    private EasyAutoCompleteTextView editText;
    private TextView title;
    private TextView description;
    private ListView listView;
    //Client
    private TranslationClient client;
    //Adapters
    private TranslationsListAdapter adapterTranslations;

    public SearchFragment() {
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
                ArrayList<Translation> translationArrayList = savedInstanceState.getParcelableArrayList(KEY_RESULT_VALUE);
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

        editText = (EasyAutoCompleteTextView) rootView.findViewById(R.id.fragment_search_edittext);
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        title = (TextView) rootView.findViewById(R.id.fragment_search_welcome_title_textview);
        description = (TextView) rootView.findViewById(R.id.fragment_search_welcome_description_textview);
        listView = (ListView) rootView.findViewById(R.id.fragment_search_listview);
        squareProgressBarStart = (SquareProgressBar) rootView.findViewById(R.id.fragment_search_start_progressbar);
        squareProgressBarEnd = (SquareProgressBar) rootView.findViewById(R.id.fragment_search_end_progressbar);
        ImageButton infoButton = (ImageButton) rootView.findViewById(R.id.fragment_search_info_button);

        //Translations listview
        adapterTranslations = new TranslationsListAdapter(getActivity(), R.layout.translation_cell, translations);
        listView.setAdapter(adapterTranslations);

        //Info Button
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), InfoActivity.class);
                startActivity(intent);
            }
        });

        editText.setAutoCompleteHandler(new AutoCompleteHandler() {
            @Override
            public void onSuggestionFetching(String term, final SuggestionsCallback callback) {
                SearchFragment.this.fetchSuggestions(term, new ClientCallBack<String[]>() {
                    @Override
                    public void onResult(String[] result, Error error) {
                        callback.onResult(result, error);
                    }
                });
            }

            @Override
            public void onSuggestionClicked(String suggestion) {
                updateTranslation(suggestion);
            }
        });
        //launch the search on DONE button
        //editText.setOnEditorActionListener(this);
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
                SearchFragment.this.getActivity().runOnUiThread(new Runnable() {
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

    private void showEmptySearchErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.error_title_empty_search)
                .setMessage(R.string.error_message_empty_search)
                .setIcon(android.R.drawable.ic_popup_reminder);
        builder.create().show();
    }

    public void updateTranslation(String expression) {
        if (isSearching()) {
            client.removeTranslationsRequests();
        }
        startProgressBar();
        client.translationsForExpression(expression, TranslationClient.LANGUAGE.LANG1, TranslationClient.LANGUAGE.LANG2, new ClientCallBack<List<Translation>>() {
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

    public void fetchSuggestions(String term, final ClientCallBack<String[]> clientCallBack) {
        client.getSuggestion(term, TranslationClient.LANGUAGE.LANG1, TranslationClient.LANGUAGE.LANG2, clientCallBack);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_SEARCH_VALUE, editText.getText().toString());
        outState.putParcelableArrayList(KEY_RESULT_VALUE, translations);
        outState.putBoolean(KEY_IS_SEARCHING, isSearching());
    }
}
