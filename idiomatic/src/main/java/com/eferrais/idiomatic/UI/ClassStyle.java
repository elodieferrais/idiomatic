package com.eferrais.idiomatic.UI;

import android.graphics.Color;

/**
 * Created by elodieferrais on 3/29/14.
*/
public enum ClassStyle {
    B1("b1", "#55fff888"),
    B2("b2", "#88fff888"),
    B3("b3", "#aafff888"),
    B4("b4", "#ccfff888"),
    B5("b5", "#fffff888");

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
