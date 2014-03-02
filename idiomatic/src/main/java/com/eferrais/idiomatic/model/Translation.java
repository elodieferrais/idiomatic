package com.eferrais.idiomatic.model;

import android.text.Spanned;

/**
 * Created by elodieferrais on 2/27/14.
 */
public class Translation {
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
}
