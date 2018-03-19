package io.github.patrickconley.arbutus.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Settings {

    private final SharedPreferences preferences;

    public Settings(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

}
