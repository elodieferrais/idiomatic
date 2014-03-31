package com.eferrais.idiomatic.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;
import android.text.Spanned;
import android.text.SpannedString;

/**
 * Created by elodieferrais on 2/27/14.
 */
public class Translation implements Parcelable {
    private String initialText;
    private String translatedText;

    public String getInitialText() {
        return initialText;
    }

    public Translation(String initialText, String translatedText) {
        this.initialText = initialText;
        this.translatedText = translatedText;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(initialText);
        dest.writeString(translatedText);
    }

    public static final Parcelable.Creator<Translation> CREATOR
            = new Parcelable.Creator<Translation>() {
        public Translation createFromParcel(Parcel in) {
            return new Translation(in);
        }

        @Override
        public Translation[] newArray(int size) {
            return new Translation[size];
        }
    };

    private Translation(Parcel in) {
        initialText = in.readString();
        translatedText = in.readString();
    }
}
