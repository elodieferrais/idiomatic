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
import com.eferrais.idiomatic.model.Translation;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by elodieferrais on 2/27/14.
 */
public class TranslationClient {
    private RequestQueue requestQueue;

    public TranslationClient(Context context) {
        requestQueue = Volley.newRequestQueue(context);
    }

    public enum LANGUAGE {
        FRENCH("french"),
        ENGLISH("english");

        private String value;

        LANGUAGE(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }



    public void translationsForExpression(final String expression, final LANGUAGE fromLang, final LANGUAGE toLang, final ClientCallBack<List<Translation>> callBack) {
        String url = null;
        try {
            url = String.format("http://www.linguee.com/%s-%s/search?source=auto&query=%s", fromLang.getValue(), toLang.getValue(), URLEncoder.encode(expression, "UTF-8"));
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
                    result = new String(networkResponse.data, "ISO-8859-15");
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
                        Translation translation = new Translation(Html.fromHtml(treatString(lefts.get(i).html())), Html.fromHtml(treatString(rights.get(i).html())));
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
        requestQueue.add(request);
    }

    private String treatString(String string) {
        String updated = string.replaceAll("[\\r\\n\\w\\.-]+\\.[\\w]{1,3}", "");
        String noHtml = updated.toString().replaceAll("\\<.*?>","");
        return noHtml.replaceAll("\n " , "");
    }

    public void getSuggestion(String expression, final LANGUAGE fromLang, final LANGUAGE toLang, final ClientCallBack<String[]> callBack) {
        String url = null;
        try {
            url = String.format("http://www.linguee.com/%s-%s/search?q=%s&limit=15&source=%s-%s", fromLang.getValue(), toLang.getValue(), URLEncoder.encode(expression, "UTF-8"), fromLang.getValue(), toLang.getValue());
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
                    result = new String(networkResponse.data, "ISO-8859-15");
                } catch (UnsupportedEncodingException e) {
                    callBack.onResult(null, new Error(e.getMessage()));
                }

                String[] suggestions = result.split("\\|.*\r\n");

                return Response.success(suggestions, HttpHeaderParser.parseCacheHeaders(networkResponse));
            }

            @Override
            protected void deliverResponse(String[] result) {
                callBack.onResult(result, null);
            }
        };
        requestQueue.add(request);
    }

}
