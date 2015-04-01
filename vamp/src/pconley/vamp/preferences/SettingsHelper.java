package pconley.vamp.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Simplify my interaction with SharedPreferences.
 * 
 * @author pconley
 */
public class SettingsHelper {

	public static final String KEY_MUSIC_FOLDER = "music folder";
	public static final String KEY_DEBUG = "debug mode";

	private static SharedPreferences preferences;

	public SettingsHelper(Context context) {
		if (preferences == null) {
			preferences = PreferenceManager
					.getDefaultSharedPreferences(context);
		}
	}

	/**
	 * @return Whether the app is using a sample library
	 */
	public boolean getDebugMode() {
		return preferences.getBoolean(KEY_DEBUG, false);
	}

	/**
	 * @return The path to the music folder.
	 */
	public String getMusicFolder() {
		return preferences.getString(KEY_MUSIC_FOLDER, null);
	}

	/**
	 * Switch to an alternate SharedPreferences file for the remainder of the
	 * application instance.
	 * 
	 * Meant for unit testing.
	 * 
	 * @param preferences
	 */
	public static void setPreferences(SharedPreferences preferences) {
		SettingsHelper.preferences = preferences;
	}

}