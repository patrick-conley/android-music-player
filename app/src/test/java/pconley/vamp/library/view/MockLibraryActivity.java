package pconley.vamp.library.view;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import pconley.vamp.R;

public class MockLibraryActivity extends Activity
		implements AdapterView.OnItemClickListener {

	private int count = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_library);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		count++;
	}

	public int getCount() {
		return count;
	}
}
