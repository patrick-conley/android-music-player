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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);

		// Configure the library path preference item. Input is a single line of
		// possibly non-English text, which must point to a valid, readable
		// directory.
		EditTextPreference musicFolderPathPref = (EditTextPreference) getPreferenceScreen()
				.findPreference(SettingsHelper.KEY_MUSIC_FOLDER);

		musicFolderPathPref.getEditText().setInputType(
				InputType.TYPE_CLASS_TEXT
						| InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		musicFolderPathPref
				.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {

						File dir = new File((String) newValue);

						int error = -1;
						if (!dir.exists()) {
							error = R.string.setting_library_error_no_such_path;
						} else if (!dir.isDirectory()) {
							error = R.string.setting_library_error_not_a_folder;
						} else if (!dir.canExecute()) {
							error = R.string.setting_library_error_not_readable;
						}

						if (error >= 0) {
							Toast.makeText(getActivity(), error,
									Toast.LENGTH_LONG).show();
						}

						return error == -1;
					}
				});
	}
}
