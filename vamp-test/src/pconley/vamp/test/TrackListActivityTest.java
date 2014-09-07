package pconley.vamp.test;

import pconley.vamp.LibraryActivity;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

public class TrackListActivityTest extends
		ActivityInstrumentationTestCase2<LibraryActivity> {

	private LibraryActivity activity;

	public TrackListActivityTest() {
		super(LibraryActivity.class);
	}

	public void setUp() throws Exception {
		super.setUp();

		activity = getActivity();
	}

	public void testDefaultMessage() {
		TextView text = (TextView) activity
				.findViewById(pconley.vamp.R.id.text_hello);
		assertEquals("Activity holds the right text",
				activity.getText(pconley.vamp.R.string.hello), text.getText());
	}

}
