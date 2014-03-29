package com.eferrais.idiomatic.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Spanned;
import android.text.SpannedString;

/**
 * Created by elodieferrais on 2/27/14.
 */
public class Translation implements Parcelable {
    private Spanned initialText;
    private Spanned translatedText;

    public Spanned getInitialText() {
        return initialText;
    }

    public Translation(Spanned initialText, Spanned translatedText) {
        this.initialText = initialText;
        this.translatedText = translatedText;
    }

    public Spanned getTranslatedText() {
        return translatedText;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(initialText.toString());
        dest.writeString(translatedText.toString());
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
        initialText = SpannedString.valueOf(in.readString());
        translatedText = SpannedString.valueOf(in.readString());
    }
}
