package com.eferrais.idiomatic.UI;

import android.graphics.Color;

/**
 * Created by elodieferrais on 3/29/14.
*/
public enum ClassStyle {
    B1("b1", "#5587CEFA"),
    B2("b2", "#8887CEFA"),
    B3("b3", "#aa87CEFA"),
    B4("b4", "#bb87CEFA"),
    B5("b5", "#cc87CEFA");

    public String name;
    private String backgroundColor;

    ClassStyle(String name, String backgroundColor) {
        this.name = name;
        this.backgroundColor = backgroundColor;
    }

    public int getBackgroundColor() {
        return Color.parseColor(backgroundColor);
    }
}
