package com.eferrais.idiomatic.client;

import android.content.Context;
import android.text.Html;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.Volley;
import com.eferrais.idiomatic.R;
import com.eferrais.idiomatic.UI.ClassStyle;
import com.eferrais.idiomatic.model.Translation;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by elodieferrais on 2/27/14.
 */
public class TranslationClient {
    private static final String REQUEST_TRANSLATION_TAG = "request:translation:tag";
    private static final String REQUEST_SUGGESTION_TAG = "request:suggestion:tag";
    final private RequestQueue requestQueue;
    final private Context context;

    public TranslationClient(Context context) {
        requestQueue = Volley.newRequestQueue(context);
        this.context = context.getApplicationContext();
    }

    public enum LANGUAGE {
        LANG1(R.string.language_1),
        LANG2(R.string.language_2);

        private int resourceId;

        LANGUAGE(int resourceId) {
            this.resourceId = resourceId;
        }

        public int getResourceId() {
            return resourceId;
        }
    }



    public void translationsForExpression(final String expression, final LANGUAGE fromLang, final LANGUAGE toLang, final ClientCallBack<List<Translation>> callBack) {
        String url = null;
        try {
            url = String.format("http://www.linguee.com/%s-%s/search?source=auto&query=%s", context.getString(fromLang.getResourceId()), context.getString(toLang.getResourceId()), URLEncoder.encode(expression, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        Request<List<Translation>> request = new Request<List<Translation>>(Request.Method.GET, url, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }) {
            @Override
            protected Response<List<Translation>> parseNetworkResponse(NetworkResponse networkResponse) {
                String result = null;
                try {
                    result = new String(networkResponse.data, context.getString(R.string.encoding));
                } catch (UnsupportedEncodingException e) {
                    callBack.onResult(null, new Error(e.getMessage()));
                }
                Document document = Jsoup.parse(result);

                Elements elements = document.select("td.sentence");
                Elements lefts = elements.select(".left");
                Elements rights = elements.select(".right2");


                List<Translation> translations = new ArrayList<Translation>(Math.min(lefts.size(), rights.size()));
                if (lefts != null && rights != null) {
                    for (int i = 0; i < Math.min(lefts.size(), rights.size()); i++) {
                        Translation translation = new Translation(treatString(lefts.get(i).html()), treatString(rights.get(i).html()));
                        translations.add(translation);
                    }
                }

                return Response.success(translations, HttpHeaderParser.parseCacheHeaders(networkResponse));
            }

            @Override
            protected void deliverResponse(List<Translation> result) {
                callBack.onResult(result, null);
            }
        };
        request.setTag(REQUEST_TRANSLATION_TAG);
        requestQueue.add(request);
    }

    private String treatString(String string) {
        String updated = string.replaceAll("[\\r\\n\\w\\.-]+\\.[\\w]{1,3}", "");
        updated = updated.toString().replaceAll("<(?!b|/b).*?>","");
        updated = updated.replaceAll("\n ?" , "");
        updated = updated.replaceAll("<b", "<span");
        updated = updated.replaceAll("</b", "</span");

        Pattern specialCharacterPattern = Pattern.compile("&.*?;");
        Matcher matcher = specialCharacterPattern.matcher(updated);
        int indexDiff = 0;
        while (matcher.find()) {
            String escapedString = updated.substring(matcher.start() - indexDiff, matcher.end() - indexDiff);
            String unescapedString = Html.fromHtml(escapedString).toString();
            updated = updated.replaceFirst(escapedString, unescapedString);
            indexDiff += escapedString.length() - unescapedString.length();
        }
        return updated.trim();

    }

    public void getSuggestion(String expression, final LANGUAGE fromLang, final LANGUAGE toLang, final ClientCallBack<String[]> callBack) {
        String url = null;
        try {
            url = String.format("http://www.linguee.com/%s-%s/search?q=%s&limit=15&source=%s-%s", context.getString(fromLang.getResourceId()), context.getString(toLang.getResourceId()), URLEncoder.encode(expression, "UTF-8"), context.getString(fromLang.getResourceId()), context.getString(toLang.getResourceId()));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        Request<String[]> request = new Request<String[]>(Request.Method.GET, url, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }) {
            @Override
            protected Response<String[]> parseNetworkResponse(NetworkResponse networkResponse) {
                String result = null;
                try {
                    result = new String(networkResponse.data, context.getString(R.string.encoding));
                } catch (UnsupportedEncodingException e) {
                    callBack.onResult(null, new Error(e.getMessage()));
                }

                String[] suggestions = result.split("\\|.*\r\n");
                for (int i = 0; i < suggestions.length; i++) {
                    suggestions[i] = Html.fromHtml(suggestions[i]).toString();
                }
                return Response.success(suggestions, HttpHeaderParser.parseCacheHeaders(networkResponse));
            }

            @Override
            protected void deliverResponse(String[] result) {
                callBack.onResult(result, null);
            }
        };
        request.setTag(REQUEST_SUGGESTION_TAG);
        requestQueue.add(request);
    }

    public void removeTranslationsRequests() {
        requestQueue.cancelAll(REQUEST_TRANSLATION_TAG);
    }

}
