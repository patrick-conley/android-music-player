package pconley.vamp.preferences;

import java.io.File;

import pconley.vamp.R;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.text.InputType;
import android.widget.Toast;

public class SettingsFragment extends PreferenceFragment {

	public static final String KEY_LIBRARY_PATH = "library path";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);

		// Configure the library path preference item. Input is a single line of
		// possibly non-English text, which must point to a valid, readable
		// directory.
		EditTextPreference libraryPathPref = (EditTextPreference) getPreferenceScreen()
				.findPreference(KEY_LIBRARY_PATH);

		libraryPathPref.getEditText().setInputType(
				InputType.TYPE_CLASS_TEXT
						| InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		libraryPathPref
				.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {

						File path = new File((String) newValue);

						String error = null;
						if (!path.exists()) {
							error = "Path does not exist";
						} else if (!path.isDirectory()) {
							error = "Path doesn't point to a folder";
						} else if (!path.canExecute()) {
							error = "Directory contents aren't readable";
						}

						if (error != null) {
							Toast.makeText(getActivity(), error,
									Toast.LENGTH_LONG).show();
							return false;
						}

						return true;
					}
				});
	}
}
