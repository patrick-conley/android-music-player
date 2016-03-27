package pconley.vamp.preferences.view;

import android.app.Activity;
import android.os.Bundle;

import pconley.vamp.preferences.view.SettingsFragment;

public class SettingsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getFragmentManager().beginTransaction()
		                    .replace(android.R.id.content,
		                             new SettingsFragment())
		                    .commit();
	}

}
